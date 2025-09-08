<script lang="ts">
	import { CategoriesStore } from '$houdini';

	interface Props {
		selectedCategoryId?: string;
		onCategorySelect: (categoryId: string | null) => void;
		placeholder?: string;
		error?: string;
		required?: boolean;
	}

	let {
		selectedCategoryId = '',
		onCategorySelect,
		placeholder = 'Select a category...',
		error,
		required = false
	}: Props = $props();

	// Load categories from database
	const categories = new CategoriesStore();
	categories.fetch();

	// Build hierarchical tree structure for grouped display
	function buildCategoryTree(edges: any[]) {
		const nodeMap = new Map();
		const rootNodes: any[] = [];

		// First pass: create all nodes
		edges.forEach(edge => {
			if (edge?.node) {
				nodeMap.set(edge.node.id, {
					...edge.node,
					children: []
				});
			}
		});

		// Second pass: organize into tree structure
		edges.forEach(edge => {
			if (edge?.node) {
				const node = nodeMap.get(edge.node.id);
				if (node.parent?.id) {
					const parent = nodeMap.get(node.parent.id);
					if (parent) {
						parent.children.push(node);
					}
				} else {
					rootNodes.push(node);
				}
			}
		});

		return rootNodes;
	}

	// Flatten tree for option rendering with proper indentation
	function flattenTreeWithIndentation(nodes: any[], depth = 0): any[] {
		let flattened: any[] = [];
		
		nodes.forEach(node => {
			flattened.push({
				...node,
				depth,
				displayTitle: '　'.repeat(depth) + (depth > 0 ? '└ ' : '') + node.title
			});
			
			if (node.children?.length > 0) {
				flattened = flattened.concat(flattenTreeWithIndentation(node.children, depth + 1));
			}
		});
		
		return flattened;
	}

	// Handle selection change
	function handleSelectionChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		const value = target.value;
		onCategorySelect(value || null);
	}

	// Reactive tree structure
	const treeData = $derived($categories.data ? buildCategoryTree($categories.data.categories.edges) : []);
	const flattenedOptions = $derived(flattenTreeWithIndentation(treeData));

	// Get selected category display info
	const selectedCategory = $derived(flattenedOptions.find(cat => cat.id === selectedCategoryId));
</script>

<div class="category-selector">
	<select
		value={selectedCategoryId}
		onchange={handleSelectionChange}
		class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500 {error ? 'border-red-300' : ''}"
		{required}
		aria-invalid={error ? 'true' : 'false'}
	>
		<option value="">{placeholder}</option>
		
		{#if $categories.fetching}
			<option disabled>Loading categories...</option>
		{:else if flattenedOptions.length > 0}
			{#each flattenedOptions as category}
				<option 
					value={category.id}
					class="category-option level-{category.depth}"
				>
					{category.displayTitle}
				</option>
			{/each}
		{:else}
			<option disabled>No categories available</option>
		{/if}
	</select>

	{#if selectedCategory}
		<div class="mt-1 text-xs text-gray-500">
			<span class="font-medium">Path:</span> {selectedCategory.breadCrumb}
		</div>
	{/if}

	{#if error}
		<p class="mt-1 text-xs text-red-600">{error}</p>
	{/if}
</div>

<style>
	.category-selector select {
		font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
	}
	
	.category-option.level-0 {
		font-weight: 600;
	}
	
	.category-option.level-1 {
		color: #374151;
	}
	
	.category-option.level-2 {
		color: #6b7280;
	}
	
	.category-option.level-3 {
		color: #9CA3AF;
	}
</style>