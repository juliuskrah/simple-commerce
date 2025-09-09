<!-- Customer Products Listing Page -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { useCart } from '$lib/stores/cart.svelte.js';
	
	let products = $state([]);
	let categories = $state([]);
	let loading = $state(true);
	let searchQuery = $state('');
	let selectedCategory = $state('');
	let sortBy = $state('name');
	let currentPage = $state(0);
	let hasNextPage = $state(false);
	let hasPreviousPage = $state(false);
	let addingToCart = $state({});
	
	const cart = useCart();
	
	onMount(() => {
		loadProducts();
		loadCategories();
		
		// Handle URL search params
		const urlParams = new URLSearchParams($page.url.search);
		searchQuery = urlParams.get('search') || '';
		selectedCategory = urlParams.get('category') || '';
		sortBy = urlParams.get('sort') || 'name';
	});
	
	async function loadProducts() {
		loading = true;
		try {
			const response = await fetch('/graphql', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					query: `
						query StorefrontProducts($first: Int, $after: String, $query: String, $categoryId: String) {
							products(first: $first, after: $after, query: $query, categoryId: $categoryId) {
								edges {
									node {
										id
										name
										description
										status
										media {
											edges {
												node {
													id
													alt
													url
												}
											}
										}
										variants {
											edges {
												node {
													id
													name
													priceSet {
														amount
														currency
													}
													stock
												}
											}
										}
									}
									cursor
								}
								pageInfo {
									hasNextPage
									hasPreviousPage
									startCursor
									endCursor
								}
							}
						}
					`,
					variables: {
						first: 12,
						after: currentPage > 0 ? btoa(`cursor:${currentPage * 12}`) : null,
						query: searchQuery || null,
						categoryId: selectedCategory || null
					}
				})
			});
			
			const data = await response.json();
			if (data.data) {
				products = data.data.products.edges.map(edge => edge.node).filter(product => 
					product.status === 'PUBLISHED' || product.status === 'ACTIVE'
				);
				hasNextPage = data.data.products.pageInfo.hasNextPage;
				hasPreviousPage = data.data.products.pageInfo.hasPreviousPage;
			}
		} catch (error) {
			console.error('Error loading products:', error);
		} finally {
			loading = false;
		}
	}
	
	async function loadCategories() {
		try {
			const response = await fetch('/graphql', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					query: `
						query StorefrontCategories {
							categories {
								edges {
									node {
										id
										name
										slug
									}
								}
							}
						}
					`
				})
			});
			
			const data = await response.json();
			if (data.data) {
				categories = data.data.categories.edges.map(edge => edge.node);
			}
		} catch (error) {
			console.error('Error loading categories:', error);
		}
	}
	
	function handleSearch(event) {
		event.preventDefault();
		updateFilters();
	}
	
	function updateFilters() {
		const params = new URLSearchParams();
		if (searchQuery) params.set('search', searchQuery);
		if (selectedCategory) params.set('category', selectedCategory);
		if (sortBy !== 'name') params.set('sort', sortBy);
		
		window.history.pushState({}, '', `${window.location.pathname}?${params}`);
		currentPage = 0;
		loadProducts();
	}
	
	function nextPage() {
		if (hasNextPage) {
			currentPage++;
			loadProducts();
		}
	}
	
	function previousPage() {
		if (hasPreviousPage && currentPage > 0) {
			currentPage--;
			loadProducts();
		}
	}
	
	function getProductPrice(product) {
		if (product.variants?.edges?.length > 0) {
			const variant = product.variants.edges[0].node;
			if (variant.priceSet) {
				return `${variant.priceSet.currency} ${variant.priceSet.amount}`;
			}
		}
		return 'Price on request';
	}
	
	function getProductImage(product) {
		if (product.media?.edges?.length > 0) {
			return product.media.edges[0].node.url;
		}
		return '/placeholder-product.jpg';
	}
	
	function isInStock(product) {
		if (product.variants?.edges?.length > 0) {
			return product.variants.edges.some(edge => edge.node.stock > 0);
		}
		return false;
	}
	
	function clearFilters() {
		searchQuery = '';
		selectedCategory = '';
		sortBy = 'name';
		window.history.pushState({}, '', window.location.pathname);
		currentPage = 0;
		loadProducts();
	}
	
	async function addToCart(product) {
		if (!isInStock(product)) return;
		
		// Get the first available variant
		const firstVariant = product.variants?.edges?.[0]?.node;
		if (!firstVariant) return;
		
		addingToCart[product.id] = true;
		addingToCart = { ...addingToCart }; // Trigger reactivity
		
		try {
			const success = cart.addToCart(product, firstVariant, 1);
			
			if (success) {
				// Show brief success feedback
				const variantName = firstVariant.name !== product.name ? ` (${firstVariant.name})` : '';
				// Could replace with a toast notification in a real app
				console.log(`Added ${product.name}${variantName} to cart!`);
			}
		} catch (err) {
			console.error('Error adding to cart:', err);
		} finally {
			delete addingToCart[product.id];
			addingToCart = { ...addingToCart }; // Trigger reactivity
		}
	}
