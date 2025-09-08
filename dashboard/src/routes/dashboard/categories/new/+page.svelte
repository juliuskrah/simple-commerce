<script lang="ts">
	import { goto } from '$app/navigation';
	import { notifications } from '$lib/stores/notifications';
	import { AddCategoryStore, CategoriesStore } from '$houdini';
	import { superForm } from 'sveltekit-superforms/client';
	import { z } from 'zod';
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);

	// Load parent categories for dropdown
	const categories = new CategoriesStore();
	categories.fetch();

	// Create mutation store
	const addCategory = new AddCategoryStore(); 

	// Form validation schema
	const categorySchema = z.object({
		title: z.string().min(1, 'Title is required'),
		slug: z.string().optional(),
		description: z.string().optional(),
		parentId: z.string().optional()
	});

	let isSubmitting = false;

	// Form state
	let titleVal = '';
	let slugVal = '';
	let descriptionVal = '';
	let parentIdVal = '';

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

			const result = await addCategory.mutate({ input });
			
			if (result.data?.addCategory) {
				notifications.push({ type: 'success', message: 'Category created successfully' });
				goto('/dashboard/categories');
			}
		} catch (error) {
			console.error('Error creating category:', error);
			notifications.push({ type: 'error', message: 'Failed to create category' });
		} finally {
			isSubmitting = false;
		}
	}
</script>

<svelte:head>
	<title>New Category - Simple Commerce</title>
</svelte:head>

<DashboardLayout title="Create New Category" {user}>
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
		<h1 class="mt-2 text-2xl font-bold text-gray-900">Create New Category</h1>
		<p class="mt-1 text-sm text-gray-500">
			Add a new category to organize your products
		</p>
	</div>

	<div class="mx-auto max-w-2xl">
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
									placeholder="Electronics"
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
									placeholder="electronics (leave empty to auto-generate)"
								/>
							</div>
							<p class="mt-2 text-xs text-gray-500">
								URL-friendly version of the title. Leave empty to auto-generate from title.
							</p>
						</div>

						<!-- Parent Category -->
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
									{#if $categories.data}
										{#each $categories.data.categories.edges as edge}
											{@const category = edge.node}
											<option value={category.id}>
												{category.breadCrumb} ({category.title})
											</option>
										{/each}
									{/if}
								</select>
							</div>
							<p class="mt-2 text-xs text-gray-500">
								Select a parent category to create a subcategory, or leave blank for a root category.
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
									placeholder="Optional description for this category..."
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
						{isSubmitting ? 'Creating...' : 'Create Category'}
					</button>
				</div>
			</div>
		</form>
	</div>
</div>
</DashboardLayout>