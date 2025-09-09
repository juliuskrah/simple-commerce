<!-- Customer Storefront Homepage -->
<script lang="ts">
	import { onMount } from 'svelte';
	
	let featuredProducts = $state([]);
	let categories = $state([]);
	let loading = $state(true);
	
	onMount(() => {
		loadStorefrontData();
	});
	
	async function loadStorefrontData() {
		try {
			// Load featured products and categories
			const [productsResponse, categoriesResponse] = await Promise.all([
				fetch('/graphql', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						query: `
							query FeaturedProducts {
								products(first: 8) {
									edges {
										node {
											id
											name
											description
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
													}
												}
											}
										}
									}
								}
							}
						`
					})
				}),
				fetch('/graphql', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						query: `
							query FeaturedCategories {
								categories {
									edges {
										node {
											id
											name
											description
											slug
										}
									}
								}
							}
						`
					})
				})
			]);
			
			const productsData = await productsResponse.json();
			const categoriesData = await categoriesResponse.json();
			
			if (productsData.data) {
				featuredProducts = productsData.data.products.edges.map(edge => edge.node);
			}
			
			if (categoriesData.data) {
				categories = categoriesData.data.categories.edges.map(edge => edge.node);
			}
			
		} catch (error) {
			console.error('Error loading storefront data:', error);
		} finally {
			loading = false;
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
</script>

<svelte:head>
	<title>Simple Commerce - Your Online Store</title>
	<meta name="description" content="Discover amazing products at Simple Commerce. Shop the latest items with great prices and fast delivery." />
</svelte:head>

<div class="storefront-home">
	<!-- Hero Section -->
	<section class="hero">
		<div class="hero-content">
			<h1>Welcome to Simple Commerce</h1>
			<p>Discover amazing products with unbeatable prices and exceptional quality</p>
			<div class="hero-actions">
				<a href="/storefront/products" class="btn-primary">Shop Now</a>
				<a href="/storefront/categories" class="btn-secondary">Browse Categories</a>
			</div>
		</div>
	</section>
	
	{#if loading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading products...</p>
		</div>
	{:else}
		<!-- Featured Categories -->
		{#if categories.length > 0}
			<section class="categories-section">
				<h2>Shop by Category</h2>
				<div class="categories-grid">
					{#each categories.slice(0, 6) as category}
						<a href="/storefront/categories/{category.slug || category.id}" class="category-card">
							<div class="category-icon">
								<span>{category.name.charAt(0)}</span>
							</div>
							<h3>{category.name}</h3>
							{#if category.description}
								<p>{category.description}</p>
							{/if}
						</a>
					{/each}
				</div>
			</section>
		{/if}
		
		<!-- Featured Products -->
		{#if featuredProducts.length > 0}
			<section class="products-section">
				<div class="section-header">
					<h2>Featured Products</h2>
					<a href="/storefront/products" class="view-all">View All Products →</a>
				</div>
				<div class="products-grid">
					{#each featuredProducts as product}
						<div class="product-card">
							<a href="/storefront/products/{product.id}" class="product-link">
								<div class="product-image">
									<img src={getProductImage(product)} alt={product.name} />
								</div>
								<div class="product-info">
									<h3>{product.name}</h3>
									{#if product.description}
										<p class="product-description">
											{product.description.length > 100 
												? product.description.substring(0, 100) + '...' 
												: product.description}
										</p>
									{/if}
									<div class="product-price">
										{getProductPrice(product)}
									</div>
								</div>
							</a>
							<div class="product-actions">
								<button class="btn-add-cart">Add to Cart</button>
							</div>
						</div>
					{/each}
				</div>
			</section>
		{:else}
			<section class="empty-state">
				<h2>No Products Available</h2>
				<p>We're working hard to bring you amazing products. Check back soon!</p>
			</section>
		{/if}
	{/if}
	
	<!-- Newsletter Section -->
	<section class="newsletter">
		<div class="newsletter-content">
			<h2>Stay Updated</h2>
			<p>Subscribe to our newsletter for the latest products and exclusive offers</p>
			<form class="newsletter-form">
				<input type="email" placeholder="Enter your email" required />
				<button type="submit" class="btn-primary">Subscribe</button>
			</form>
		</div>
	</section>
</div>

<style>
	.storefront-home {
		min-height: 100vh;
	}
	
	.hero {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: white;
		padding: 4rem 0;
		text-align: center;
	}
	
	.hero-content h1 {
		font-size: 3rem;
		font-weight: 700;
		margin-bottom: 1rem;
	}
	
	.hero-content p {
		font-size: 1.25rem;
		margin-bottom: 2rem;
		opacity: 0.9;
	}
	
	.hero-actions {
		display: flex;
		gap: 1rem;
		justify-content: center;
		flex-wrap: wrap;
	}
	
	.btn-primary, .btn-secondary {
		padding: 0.875rem 2rem;
		border-radius: 0.5rem;
		text-decoration: none;
		font-weight: 600;
		transition: all 0.2s;
		display: inline-block;
	}
	
	.btn-primary {
		background-color: #3b82f6;
		color: white;
		border: 2px solid #3b82f6;
	}
	
	.btn-primary:hover {
		background-color: #2563eb;
		border-color: #2563eb;
	}
	
	.btn-secondary {
		background-color: transparent;
		color: white;
		border: 2px solid white;
	}
	
	.btn-secondary:hover {
		background-color: white;
		color: #667eea;
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
	
	.categories-section, .products-section {
		padding: 3rem 0;
	}
	
	.categories-section h2, .products-section h2 {
		text-align: center;
		font-size: 2rem;
		font-weight: 700;
		margin-bottom: 2rem;
		color: #1f2937;
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
	}
	
	.view-all {
		color: #3b82f6;
		text-decoration: none;
		font-weight: 600;
	}
	
	.view-all:hover {
		color: #2563eb;
	}
	
	.categories-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1.5rem;
	}
	
	.category-card {
		text-decoration: none;
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		padding: 2rem;
		text-align: center;
		transition: all 0.2s;
		color: inherit;
	}
	
	.category-card:hover {
		transform: translateY(-2px);
		box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
	}
	
	.category-icon {
		width: 60px;
		height: 60px;
		border-radius: 50%;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: white;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 1.5rem;
		font-weight: 700;
		margin: 0 auto 1rem;
	}
	
	.category-card h3 {
		font-size: 1.25rem;
		font-weight: 600;
		margin-bottom: 0.5rem;
		color: #1f2937;
	}
	
	.category-card p {
		color: #6b7280;
		font-size: 0.875rem;
		margin: 0;
	}
	
	.products-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
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
	}
	
	.product-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
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
	
	.btn-add-cart:hover {
		background-color: #2563eb;
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
	}
	
	.newsletter {
		background-color: #f9fafb;
		padding: 3rem 0;
		text-align: center;
	}
	
	.newsletter-content h2 {
		font-size: 1.875rem;
		font-weight: 700;
		margin-bottom: 0.5rem;
		color: #1f2937;
	}
	
	.newsletter-content p {
		color: #6b7280;
		margin-bottom: 2rem;
	}
	
	.newsletter-form {
		display: flex;
		gap: 1rem;
		justify-content: center;
		max-width: 400px;
		margin: 0 auto;
	}
	
	.newsletter-form input {
		flex: 1;
		padding: 0.875rem;
		border: 1px solid #d1d5db;
		border-radius: 0.5rem;
		font-size: 1rem;
	}
	
	.newsletter-form input:focus {
		outline: none;
		border-color: #3b82f6;
		box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
	}
	
	@media (max-width: 768px) {
		.hero-content h1 {
			font-size: 2rem;
		}
		
		.hero-actions {
			flex-direction: column;
			align-items: center;
		}
		
		.section-header {
			flex-direction: column;
			gap: 1rem;
			text-align: center;
		}
		
		.newsletter-form {
			flex-direction: column;
		}
	}
</style>