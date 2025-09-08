<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { notifications } from '$lib/stores/notifications';
	import { CategoryDetailsStore, UpdateCategoryStore, CategoriesStore } from '$houdini';
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);

	// Get category ID from URL
	const categoryId = $page.params.id;

	// Load category details and available parent categories
	const categoryDetails = new CategoryDetailsStore();
	const categories = new CategoriesStore();
	const updateCategory = new UpdateCategoryStore();

	// Fetch data
	categoryDetails.fetch({ variables: { id: categoryId } });
	categories.fetch();

	let isSubmitting = false;

	// Form state - will be populated when data loads
	let titleVal = '';
	let slugVal = '';
	let descriptionVal = '';
	let parentIdVal = '';

	// Update form state when data loads
	$effect(() => {
		if ($categoryDetails.data?.category) {
			const category = $categoryDetails.data.category;
			titleVal = category.title || '';
			slugVal = category.slug || '';
			descriptionVal = category.description || '';
			parentIdVal = category.parent?.id || '';
		}
	});

	// Handle form submission
	async function handleSubmit() {
		isSubmitting = true;
		
		try {
			const input = {
				title: titleVal,
				...(slugVal && { slug: slugVal }),
				...(descriptionVal && { description: descriptionVal }),
				...(parentIdVal && { parentId: parentIdVal })
			};

			const result = await updateCategory.mutate({ 
				id: categoryId, 
				input 
			});
			
			if (result.data?.updateCategory) {
				notifications.push({ type: 'success', message: 'Category updated successfully' });
				goto('/dashboard/categories');
			}
		} catch (error) {
			console.error('Error updating category:', error);
			notifications.push({ type: 'error', message: 'Failed to update category' });
		} finally {
			isSubmitting = false;
		}
	}

	// Get filtered parent options (exclude self and descendants to prevent cycles)
	$: parentOptions = $categories.data 
		? $categories.data.categories.edges.filter(edge => {
			if (!edge?.node) return false;
			const node = edge.node;
			
			// Exclude self
			if (node.id === categoryId) return false;
			
			// Exclude if this category would be an ancestor 
			// (i.e., prevent moving a parent under its child)
			if ($categoryDetails.data?.category?.breadCrumb && 
				node.breadCrumb.startsWith($categoryDetails.data.category.breadCrumb)) {
				return false;
			}
			
			return true;
		}).map(edge => edge!.node)
		: [];
</script>

<svelte:head>
	<title>Edit Category - Simple Commerce</title>
</svelte:head>

