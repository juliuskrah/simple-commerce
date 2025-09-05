<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$houdini';
	import { goto } from '$app/navigation';

	interface Props {
		data: PageData;
	}

	/** @type { import('undefined').Props } */
	let { data }: Props = $props();
	const user = $derived(data.user);
	let { Products } = $derived(data);
</script>

<DashboardLayout title="Products" {user}>
	<div class="mb-8 rounded-lg bg-white shadow-md">
		<div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
			<h2 class="text-lg font-semibold text-gray-800">Products</h2>
			<button 
			class="flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm text-white"
			onclick={() => goto('/dashboard/products/new')}
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
				Add Product
			</button>
		</div>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow-md">
		<div class="flex items-center justify-between border-b p-4">
			<div class="relative">
				<input
					type="text"
					placeholder="Search products..."
					class="rounded-lg border py-2 pl-10 pr-4 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
				/>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="absolute left-3 top-2.5 h-5 w-5 text-gray-400"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
						clip-rule="evenodd"
					/>
				</svg>
			</div>

			<div class="flex space-x-2">
				<select
					class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
				>
					<option>All Categories</option>
					<option>Accessories</option>
					<option>Electronics</option>
					<option>Clothing</option>
				</select>
				<select
					class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
				>
					<option>All Status</option>
					<option>Active</option>
					<option>Low stock</option>
					<option>Out of stock</option>
				</select>
			</div>
		</div>

		<table class="w-full">
			<thead>
				<tr class="bg-gray-50 text-left">
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Product</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Price Range</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Category</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Status</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Created</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Actions</th>
				</tr>
			</thead>
			<tbody>
				{#if $Products.data?.products.edges.length}
					{#each $Products.data.products.edges as edge (edge?.node.id)}
						{#if edge?.node}
							{@const product = edge.node}
							<tr class="border-t border-gray-100 hover:bg-gray-50">
								<td class="px-6 py-4">
									<div class="flex items-center">
										<div class="h-10 w-10 flex-shrink-0 rounded bg-gray-200"></div>
										<div class="ml-3">
											<p class="font-medium text-gray-800">{product.title}</p>
											{#if product.description}
												<p class="text-sm text-gray-500">{product.description}</p>
											{/if}
										</div>
									</div>
								</td>
								<td class="px-6 py-4 text-gray-800">
									{#if product.priceRange}
										{#if product.priceRange.start && product.priceRange.stop}
											{#if product.priceRange.start.amount === product.priceRange.stop.amount}
												GHS {product.priceRange.start.amount}
											{:else}
												GHS {product.priceRange.start.amount} - {product.priceRange.stop.amount}
											{/if}
										{:else}
											-
										{/if}
									{:else}
										-
									{/if}
								</td>
								<td class="px-6 py-4 text-gray-800">
									{product.category?.title || '-'}
								</td>
								<td class="px-6 py-4">
									<span
										class="rounded-full px-2 py-1 text-xs {product.status === 'PUBLISHED'
											? 'bg-green-100 text-green-800'
											: product.status === 'DRAFT'
												? 'bg-yellow-100 text-yellow-800'
												: 'bg-red-100 text-red-800'}"
									>
										{product.status || 'DRAFT'}
									</span>
								</td>
								<td class="px-6 py-4 text-gray-800">
									{new Date(product.createdAt).toLocaleDateString()}
								</td>
								<td class="px-6 py-4">
									<div class="flex space-x-2">
										<button
											class="text-gray-400 hover:text-primary-600"
											aria-label="Edit product"
											onclick={() => goto(`/dashboard/products/${product.id}`)}
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
										<button class="text-gray-400 hover:text-red-600" aria-label="Delete product">
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
				{:else}
					<tr>
						<td colspan="6" class="px-6 py-8 text-center text-gray-500">
							{#if $Products.fetching}
								Loading products...
							{:else if $Products.errors?.length}
								Error loading products: {$Products.errors[0].message}
							{:else}
								No products found
							{/if}
						</td>
					</tr>
				{/if}
			</tbody>
		</table>

		<div class="flex items-center justify-between border-t px-6 py-4">
			<p class="text-sm text-gray-500">
				{#if $Products.data?.products.edges.length}
					Showing {$Products.data.products.edges.length} product{$Products.data.products.edges
						.length === 1
						? ''
						: 's'}
				{:else}
					No products to show
				{/if}
			</p>
			<div class="flex space-x-1">
				<button class="disabled rounded-md bg-gray-100 px-3 py-1 text-gray-600" disabled
					>Previous</button
				>
				<button class="rounded-md bg-primary-600 px-3 py-1 text-white">1</button>
				<button class="disabled rounded-md bg-gray-100 px-3 py-1 text-gray-600" disabled
					>Next</button
				>
			</div>
		</div>
	</div>
</DashboardLayout>
