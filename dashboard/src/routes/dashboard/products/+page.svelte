<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import PriceRange from '$lib/components/PriceRange.svelte';
	import type { PageData } from './$houdini';
	import { goto } from '$app/navigation';

	interface Props {
		data: PageData;
	}

	/** @type { import('undefined').Props } */
	let { data }: Props = $props();
	const user = $derived(data.user);
	let { Products } = $derived(data);
	
	// Categories come from the same query as products
	const categories = $derived($Products.data?.categories);

	// Search and filter state
	let searchQuery = $state('');
	let selectedCategory = $state('All Categories');
	let selectedStatus = $state('All Status');
	let selectedSort = $state('title-asc');
	let priceMin = $state('');
	let priceMax = $state('');
	
	// Pagination state
	let currentCursor = $state(null);
	let isForwardPaging = $state(true);

	// Debounced search function
	let searchTimeout: NodeJS.Timeout;
	function handleSearch() {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			updateQuery();
		}, 300);
	}

	// Update GraphQL query based on filters
	function updateQuery(resetPagination = true) {
		let queryParts: string[] = [];
		
		// Add text search if provided
		if (searchQuery.trim()) {
			queryParts.push(`"${searchQuery.trim()}"`);
		}
		
		// Add status filter if not "All Status"
		if (selectedStatus !== 'All Status') {
			const statusMapping: Record<string, string> = {
				'Active': 'PUBLISHED',
				'Low stock': 'DRAFT',
				'Out of stock': 'ARCHIVED'
			};
			const mappedStatus = statusMapping[selectedStatus] || selectedStatus.toUpperCase();
			queryParts.push(`status:${mappedStatus}`);
		}
		
		// Add category filter if not "All Categories"
		if (selectedCategory !== 'All Categories') {
			queryParts.push(`category:${selectedCategory.toLowerCase()}`);
		}
		
		// Add price range filter if provided
		if (String(priceMin).trim() || String(priceMax).trim()) {
			const min = String(priceMin).trim() || '0';
			const max = String(priceMax).trim() || '*';
			queryParts.push(`price:${min}..${max}`);
		}
		
		const searchString = queryParts.join(' AND ');
		
		// Parse sorting option
		const [sortField, sortDirection] = selectedSort.split('-');
		const orderBy = sortField ? [sortField] : undefined;
		const orderDirection = sortDirection?.toUpperCase() || 'ASC';
		
		// Reset pagination when filters change
		if (resetPagination) {
			currentCursor = null;
			isForwardPaging = true;
		}
		
		// Build variables for GraphQL query
		const variables: any = {
			query: searchString || undefined,
			orderBy,
			orderDirection,
			first: isForwardPaging ? 10 : undefined,
			last: !isForwardPaging ? 10 : undefined,
			after: isForwardPaging ? currentCursor : undefined,
			before: !isForwardPaging ? currentCursor : undefined
		};
		
		// Update the GraphQL query
		Products.fetch({ variables });
	}

	// Handle filter changes
	function handleCategoryChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		selectedCategory = target.value;
		updateQuery();
	}

	function handleStatusChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		selectedStatus = target.value;
		updateQuery();
	}

	function handleSortChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		selectedSort = target.value;
		updateQuery();
	}

	function handlePriceFilter() {
		updateQuery();
	}

	// Clear individual filters
	function clearCategoryFilter() {
		selectedCategory = 'All Categories';
		updateQuery();
	}

	function clearStatusFilter() {
		selectedStatus = 'All Status';
		updateQuery();
	}

	function clearSearchQuery() {
		searchQuery = '';
		updateQuery();
	}

	function clearPriceFilter() {
		priceMin = '';
		priceMax = '';
		updateQuery();
	}

	function clearSortFilter() {
		selectedSort = 'title-asc';
		updateQuery();
	}

	function clearAllFilters() {
		searchQuery = '';
		selectedCategory = 'All Categories';
		selectedStatus = 'All Status';
		selectedSort = 'title-asc';
		priceMin = '';
		priceMax = '';
		updateQuery();
	}

	// Pagination functions
	function handleNextPage() {
		if ($Products.data?.products.pageInfo.hasNextPage) {
			currentCursor = $Products.data.products.pageInfo.endCursor;
			isForwardPaging = true;
			updateQuery(false);
		}
	}

	function handlePreviousPage() {
		if ($Products.data?.products.pageInfo.hasPreviousPage) {
			currentCursor = $Products.data.products.pageInfo.startCursor;
			isForwardPaging = false;
			updateQuery(false);
		}
	}

	// Get active filters for display
	const activeFilters = $derived([
		...(searchQuery.trim() ? [{ type: 'search', label: `"${searchQuery.trim()}"`, clear: clearSearchQuery }] : []),
		...(selectedCategory !== 'All Categories' ? [{ type: 'category', label: `Category: ${selectedCategory}`, clear: clearCategoryFilter }] : []),
		...(selectedStatus !== 'All Status' ? [{ type: 'status', label: `Status: ${selectedStatus}`, clear: clearStatusFilter }] : []),
		...(selectedSort !== 'title-asc' ? [{ type: 'sort', label: `Sort: ${selectedSort.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase())}`, clear: clearSortFilter }] : []),
		...((String(priceMin).trim() || String(priceMax).trim()) ? [{ type: 'price', label: `Price: ${priceMin || '0'} - ${priceMax || '∞'}`, clear: clearPriceFilter }] : [])
	]);
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
		<div class="border-b p-4">
			<div class="flex items-center justify-between">
				<div class="relative">
					<input
						type="text"
						placeholder="Search products..."
						bind:value={searchQuery}
						oninput={handleSearch}
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
						bind:value={selectedCategory}
						onchange={handleCategoryChange}
						class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
					>
						<option>All Categories</option>
						{#if categories?.edges}
							{#each categories.edges as edge}
								{#if edge?.node}
									<option value={edge.node.title}>{edge.node.title}</option>
								{/if}
							{/each}
						{/if}
					</select>
					<select
						bind:value={selectedStatus}
						onchange={handleStatusChange}
						class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
					>
						<option>All Status</option>
						<option>Active</option>
						<option>Low stock</option>
						<option>Out of stock</option>
					</select>
					<select
						bind:value={selectedSort}
						onchange={handleSortChange}
						class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
					>
						<option value="title-asc">Name A-Z</option>
						<option value="title-desc">Name Z-A</option>
						<option value="createdAt-desc">Newest First</option>
						<option value="createdAt-asc">Oldest First</option>
						<option value="updatedAt-desc">Recently Updated</option>
					</select>
				</div>
			</div>
			
			<!-- Price Range Filter -->
			<div class="mt-4 flex items-center gap-2">
				<span class="text-sm font-medium text-gray-600">Price Range:</span>
				<input
					type="number"
					placeholder="Min"
					bind:value={priceMin}
					onchange={handlePriceFilter}
					class="w-20 rounded-lg border px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
				/>
				<span class="text-gray-400">-</span>
				<input
					type="number"
					placeholder="Max"
					bind:value={priceMax}
					onchange={handlePriceFilter}
					class="w-20 rounded-lg border px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
				/>
				<button
					onclick={handlePriceFilter}
					class="rounded-lg bg-primary-600 px-3 py-1 text-sm text-white hover:bg-primary-700"
				>
					Apply
				</button>
			</div>
			
			<!-- Active Filters Display -->
			{#if activeFilters.length > 0}
				<div class="mt-3 flex flex-wrap items-center gap-2">
					<span class="text-sm font-medium text-gray-600">Active filters:</span>
					{#each activeFilters as filter (filter.type)}
						<span class="inline-flex items-center gap-1 rounded-full bg-blue-100 px-3 py-1 text-sm text-blue-800">
							{filter.label}
							<button
								type="button"
								onclick={filter.clear}
								class="ml-1 inline-flex h-4 w-4 items-center justify-center rounded-full hover:bg-blue-200"
								aria-label="Remove {filter.label} filter"
							>
								<svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
									<path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"></path>
								</svg>
							</button>
						</span>
					{/each}
					{#if activeFilters.length > 1}
						<button
							type="button"
							onclick={clearAllFilters}
							class="text-sm text-blue-600 hover:text-blue-800 underline"
						>
							Clear all
						</button>
					{/if}
				</div>
			{/if}
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
								<td class="px-6 py-4">
									<PriceRange priceRange={product.priceRange} />
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
					{#if searchQuery.trim() || selectedCategory !== 'All Categories' || selectedStatus !== 'All Status'}
						<span class="ml-2 text-blue-600">
							(filtered)
						</span>
					{/if}
				{:else if $Products.fetching}
					Loading products...
				{:else if searchQuery.trim() || selectedCategory !== 'All Categories' || selectedStatus !== 'All Status'}
					No products match your search criteria
				{:else}
					No products to show
				{/if}
			</p>
			<div class="flex space-x-1">
				<button 
					class="rounded-md px-3 py-1 {$Products.data?.products.pageInfo.hasPreviousPage 
						? 'bg-primary-600 text-white hover:bg-primary-700' 
						: 'bg-gray-100 text-gray-600 cursor-not-allowed'}"
					disabled={!$Products.data?.products.pageInfo.hasPreviousPage}
					onclick={handlePreviousPage}
				>
					Previous
				</button>
				<span class="flex items-center px-3 py-1 text-gray-600 bg-gray-50 rounded-md">
					Page {currentCursor ? '...' : '1'}
				</span>
				<button 
					class="rounded-md px-3 py-1 {$Products.data?.products.pageInfo.hasNextPage 
						? 'bg-primary-600 text-white hover:bg-primary-700' 
						: 'bg-gray-100 text-gray-600 cursor-not-allowed'}"
					disabled={!$Products.data?.products.pageInfo.hasNextPage}
					onclick={handleNextPage}
				>
					Next
				</button>
			</div>
		</div>
	</div>
</DashboardLayout>