<DashboardLayout title="Edit Category" {user}>
<div class="p-0">
	<div class="mb-6">
		<div class="flex items-center space-x-4">
			<a
				href="/dashboard/categories"
				class="text-gray-400 hover:text-gray-600"
			>
				← Back to Categories
			</a>
		</div>
		{#if $categoryDetails.data?.category}
			<h1 class="mt-2 text-2xl font-bold text-gray-900">
				Edit Category: {$categoryDetails.data.category.title}
			</h1>
			<p class="mt-1 text-sm text-gray-500">
				Update category details and hierarchy
			</p>
		{:else}
			<h1 class="mt-2 text-2xl font-bold text-gray-900">Edit Category</h1>
		{/if}
	</div>

	{#if $categoryDetails.fetching}
		<div class="text-center py-12">
			<div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent">
				<span class="sr-only">Loading...</span>
			</div>
		</div>
	{:else if $categoryDetails.data?.category}
		<div class="mx-auto max-w-2xl">
			<!-- Current Category Info -->
			<div class="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
				<h3 class="text-sm font-medium text-blue-900">Current Category Information:</h3>
				<div class="mt-2 text-sm text-blue-800">
					<p><strong>Current Path:</strong> {$categoryDetails.data.category.breadCrumb}</p>
					<p><strong>Level:</strong> {$categoryDetails.data.category.level}</p>
					<p><strong>Type:</strong> 
						{#if $categoryDetails.data.category.isRoot}Root Category
						{:else if $categoryDetails.data.category.isLeaf}Leaf Category
						{:else}Branch Category
						{/if}
					</p>
				</div>
			</div>

			<form on:submit|preventDefault={handleSubmit} class="space-y-6">
				<div class="bg-white shadow-sm ring-1 ring-gray-900/5 sm:rounded-xl">
					<div class="px-4 py-6 sm:p-8">
						<div class="grid max-w-2xl grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
							<!-- Title -->
							<div class="sm:col-span-4">
								<label for="title" class="block text-sm font-medium leading-6 text-gray-900">
									Title
								</label>
								<div class="mt-2">
									<input
										id="title"
										name="title"
										type="text"
										bind:value={titleVal}
										required
										class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-primary-600 sm:text-sm sm:leading-6"
									/>
								</div>
							</div>

							<!-- Slug -->
							<div class="sm:col-span-4">
								<label for="slug" class="block text-sm font-medium leading-6 text-gray-900">
									Slug
								</label>
								<div class="mt-2">
									<input
										id="slug"
										name="slug"
										type="text"
										bind:value={slugVal}
										class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-primary-600 sm:text-sm sm:leading-6"
									/>
								</div>
								<p class="mt-2 text-xs text-gray-500">
									URL-friendly version of the title.
								</p>
							</div>

							<!-- Parent Category with Replanting Warning -->
							<div class="sm:col-span-4">
								<label for="parentId" class="block text-sm font-medium leading-6 text-gray-900">
									Parent Category
								</label>
								<div class="mt-2">
									<select
										id="parentId"
										name="parentId"
										bind:value={parentIdVal}
										class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-primary-600 sm:text-sm sm:leading-6"
									>
										<option value="">-- Root Category --</option>
										{#each parentOptions as category}
											<option value={category.id}>
												{category.breadCrumb} ({category.title})
											</option>
										{/each}
									</select>
								</div>
								{#if parentIdVal !== ($categoryDetails.data.category.parent?.id || '')}
									<div class="mt-2 p-2 bg-yellow-50 border border-yellow-200 rounded-md">
										<div class="flex">
											<div class="flex-shrink-0">
												<svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
													<path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
												</svg>
											</div>
											<div class="ml-3">
												<h3 class="text-sm font-medium text-yellow-800">
													Branch Replanting
												</h3>
												<div class="mt-1 text-sm text-yellow-700">
													<p>
														Changing the parent will move this category and all its subcategories to a new location in the hierarchy. This operation affects the entire branch.
													</p>
												</div>
											</div>
										</div>
									</div>
								{/if}
								<p class="mt-2 text-xs text-gray-500">
									Change parent to move this category and all its children to a different part of the tree.
								</p>
							</div>

							<!-- Description -->
							<div class="col-span-full">
								<label for="description" class="block text-sm font-medium leading-6 text-gray-900">
									Description
								</label>
								<div class="mt-2">
									<textarea
										id="description"
										name="description"
										rows="3"
										bind:value={descriptionVal}
										class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-primary-600 sm:text-sm sm:leading-6"
									></textarea>
								</div>
							</div>
						</div>
					</div>
					
					<div class="flex items-center justify-end gap-x-6 border-t border-gray-900/10 px-4 py-4 sm:px-8">
						<a
							href="/dashboard/categories"
							class="text-sm font-semibold leading-6 text-gray-900"
						>
							Cancel
						</a>
						<button
							type="submit"
							disabled={isSubmitting}
							class="rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 disabled:opacity-50"
						>
							{isSubmitting ? 'Updating...' : 'Update Category'}
						</button>
					</div>
				</div>
			</form>
		</div>
	{:else}
		<div class="text-center py-12">
			<p class="text-gray-500">Category not found.</p>
			<a
				href="/dashboard/categories"
				class="mt-4 inline-flex items-center rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500"
			>
				Back to Categories
			</a>
		</div>
	{/if}
</div>
</DashboardLayout>