</script>

<svelte:head>
	<title>Products - Simple Commerce</title>
	<meta name="description" content="Browse our complete collection of products. Find exactly what you're looking for with our advanced search and filtering options." />
</svelte:head>

<div class="products-page">
	<div class="page-header">
		<h1>All Products</h1>
		<p class="page-description">Discover our complete collection of amazing products</p>
	</div>
	
	<!-- Search and Filters -->
	<div class="filters-section">
		<form onsubmit={handleSearch} class="search-form">
			<div class="search-input">
				<input 
					type="text" 
					bind:value={searchQuery}
					placeholder="Search products..." 
				/>
				<button type="submit" class="search-btn">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="11" cy="11" r="8"></circle>
						<path d="21 21l-4.35-4.35"></path>
					</svg>
				</button>
			</div>
		</form>
		
		<div class="filter-controls">
			<select bind:value={selectedCategory} onchange={updateFilters}>
				<option value="">All Categories</option>
				{#each categories as category}
					<option value={category.id}>{category.name}</option>
				{/each}
			</select>
			
			<select bind:value={sortBy} onchange={updateFilters}>
				<option value="name">Sort by Name</option>
				<option value="price_asc">Price: Low to High</option>
				<option value="price_desc">Price: High to Low</option>
				<option value="newest">Newest First</option>
			</select>
			
			{#if searchQuery || selectedCategory || sortBy !== 'name'}
				<button onclick={clearFilters} class="clear-filters">
					Clear Filters
				</button>
			{/if}
		</div>
	</div>
	
	{#if loading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading products...</p>
		</div>
	{:else if products.length === 0}
		<div class="empty-state">
			<h2>No Products Found</h2>
			<p>
				{#if searchQuery || selectedCategory}
					Try adjusting your search criteria or filters.
				{:else}
					We're working to add more products. Check back soon!
				{/if}
			</p>
			{#if searchQuery || selectedCategory}
				<button onclick={clearFilters} class="btn-primary">View All Products</button>
			{/if}
		</div>
	{:else}
		<!-- Products Grid -->
		<div class="products-grid">
			{#each products as product}
				<div class="product-card">
					<a href="/storefront/products/{product.id}" class="product-link">
						<div class="product-image">
							<img src={getProductImage(product)} alt={product.name} />
							{#if !isInStock(product)}
								<div class="out-of-stock-badge">Out of Stock</div>
							{/if}
						</div>
						<div class="product-info">
							<h3>{product.name}</h3>
							{#if product.description}
								<p class="product-description">
									{product.description.length > 80 
										? product.description.substring(0, 80) + '...' 
										: product.description}
								</p>
							{/if}
							<div class="product-price">
								{getProductPrice(product)}
							</div>
						</div>
					</a>
					<div class="product-actions">
						<button 
							class="btn-add-cart" 
							disabled={!isInStock(product) || addingToCart[product.id]}
							onclick={() => addToCart(product)}
						>
							{addingToCart[product.id] ? 'Adding...' : isInStock(product) ? 'Add to Cart' : 'Out of Stock'}
						</button>
					</div>
				</div>
			{/each}
		</div>
		
		<!-- Pagination -->
		{#if hasPreviousPage || hasNextPage}
			<div class="pagination">
				<button 
					onclick={previousPage} 
					disabled={!hasPreviousPage}
					class="pagination-btn"
				>
					← Previous
				</button>
				<span class="page-info">Page {currentPage + 1}</span>
				<button 
					onclick={nextPage} 
					disabled={!hasNextPage}
					class="pagination-btn"
				>
					Next →
				</button>
			</div>
		{/if}
	{/if}
</div>

<style>
	.products-page {
		max-width: 1200px;
		margin: 0 auto;
		padding: 0 1rem;
	}
	
	.page-header {
		text-align: center;
		margin-bottom: 3rem;
	}
	
	.page-header h1 {
		font-size: 2.5rem;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 0.5rem;
	}
	
	.page-description {
		color: #6b7280;
		font-size: 1.125rem;
	}
	
	.filters-section {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		padding: 1.5rem;
		margin-bottom: 2rem;
		display: flex;
		gap: 1rem;
		flex-wrap: wrap;
		align-items: center;
	}
	
	.search-form {
		flex: 1;
		min-width: 300px;
	}
	
	.search-input {
		position: relative;
		display: flex;
	}
	
	.search-input input {
		flex: 1;
		padding: 0.75rem 1rem;
		border: 1px solid #d1d5db;
		border-radius: 0.5rem 0 0 0.5rem;
		font-size: 1rem;
		outline: none;
	}
	
	.search-input input:focus {
		border-color: #3b82f6;
		box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
	}
	
	.search-btn {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 0.75rem 1rem;
		border-radius: 0 0.5rem 0.5rem 0;
		cursor: pointer;
		display: flex;
		align-items: center;
	}
	
	.search-btn:hover {
		background-color: #2563eb;
	}
	
	.filter-controls {
		display: flex;
		gap: 1rem;
		flex-wrap: wrap;
	}
	
	.filter-controls select {
		padding: 0.75rem;
		border: 1px solid #d1d5db;
		border-radius: 0.5rem;
		background: white;
		font-size: 0.875rem;
	}
	
	.clear-filters {
		background: none;
		border: 1px solid #d1d5db;
		color: #6b7280;
		padding: 0.75rem 1rem;
		border-radius: 0.5rem;
		cursor: pointer;
		font-size: 0.875rem;
	}
	
	.clear-filters:hover {
		background-color: #f3f4f6;
		color: #1f2937;
	}
	
	.loading {
		text-align: center;
		padding: 4rem 0;
	}
	
	.loading-spinner {
		width: 40px;
		height: 40px;
		border: 4px solid #f3f4f6;
		border-top: 4px solid #3b82f6;
		border-radius: 50%;
		animation: spin 1s linear infinite;
		margin: 0 auto 1rem;
	}
	
	@keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	}
	
	.empty-state {
		text-align: center;
		padding: 4rem 0;
	}
	
	.empty-state h2 {
		font-size: 1.5rem;
		color: #6b7280;
		margin-bottom: 1rem;
	}
	
	.empty-state p {
		color: #9ca3af;
		margin-bottom: 2rem;
	}
	
	.btn-primary {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.5rem;
		font-weight: 600;
		cursor: pointer;
		text-decoration: none;
		display: inline-block;
	}
	
	.btn-primary:hover {
		background-color: #2563eb;
	}
	
	.products-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
		margin-bottom: 3rem;
	}
	
	.product-card {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		overflow: hidden;
		transition: all 0.2s;
	}
	
	.product-card:hover {
		transform: translateY(-2px);
		box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
	}
	
	.product-link {
		text-decoration: none;
		color: inherit;
		display: block;
	}
	
	.product-image {
		aspect-ratio: 1;
		overflow: hidden;
		background-color: #f3f4f6;
		position: relative;
	}
	
	.product-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	
	.out-of-stock-badge {
		position: absolute;
		top: 0.75rem;
		right: 0.75rem;
		background-color: #ef4444;
		color: white;
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
		font-size: 0.75rem;
		font-weight: 600;
	}
	
	.product-info {
		padding: 1.25rem;
	}
	
	.product-info h3 {
		font-size: 1.125rem;
		font-weight: 600;
		margin-bottom: 0.5rem;
		color: #1f2937;
	}
	
	.product-description {
		color: #6b7280;
		font-size: 0.875rem;
		margin-bottom: 1rem;
		line-height: 1.4;
	}
	
	.product-price {
		font-size: 1.125rem;
		font-weight: 700;
		color: #059669;
	}
	
	.product-actions {
		padding: 0 1.25rem 1.25rem;
	}
	
	.btn-add-cart {
		width: 100%;
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 0.75rem;
		border-radius: 0.5rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}
	
	.btn-add-cart:hover:not(:disabled) {
		background-color: #2563eb;
	}
	
	.btn-add-cart:disabled {
		background-color: #9ca3af;
		cursor: not-allowed;
	}
	
	.pagination {
		display: flex;
		justify-content: center;
		align-items: center;
		gap: 1rem;
		margin-top: 2rem;
	}
	
	.pagination-btn {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.5rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}
	
	.pagination-btn:hover:not(:disabled) {
		background-color: #2563eb;
	}
	
	.pagination-btn:disabled {
		background-color: #9ca3af;
		cursor: not-allowed;
	}
	
	.page-info {
		color: #6b7280;
		font-weight: 500;
	}
	
	@media (max-width: 768px) {
		.page-header h1 {
			font-size: 2rem;
		}
		
		.filters-section {
			flex-direction: column;
			align-items: stretch;
		}
		
		.search-form {
			min-width: unset;
		}
		
		.filter-controls {
			justify-content: stretch;
		}
		
		.filter-controls select,
		.clear-filters {
			flex: 1;
		}
		
		.pagination {
			flex-direction: column;
		}
	}
</style>