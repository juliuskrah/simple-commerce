<script lang="ts">
	import { CategoryTreeStore } from '$houdini';
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
	categoryTree.fetch();

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

	// Reactive tree structure  
	let treeData = $derived($categoryTree.data ? buildTree($categoryTree.data.categories.edges) : []);
</script>

<svelte:head>
	<title>Category Taxonomy - Simple Commerce</title>
</svelte:head>

<DashboardLayout title="Category Taxonomy Tree" {user}>
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
			<h1 class="mt-2 text-2xl font-bold text-gray-900">Category Taxonomy Tree</h1>
			<p class="mt-1 text-sm text-gray-500">
				Interactive view of your category hierarchy. Click any category to highlight its path.
			</p>
		</div>
		<div class="flex space-x-3">
			<a
				href="/dashboard/categories/new"
				class="rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500"
			>
				Add Category
			</a>
		</div>
	</div>

	{#if $categoryTree.fetching}
		<div class="text-center py-12">
			<div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent">
				<span class="sr-only">Loading...</span>
			</div>
		</div>
	{:else if treeData.length > 0}
		<div class="bg-white shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
			<div class="p-6">
				<div class="space-y-4 min-h-96">
					{#each treeData as rootNode}
						<div class="category-tree">
							{@render CategoryNode(rootNode, 0)}
						</div>
					{/each}
				</div>
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
								Edit
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
	
	<div class="category-node" style="margin-left: {depth * 24}px;">
		<div 
			class="flex items-center space-x-2 py-2 px-3 rounded-md cursor-pointer transition-colors duration-150 {
				isSelected ? 'bg-primary-100 border-2 border-primary-300' : 
				isHighlighted ? 'bg-yellow-100 border border-yellow-300' : 
				'hover:bg-gray-50 border border-transparent'
			}"
			onclick={() => selectCategory(node.id, getAncestorsPath(node.id, treeData))}
		>
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
</style>