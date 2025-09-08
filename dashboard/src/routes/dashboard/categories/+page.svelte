<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import { CategoriesStore } from '$houdini';
	import { notifications } from '$lib/stores/notifications';
	import CategoryDeleteModal from '$lib/components/CategoryDeleteModal.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);

	// Load categories data
	const categories = new CategoriesStore();
	categories.fetch();

	// Delete modal state
	let showDeleteModal = $state(false);
	let categoryToDelete = $state<{
		id: string;
		title: string;
		breadCrumb: string;
		isLeaf: boolean;
	} | null>(null);

	// Show delete confirmation modal
	function showDeleteConfirmation(category: any) {
		categoryToDelete = {
			id: category.id,
			title: category.title,
			breadCrumb: category.breadCrumb,
			isLeaf: category.isLeaf
		};
		showDeleteModal = true;
	}

	// Handle successful deletion
	function handleCategoryDeleted() {
		categories.fetch(); // Refresh the list
		categoryToDelete = null;
	}

	// Close modal
	function closeDeleteModal() {
		showDeleteModal = false;
		categoryToDelete = null;
	}
</script>

<svelte:head>
	<title>Categories - Simple Commerce</title>
</svelte:head>

<DashboardLayout title="Categories" {user}>
	<div class="p-0">
	<div class="mb-6 flex items-center justify-between">
		<div>
			<h1 class="text-2xl font-bold text-gray-900">Categories</h1>
			<p class="mt-1 text-sm text-gray-500">
				Manage your product categories and taxonomy structure
			</p>
		</div>
		<div class="flex space-x-3">
			<a
				href="/dashboard/categories/taxonomy"
				class="rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
			>
				View Tree
			</a>
			<a
				href="/dashboard/categories/taxonomy-editor"
				class="rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
			>
				Drag & Drop Editor
			</a>
			<a
				href="/dashboard/categories/new"
				class="rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
			>
				Add Category
			</a>
		</div>
	</div>

	{#if $categories.fetching}
		<div class="text-center">
			<div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]">
				<span class="!absolute !-m-px !h-px !w-px !overflow-hidden !whitespace-nowrap !border-0 !p-0 ![clip:rect(0,0,0,0)]">Loading...</span>
			</div>
		</div>
	{:else if $categories.data}
		<div class="overflow-hidden bg-white shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
			<table class="min-w-full divide-y divide-gray-300">
				<thead class="bg-gray-50">
					<tr>
						<th scope="col" class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
							Category
						</th>
						<th scope="col" class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
							Breadcrumb
						</th>
						<th scope="col" class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
							Level
						</th>
						<th scope="col" class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
							Status
						</th>
						<th scope="col" class="relative px-6 py-3">
							<span class="sr-only">Actions</span>
						</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					{#each $categories.data.categories.edges as edge}
						{#if edge}
							{@const category = edge.node}
						<tr class="hover:bg-gray-50">
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="flex items-center">
									<div class="flex-shrink-0">
										{#if category.isRoot}
											<span class="text-primary-600">📁</span>
										{:else if category.isLeaf}
											<span class="text-gray-400">📄</span>
										{:else}
											<span class="text-yellow-600">📂</span>
										{/if}
									</div>
									<div class="ml-4">
										<div class="text-sm font-medium text-gray-900">{category.title}</div>
										<div class="text-sm text-gray-500">/{category.slug}</div>
									</div>
								</div>
							</td>
							<td class="px-6 py-4">
								<div class="text-sm text-gray-900">{category.breadCrumb}</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<span class="inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-800">
									Level {category.level}
								</span>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								{#if category.isRoot}
									<span class="inline-flex items-center rounded-md bg-blue-100 px-2 py-1 text-xs font-medium text-blue-800">
										Root
									</span>
								{:else if category.isLeaf}
									<span class="inline-flex items-center rounded-md bg-green-100 px-2 py-1 text-xs font-medium text-green-800">
										Leaf
									</span>
								{:else}
									<span class="inline-flex items-center rounded-md bg-yellow-100 px-2 py-1 text-xs font-medium text-yellow-800">
										Branch
									</span>
								{/if}
							</td>
							<td class="relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
								<div class="flex items-center justify-end space-x-2">
									<a
										href="/dashboard/categories/{category.id}/edit"
										class="text-primary-600 hover:text-primary-900"
									>
										Edit
									</a>
									<button
										type="button"
										class="text-red-600 hover:text-red-900"
										onclick={() => showDeleteConfirmation(category)}
									>
										Delete
									</button>
								</div>
							</td>
						</tr>
						{/if}
					{/each}
				</tbody>
			</table>
		</div>
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

<!-- Delete confirmation modal -->
<CategoryDeleteModal 
	isOpen={showDeleteModal}
	category={categoryToDelete}
	onClose={closeDeleteModal}
	onDeleted={handleCategoryDeleted}
/>