<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$houdini';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);
	let { ProductDetail } = $derived(data);

	const product = $derived($ProductDetail.data?.product);
	const variants = $derived(product?.variants?.edges || []);

	// Helper function to format currency
	function formatCurrency(amount: number, currency: string = 'USD'): string {
		return new Intl.NumberFormat('en-US', {
			style: 'currency',
			currency: currency
		}).format(amount);
	}

	// Handle variant actions
	function editVariant(variantId: string) {
		goto(`/dashboard/products/${$page.params.id}/variants/${variantId}/edit`);
	}

	function addVariant() {
		goto(`/dashboard/products/${$page.params.id}/variants/new`);
	}

	function deleteVariant(variantId: string) {
		// TODO: Implement delete variant mutation
		console.log('Delete variant:', variantId);
	}
</script>

<DashboardLayout title="Product Details" {user}>
	{#if product}
		<!-- Product Header -->
		<div class="mb-6">
			<div class="flex items-center justify-between">
				<div>
					<nav class="mb-2 text-sm text-gray-500">
						<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
						<span class="mx-2">/</span>
						<span class="text-gray-900">{product.title}</span>
					</nav>
					<h1 class="text-2xl font-bold text-gray-900">{product.title}</h1>
				</div>
				<div class="flex space-x-3">
					<button
						class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
					>
						Edit Product
					</button>
					<button
						onclick={addVariant}
						class="bg-primary-600 hover:bg-primary-700 flex items-center rounded-md px-4 py-2 text-sm font-medium text-white"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="mr-2 h-4 w-4"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								fill-rule="evenodd"
								d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
								clip-rule="evenodd"
							/>
						</svg>
						Add Variant
					</button>
				</div>
			</div>
		</div>

		<!-- Product Information Cards -->
		<div class="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
			<!-- Basic Info Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Basic Information</h3>
				<div class="space-y-3">
					<div>
						<span class="text-sm font-medium text-gray-500">Name</span>
						<p class="text-gray-900">{product.title}</p>
					</div>
					{#if product.description}
						<div>
							<span class="text-sm font-medium text-gray-500">Description</span>
							<p class="text-gray-900">{product.description}</p>
						</div>
					{/if}
					<div>
						<span class="text-sm font-medium text-gray-500">Slug</span>
						<p class="text-gray-900">{product.slug}</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Status</span>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-medium {product.status === 'PUBLISHED'
								? 'bg-green-100 text-green-800'
								: product.status === 'DRAFT'
								? 'bg-yellow-100 text-yellow-800'
								: 'bg-red-100 text-red-800'}"
						>
							{product.status}
						</span>
					</div>
				</div>
			</div>

			<!-- Pricing Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Pricing</h3>
				<div class="space-y-3">
					{#if product.priceRange}
						<div>
							<span class="text-sm font-medium text-gray-500">Price Range</span>
							<p class="text-lg font-semibold text-gray-900">
								{#if product.priceRange.start && product.priceRange.stop}
									{formatCurrency(product.priceRange.start.amount, product.priceRange.start.currency)} - 
									{formatCurrency(product.priceRange.stop.amount, product.priceRange.stop.currency)}
								{:else if product.priceRange.start}
									From {formatCurrency(product.priceRange.start.amount, product.priceRange.start.currency)}
								{:else}
									Not set
								{/if}
							</p>
						</div>
					{/if}
					{#if product.category}
						<div>
							<span class="text-sm font-medium text-gray-500">Category</span>
							<p class="text-gray-900">{product.category.title}</p>
						</div>
					{/if}
				</div>
			</div>

			<!-- Metadata Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Metadata</h3>
				<div class="space-y-3">
					<div>
						<span class="text-sm font-medium text-gray-500">Created</span>
						<p class="text-gray-900">
							{new Date(product.createdAt).toLocaleDateString()}
						</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Updated</span>
						<p class="text-gray-900">
							{new Date(product.updatedAt).toLocaleDateString()}
						</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Variants</span>
						<p class="text-lg font-semibold text-gray-900">{variants.length}</p>
					</div>
				</div>
			</div>
		</div>

		<!-- Product Variants Section -->
		<div class="rounded-lg bg-white shadow-md">
			<div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
				<h2 class="text-lg font-semibold text-gray-800">Product Variants</h2>
				<button
					onclick={addVariant}
					class="bg-primary-600 hover:bg-primary-700 flex items-center rounded-md px-4 py-2 text-sm font-medium text-white"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
							clip-rule="evenodd"
						/>
					</svg>
					Add Variant
				</button>
			</div>

			{#if variants.length > 0}
				<!-- Variants Table -->
				<div class="overflow-x-auto">
					<table class="w-full">
						<thead>
							<tr class="bg-gray-50 text-left">
								<th class="px-6 py-3 text-sm font-medium text-gray-500">Variant</th>
								<th class="px-6 py-3 text-sm font-medium text-gray-500">SKU</th>
								<th class="px-6 py-3 text-sm font-medium text-gray-500">Price</th>
								<th class="px-6 py-3 text-sm font-medium text-gray-500">Digital Content</th>
								<th class="px-6 py-3 text-sm font-medium text-gray-500">Actions</th>
							</tr>
						</thead>
						<tbody>
							{#each variants as variantEdge (variantEdge?.node.id)}
								{#if variantEdge?.node}
									{@const variant = variantEdge.node}
								<tr class="border-t border-gray-100 hover:bg-gray-50">
									<td class="px-6 py-4">
										<div>
											<p class="font-medium text-gray-800">{variant.title}</p>
										</div>
									</td>
									<td class="px-6 py-4 text-gray-800">{variant.sku || 'N/A'}</td>
									<td class="px-6 py-4 text-gray-800">
										{variant.price ? formatCurrency(variant.price.amount, variant.price.currency) : 'N/A'}
									</td>
									<td class="px-6 py-4">
										{#if variant.digitalContent}
											<a
												href={variant.digitalContent.url}
												target="_blank"
												rel="noopener noreferrer"
												class="text-primary-600 hover:text-primary-700 text-sm"
											>
												View Content
											</a>
										{:else}
											<span class="text-gray-500 text-sm">None</span>
										{/if}
									</td>
									<td class="px-6 py-4">
										<div class="flex space-x-2">
											<button
												onclick={() => editVariant(variant.id)}
												class="hover:text-primary-600 text-gray-400"
												aria-label="Edit variant"
											>
												<svg
													xmlns="http://www.w3.org/2000/svg"
													class="h-5 w-5"
													viewBox="0 0 20 20"
													fill="currentColor"
												>
													<path
														d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
													/>
												</svg>
											</button>
											<button
												onclick={() => deleteVariant(variant.id)}
												class="text-gray-400 hover:text-red-600"
												aria-label="Delete variant"
											>
												<svg
													xmlns="http://www.w3.org/2000/svg"
													class="h-5 w-5"
													viewBox="0 0 20 20"
													fill="currentColor"
												>
													<path
														fill-rule="evenodd"
														d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
														clip-rule="evenodd"
													/>
												</svg>
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
				<!-- Empty State -->
				<div class="flex flex-col items-center justify-center py-12">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-12 w-12 text-gray-400"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
						/>
					</svg>
					<h3 class="mt-2 text-sm font-medium text-gray-900">No variants</h3>
					<p class="mt-1 text-sm text-gray-500">Get started by creating a new variant.</p>
					<button
						onclick={addVariant}
						class="bg-primary-600 hover:bg-primary-700 mt-4 inline-flex items-center rounded-md px-4 py-2 text-sm font-medium text-white"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="mr-2 h-4 w-4"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								fill-rule="evenodd"
								d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
								clip-rule="evenodd"
							/>
						</svg>
						Add Variant
					</button>
				</div>
			{/if}
		</div>
	{:else}
		<!-- Loading or Error State -->
		<div class="flex items-center justify-center py-12">
			<div class="text-center">
				<h3 class="text-lg font-medium text-gray-900">Product not found</h3>
				<p class="mt-2 text-sm text-gray-500">
					The product you're looking for doesn't exist or you don't have permission to view it.
				</p>
				<a
					href="/dashboard/products"
					class="bg-primary-600 hover:bg-primary-700 mt-4 inline-flex items-center rounded-md px-4 py-2 text-sm font-medium text-white"
				>
					Back to Products
				</a>
			</div>
		</div>
	{/if}
</DashboardLayout>
