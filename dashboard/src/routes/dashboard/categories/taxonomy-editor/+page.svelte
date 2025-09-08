<script lang="ts">
	import { CategoryTreeStore, UpdateCategoryStore } from '$houdini';
	import { notifications } from '$lib/stores/notifications';
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);

	// Load full category tree
	const categoryTree = new CategoryTreeStore();
	const updateCategory = new UpdateCategoryStore();
	categoryTree.fetch();

	// State for drag and drop
	let draggedCategory = $state<any>(null);
	let dropTarget = $state<any>(null);
	let isDragging = $state(false);

	// State for selected category and highlighted path
	let selectedCategoryId = $state<string | null>(null);
	let highlightedPath = $state<Set<string>>(new Set());

	// Function to build tree structure from flat edges
	function buildTree(edges: any[]) {
		const nodeMap = new Map();
		const rootNodes = [];

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

	// Handle category selection and path highlighting
	function selectCategory(categoryId: string, ancestors: string[] = []) {
		selectedCategoryId = categoryId;
		
		// Highlight the path from root to selected category
		const pathSet = new Set(ancestors);
		pathSet.add(categoryId);
		highlightedPath = pathSet;
	}

	// Get ancestors path for a category
	function getAncestorsPath(categoryId: string, tree: any[]): string[] {
		function findPath(nodes: any[], targetId: string, currentPath: string[]): string[] | null {
			for (const node of nodes) {
				const newPath = [...currentPath, node.id];
				
				if (node.id === targetId) {
					return newPath;
				}
				
				if (node.children?.length > 0) {
					const found = findPath(node.children, targetId, newPath);
					if (found) return found;
				}
			}
			return null;
		}
		
		const path = findPath(tree, categoryId, []);
		return path?.slice(0, -1) || []; // Exclude the target category itself
	}

	// Drag and drop handlers
	function handleDragStart(event: DragEvent, category: any) {
		if (!event.dataTransfer) return;
		
		draggedCategory = category;
		isDragging = true;
		event.dataTransfer.effectAllowed = 'move';
		event.dataTransfer.setData('text/plain', category.id);
		
		// Add visual feedback
		const target = event.target as HTMLElement;
		target.style.opacity = '0.5';
	}

	function handleDragEnd(event: DragEvent) {
		draggedCategory = null;
		dropTarget = null;
		isDragging = false;
		
		// Reset visual feedback
		const target = event.target as HTMLElement;
		target.style.opacity = '1';
	}

	function handleDragOver(event: DragEvent, category: any) {
		event.preventDefault();
		event.stopPropagation();
		
		if (!draggedCategory || draggedCategory.id === category.id) return;
		
		// Prevent dropping on descendants (would create a cycle)
		if (category.breadCrumb.startsWith(draggedCategory.breadCrumb + ' >')) {
			event.dataTransfer!.dropEffect = 'none';
			return;
		}
		
		dropTarget = category;
		event.dataTransfer!.dropEffect = 'move';
	}

	function handleDragLeave(event: DragEvent) {
		// Only clear drop target if we're leaving the element entirely
		const target = event.target as HTMLElement;
		const relatedTarget = event.relatedTarget as HTMLElement;
		
		if (!target.contains(relatedTarget)) {
			dropTarget = null;
		}
	}

	async function handleDrop(event: DragEvent, newParent: any) {
		event.preventDefault();
		event.stopPropagation();
		
		if (!draggedCategory || draggedCategory.id === newParent.id) {
			return;
		}
		
		// Prevent dropping on descendants
		if (newParent.breadCrumb.startsWith(draggedCategory.breadCrumb + ' >')) {
			notifications.push({ type: 'error', message: 'Cannot move category under its own descendant' });
			return;
		}

		try {
			// Update the category with new parent
			const input = {
				title: draggedCategory.title,
				slug: draggedCategory.slug,
				description: draggedCategory.description || undefined,
				parentId: newParent.id
			};

			const result = await updateCategory.mutate({ 
				id: draggedCategory.id, 
				input 
			});
			
			if (result.data?.updateCategory) {
				notifications.push({ type: 'success', message: `Moved "${draggedCategory.title}" under "${newParent.title}"` });
				categoryTree.fetch(); // Refresh the tree
				
				// Clear drag state
				draggedCategory = null;
				dropTarget = null;
			}
		} catch (error) {
			console.error('Error moving category:', error);
			notifications.push({ type: 'error', message: 'Failed to move category' });
		}
	}

	// Handle drop on root (move to top level)
	async function handleDropOnRoot(event: DragEvent) {
		event.preventDefault();
		
		if (!draggedCategory) return;

		try {
			// Update the category to be a root category
			const input = {
				title: draggedCategory.title,
				slug: draggedCategory.slug,
				description: draggedCategory.description || undefined,
				parentId: undefined
			};

			const result = await updateCategory.mutate({ 
				id: draggedCategory.id, 
				input 
			});
			
			if (result.data?.updateCategory) {
				notifications.push({ type: 'success', message: `Moved "${draggedCategory.title}" to root level` });
				categoryTree.fetch(); // Refresh the tree
				
				// Clear drag state
				draggedCategory = null;
				dropTarget = null;
			}
		} catch (error) {
			console.error('Error moving category:', error);
			notifications.push({ type: 'error', message: 'Failed to move category' });
		}
	}

	// Reactive tree structure  
	let treeData = $derived($categoryTree.data ? buildTree($categoryTree.data.categories.edges) : []);
</script>

<svelte:head>
	<title>Category Taxonomy Editor - Simple Commerce</title>
</svelte:head>

<DashboardLayout title="Category Taxonomy Editor" {user}>
<div class="p-0">
	<div class="mb-6 flex items-center justify-between">
		<div>
			<div class="flex items-center space-x-4">
				<a
					href="/dashboard/categories"
					class="text-gray-400 hover:text-gray-600"
				>
					← Back to Categories
				</a>
			</div>
			<h1 class="mt-2 text-2xl font-bold text-gray-900">Category Taxonomy Editor</h1>
			<p class="mt-1 text-sm text-gray-500">
				Drag and drop categories to reorganize your taxonomy structure. Click to select and view details.
			</p>
		</div>
		<div class="flex space-x-3">
			<a
				href="/dashboard/categories/taxonomy"
				class="rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
			>
				View Only
			</a>
			<a
				href="/dashboard/categories/new"
				class="rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500"
			>
				Add Category
			</a>
		</div>
	</div>

	<!-- Drag and Drop Instructions -->
	<div class="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
		<div class="flex">
			<div class="flex-shrink-0">
				<svg class="h-5 w-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
					<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
				</svg>
			</div>
			<div class="ml-3">
				<h3 class="text-sm font-medium text-blue-800">
					How to use the taxonomy editor:
				</h3>
				<div class="mt-1 text-sm text-blue-700">
					<ul class="list-disc list-inside space-y-1">
						<li><strong>Drag:</strong> Click and hold any category to start dragging</li>
						<li><strong>Drop:</strong> Drop on another category to make it a child, or drop on the root area to make it a top-level category</li>
						<li><strong>Select:</strong> Click any category to view its details and edit options</li>
						<li><strong>Restrictions:</strong> You cannot move a category under its own descendants</li>
					</ul>
				</div>
			</div>
		</div>
	</div>

	{#if $categoryTree.fetching}
		<div class="text-center py-12">
			<div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent">
				<span class="sr-only">Loading...</span>
			</div>
		</div>
	{:else if treeData.length > 0}
		<!-- Drop zone for root level -->
		<div 
			class="bg-white shadow ring-1 ring-black ring-opacity-5 md:rounded-lg min-h-96 p-6 {
				isDragging && !dropTarget ? 'bg-blue-50 border-2 border-dashed border-blue-300' : ''
			}"
			ondragover={(e) => { 
				if (isDragging) {
					e.preventDefault(); 
					dropTarget = null;
				}
			}}
			ondrop={handleDropOnRoot}
		>
			{#if isDragging && !dropTarget}
				<div class="text-center py-8 text-blue-600">
					<svg class="mx-auto h-12 w-12 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
					</svg>
					<p class="text-sm font-medium">Drop here to move to root level</p>
				</div>
			{/if}

			<div class="space-y-4">
				{#each treeData as rootNode}
					<div class="category-tree">
						{@render CategoryNode(rootNode, 0)}
					</div>
				{/each}
			</div>
		</div>

		{#if selectedCategoryId}
			<div class="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
				<h3 class="text-sm font-medium text-blue-900">Selected Category Information:</h3>
				{#if $categoryTree.data}
					{@const selectedCategory = $categoryTree.data.categories.edges.find(edge => edge?.node.id === selectedCategoryId)?.node}
					{#if selectedCategory}
						<div class="mt-2 text-sm text-blue-800">
							<p><strong>Title:</strong> {selectedCategory.title}</p>
							<p><strong>Slug:</strong> /{selectedCategory.slug}</p>
							<p><strong>Path:</strong> {selectedCategory.breadCrumb}</p>
							<p><strong>Level:</strong> {selectedCategory.level}</p>
							<p><strong>Type:</strong> 
								{#if selectedCategory.isRoot}Root Category
								{:else if selectedCategory.isLeaf}Leaf Category
								{:else}Branch Category
								{/if}
							</p>
						</div>
						<div class="mt-3 flex space-x-2">
							<a
								href="/dashboard/categories/{selectedCategory.id}/edit"
								class="inline-flex items-center rounded-md bg-blue-600 px-2 py-1 text-xs font-semibold text-white shadow-sm hover:bg-blue-500"
							>
								Edit Details
							</a>
						</div>
					{/if}
				{/if}
			</div>
		{/if}
	{:else}
		<div class="text-center py-12">
			<p class="text-gray-500">No categories found.</p>
			<a
				href="/dashboard/categories/new"
				class="mt-4 inline-flex items-center rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500"
			>
				Create your first category
			</a>
		</div>
	{/if}
</div>
</DashboardLayout>

{#snippet CategoryNode(node: any, depth: number)}
	{@const isHighlighted = highlightedPath.has(node.id)}
	{@const isSelected = selectedCategoryId === node.id}
	{@const isDropTarget = dropTarget?.id === node.id}
	{@const isDragSource = draggedCategory?.id === node.id}
	
	<div class="category-node" style="margin-left: {depth * 24}px;">
		<div 
			class="flex items-center space-x-2 py-2 px-3 rounded-md cursor-pointer transition-all duration-150 {
				isSelected ? 'bg-primary-100 border-2 border-primary-300' : 
				isHighlighted ? 'bg-yellow-100 border border-yellow-300' : 
				isDropTarget ? 'bg-green-100 border-2 border-green-300' :
				isDragSource ? 'bg-gray-100 border border-gray-300 opacity-50' :
				'hover:bg-gray-50 border border-transparent'
			}"
			draggable="true"
			ondragstart={(e) => handleDragStart(e, node)}
			ondragend={handleDragEnd}
			ondragover={(e) => handleDragOver(e, node)}
			ondragleave={handleDragLeave}
			ondrop={(e) => handleDrop(e, node)}
			onclick={() => selectCategory(node.id, getAncestorsPath(node.id, treeData))}
		>
			<div class="flex-shrink-0">
				<svg class="h-4 w-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
					<path d="M7 2a1 1 0 011-1h4a1 1 0 011 1v2h3a1 1 0 110 2h-1v12a1 1 0 01-1 1H6a1 1 0 01-1-1V6H4a1 1 0 010-2h3V2zM6 6v12h8V6H6z"/>
					<path d="M8 8a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zM8 12a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z"/>
				</svg>
			</div>
			
			<div class="flex-shrink-0">
				{#if node.isRoot}
					<span class="text-primary-600 text-lg">📁</span>
				{:else if node.isLeaf}
					<span class="text-gray-400 text-lg">📄</span>
				{:else}
					<span class="text-yellow-600 text-lg">📂</span>
				{/if}
			</div>
			
			<div class="flex-1 min-w-0">
				<div class="flex items-center space-x-2">
					<span class="text-sm font-medium text-gray-900 truncate">
						{node.title}
					</span>
					<span class="text-xs text-gray-500">
						/{node.slug}
					</span>
				</div>
				{#if node.description}
					<div class="text-xs text-gray-500 truncate">
						{node.description}
					</div>
				{/if}
			</div>
			
			<div class="flex-shrink-0 flex items-center space-x-1">
				<span class="inline-flex items-center rounded-md bg-gray-100 px-1.5 py-0.5 text-xs font-medium text-gray-600">
					L{node.level}
				</span>
				{#if isSelected}
					<span class="text-primary-600 text-xs">●</span>
				{/if}
				{#if isDropTarget}
					<span class="text-green-600 text-xs">↓</span>
				{/if}
			</div>
		</div>
		
		{#if node.children?.length > 0}
			<div class="mt-1">
				{#each node.children as child}
					{@render CategoryNode(child, depth + 1)}
				{/each}
			</div>
		{/if}
	</div>
{/snippet}

<style>
	.category-tree {
		font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
	}
	
	.category-node {
		position: relative;
	}
	
	.category-node::before {
		content: '';
		position: absolute;
		left: -12px;
		top: 0;
		bottom: 0;
		width: 1px;
		background-color: #e5e7eb;
	}
	
	.category-node::after {
		content: '';
		position: absolute;
		left: -12px;
		top: 50%;
		width: 8px;
		height: 1px;
		background-color: #e5e7eb;
	}

	.category-node [draggable="true"]:hover {
		cursor: grab;
	}

	.category-node [draggable="true"]:active {
		cursor: grabbing;
	}
</style>