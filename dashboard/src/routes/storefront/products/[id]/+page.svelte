<!-- Customer Product Detail Page -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { useCart } from '$lib/stores/cart.svelte.js';
	
	let product = $state(null);
	let selectedVariant = $state(null);
	let selectedImageIndex = $state(0);
	let quantity = $state(1);
	let loading = $state(true);
	let error = $state(null);
	let addingToCart = $state(false);
	
	const cart = useCart();
	
	$effect(() => {
		if ($page.params.id) {
			loadProduct($page.params.id);
		}
	});
	
	async function loadProduct(productId) {
		loading = true;
		error = null;
		try {
			const response = await fetch('/graphql', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					query: `
						query StorefrontProduct($id: ID!) {
							product(id: $id) {
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
											weight
											sku
										}
									}
								}
							}
						}
					`,
					variables: { id: productId }
				})
			});
			
			const data = await response.json();
			if (data.errors) {
				error = data.errors[0].message;
			} else if (data.data?.product) {
				product = data.data.product;
				// Select first variant by default
				if (product.variants?.edges?.length > 0) {
					selectedVariant = product.variants.edges[0].node;
				}
			} else {
				error = 'Product not found';
			}
		} catch (err) {
			error = 'Failed to load product';
			console.error('Error loading product:', err);
		} finally {
			loading = false;
		}
	}
	
	function selectVariant(variant) {
		selectedVariant = variant;
		quantity = 1; // Reset quantity when variant changes
	}
	
	function selectImage(index) {
		selectedImageIndex = index;
	}
	
	function incrementQuantity() {
		if (selectedVariant && quantity < selectedVariant.stock) {
			quantity++;
		}
	}
	
	function decrementQuantity() {
		if (quantity > 1) {
			quantity--;
		}
	}
	
	async function addToCart() {
		if (!selectedVariant || !product) return;
		
		addingToCart = true;
		try {
			// Add item to cart using the shared cart store
			const success = cart.addToCart(product, selectedVariant, quantity);
			
			if (success) {
				// Show success feedback
				const variantName = selectedVariant.name !== product.name ? ` (${selectedVariant.name})` : '';
				alert(`Added ${quantity} × ${product.name}${variantName} to cart!`);
			} else {
				alert('Failed to add item to cart. Please try again.');
			}
		} catch (err) {
			console.error('Error adding to cart:', err);
			alert('Failed to add item to cart. Please try again.');
		} finally {
			addingToCart = false;
		}
	}
	
	function formatPrice(priceSet) {
		if (!priceSet) return 'Price on request';
		return `${priceSet.currency} ${priceSet.amount}`;
	}
	
	function getProductImages() {
		if (product?.media?.edges?.length > 0) {
			return product.media.edges.map(edge => edge.node);
		}
		return [{ id: 'placeholder', url: '/placeholder-product.jpg', alt: product?.name || 'Product' }];
	}
	
	function isInStock() {
		return selectedVariant && selectedVariant.stock > 0;
	}
	
	function getStockStatus() {
		if (!selectedVariant) return 'Select a variant';
		if (selectedVariant.stock === 0) return 'Out of stock';
		if (selectedVariant.stock < 5) return `Only ${selectedVariant.stock} left!`;
		return 'In stock';
	}
</script>

<svelte:head>
	{#if product}
		<title>{product.name} - Simple Commerce</title>
		<meta name="description" content={product.description || `Buy ${product.name} at Simple Commerce`} />
	{:else}
		<title>Product - Simple Commerce</title>
	{/if}
</svelte:head>

<div class="product-detail">
	{#if loading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading product...</p>
		</div>
	{:else if error}
		<div class="error-state">
			<h1>Product Not Found</h1>
			<p>{error}</p>
			<a href="/storefront/products" class="btn-primary">Browse All Products</a>
		</div>
	{:else if product}
		<!-- Breadcrumb -->
		<nav class="breadcrumb">
			<a href="/storefront">Home</a>
			<span>›</span>
			<a href="/storefront/products">Products</a>
			<span>›</span>
			<span>{product.name}</span>
		</nav>
		
		<div class="product-content">
			<!-- Product Images -->
			<div class="product-images">
				<div class="main-image">
					<img 
						src={getProductImages()[selectedImageIndex]?.url} 
						alt={getProductImages()[selectedImageIndex]?.alt}
					/>
				</div>
				{#if getProductImages().length > 1}
					<div class="image-thumbnails">
						{#each getProductImages() as image, index}
							<button 
								class="thumbnail"
								class:active={index === selectedImageIndex}
								onclick={() => selectImage(index)}
							>
								<img src={image.url} alt={image.alt} />
							</button>
						{/each}
					</div>
				{/if}
			</div>
			
			<!-- Product Info -->
			<div class="product-info">
				<h1>{product.name}</h1>
				
				{#if product.description}
					<div class="product-description">
						<p>{product.description}</p>
					</div>
				{/if}
				
				<!-- Variant Selection -->
				{#if product.variants?.edges?.length > 1}
					<div class="variant-selection">
						<h3>Select Variant</h3>
						<div class="variant-options">
							{#each product.variants.edges as { node: variant }}
								<button 
									class="variant-option"
									class:selected={selectedVariant?.id === variant.id}
									onclick={() => selectVariant(variant)}
									disabled={variant.stock === 0}
								>
									<span class="variant-name">{variant.name}</span>
									<span class="variant-price">{formatPrice(variant.priceSet)}</span>
									{#if variant.stock === 0}
						                <span class="variant-stock out-of-stock">Out of Stock</span>
									{:else if variant.stock < 5}
									    <span class="variant-stock low-stock">Only {variant.stock} left</span>
									{/if}
								</button>
							{/each}
						</div>
					</div>
				{/if}
				
				{#if selectedVariant}
					<!-- Price and Stock -->
					<div class="price-section">
						<div class="price">{formatPrice(selectedVariant.priceSet)}</div>
						<div class="stock-status" class:out-of-stock={!isInStock()}>
							{getStockStatus()}
						</div>
					</div>
					
					<!-- Product Details -->
					<div class="product-details">
						{#if selectedVariant.sku}
							<div class="detail-item">
								<span class="detail-label">SKU:</span>
								<span class="detail-value">{selectedVariant.sku}</span>
							</div>
						{/if}
						{#if selectedVariant.weight}
							<div class="detail-item">
								<span class="detail-label">Weight:</span>
								<span class="detail-value">{selectedVariant.weight}g</span>
							</div>
						{/if}
					</div>
					
					<!-- Add to Cart -->
					{#if isInStock()}
						<div class="add-to-cart">
							<div class="quantity-selector">
								<label for="quantity">Quantity:</label>
								<div class="quantity-controls">
									<button 
										onclick={decrementQuantity}
										disabled={quantity <= 1}
										class="quantity-btn"
									>−</button>
									<input 
										id="quantity"
										type="number" 
										bind:value={quantity}
										min="1" 
										max={selectedVariant.stock}
										class="quantity-input"
									/>
									<button 
										onclick={incrementQuantity}
										disabled={quantity >= selectedVariant.stock}
										class="quantity-btn"
									>+</button>
								</div>
							</div>
							
							<button 
								onclick={addToCart}
								disabled={addingToCart}
								class="btn-add-to-cart"
							>
								{#if addingToCart}
									Adding...
								{:else}
									Add to Cart • {formatPrice(selectedVariant.priceSet)}
								{/if}
							</button>
						</div>
					{:else}
						<div class="out-of-stock-message">
							<p>This product is currently out of stock.</p>
							<button class="btn-notify-restock">Notify When Available</button>
						</div>
					{/if}
				{/if}
			</div>
		</div>
	{/if}
</div>

<style>
	.product-detail {
		max-width: 1200px;
		margin: 0 auto;
		padding: 0 1rem;
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
	
	.error-state {
		text-align: center;
		padding: 4rem 0;
	}
	
	.error-state h1 {
		font-size: 2rem;
		color: #6b7280;
		margin-bottom: 1rem;
	}
	
	.error-state p {
		color: #9ca3af;
		margin-bottom: 2rem;
	}
	
	.btn-primary {
		background-color: #3b82f6;
		color: white;
		text-decoration: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.5rem;
		font-weight: 600;
		display: inline-block;
	}
	
	.btn-primary:hover {
		background-color: #2563eb;
	}
	
	.breadcrumb {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 2rem;
		font-size: 0.875rem;
		color: #6b7280;
	}
	
	.breadcrumb a {
		color: #3b82f6;
		text-decoration: none;
	}
	
	.breadcrumb a:hover {
		text-decoration: underline;
	}
	
	.product-content {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 3rem;
		margin-bottom: 3rem;
	}
	
	.product-images {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
	
	.main-image {
		aspect-ratio: 1;
		overflow: hidden;
		border-radius: 0.75rem;
		background-color: #f3f4f6;
	}
	
	.main-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	
	.image-thumbnails {
		display: flex;
		gap: 0.75rem;
	}
	
	.thumbnail {
		width: 80px;
		height: 80px;
		border: 2px solid transparent;
		border-radius: 0.5rem;
		overflow: hidden;
		cursor: pointer;
		background: none;
		padding: 0;
	}
	
	.thumbnail.active {
		border-color: #3b82f6;
	}
	
	.thumbnail img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	
	.product-info {
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
	}
	
	.product-info h1 {
		font-size: 2rem;
		font-weight: 700;
		color: #1f2937;
		margin: 0;
	}
	
	.product-description p {
		color: #6b7280;
		line-height: 1.6;
		margin: 0;
	}
	
	.variant-selection h3 {
		font-size: 1rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 0.75rem;
	}
	
	.variant-options {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}
	
	.variant-option {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0.75rem;
		border: 1px solid #d1d5db;
		border-radius: 0.5rem;
		background: white;
		cursor: pointer;
		text-align: left;
	}
	
	.variant-option:hover:not(:disabled) {
		border-color: #3b82f6;
	}
	
	.variant-option.selected {
		border-color: #3b82f6;
		background-color: #eff6ff;
	}
	
	.variant-option:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}
	
	.variant-name {
		font-weight: 500;
		color: #1f2937;
	}
	
	.variant-price {
		font-weight: 600;
		color: #059669;
	}
	
	.variant-stock {
		font-size: 0.75rem;
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
	}
	
	.variant-stock.out-of-stock {
		background-color: #fee2e2;
		color: #dc2626;
	}
	
	.variant-stock.low-stock {
		background-color: #fef3c7;
		color: #d97706;
	}
	
	.price-section {
		display: flex;
		align-items: center;
		gap: 1rem;
		padding: 1rem 0;
		border-top: 1px solid #e5e7eb;
		border-bottom: 1px solid #e5e7eb;
	}
	
	.price {
		font-size: 1.5rem;
		font-weight: 700;
		color: #059669;
	}
	
	.stock-status {
		font-size: 0.875rem;
		font-weight: 500;
		color: #059669;
	}
	
	.stock-status.out-of-stock {
		color: #dc2626;
	}
	
	.product-details {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}
	
	.detail-item {
		display: flex;
		gap: 0.5rem;
	}
	
	.detail-label {
		font-weight: 500;
		color: #6b7280;
		min-width: 80px;
	}
	
	.detail-value {
		color: #1f2937;
	}
	
	.add-to-cart {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
	
	.quantity-selector label {
		font-weight: 500;
		color: #1f2937;
		margin-bottom: 0.5rem;
		display: block;
	}
	
	.quantity-controls {
		display: flex;
		align-items: center;
		border: 1px solid #d1d5db;
		border-radius: 0.5rem;
		overflow: hidden;
		width: fit-content;
	}
	
	.quantity-btn {
		background: white;
		border: none;
		padding: 0.5rem 0.75rem;
		cursor: pointer;
		font-size: 1.125rem;
		font-weight: 600;
		color: #6b7280;
	}
	
	.quantity-btn:hover:not(:disabled) {
		background-color: #f3f4f6;
		color: #1f2937;
	}
	
	.quantity-btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}
	
	.quantity-input {
		border: none;
		padding: 0.5rem;
		width: 60px;
		text-align: center;
		font-weight: 500;
	}
	
	.quantity-input:focus {
		outline: none;
	}
	
	.btn-add-to-cart {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 1rem 2rem;
		border-radius: 0.5rem;
		font-size: 1.125rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}
	
	.btn-add-to-cart:hover:not(:disabled) {
		background-color: #2563eb;
	}
	
	.btn-add-to-cart:disabled {
		opacity: 0.7;
		cursor: not-allowed;
	}
	
	.out-of-stock-message {
		text-align: center;
		padding: 2rem;
		background-color: #fef2f2;
		border: 1px solid #fecaca;
		border-radius: 0.5rem;
	}
	
	.out-of-stock-message p {
		color: #dc2626;
		font-weight: 500;
		margin-bottom: 1rem;
	}
	
	.btn-notify-restock {
		background-color: #dc2626;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.5rem;
		font-weight: 600;
		cursor: pointer;
	}
	
	.btn-notify-restock:hover {
		background-color: #b91c1c;
	}
	
	@media (max-width: 768px) {
		.product-content {
			grid-template-columns: 1fr;
			gap: 2rem;
		}
		
		.product-info h1 {
			font-size: 1.5rem;
		}
		
		.breadcrumb {
			font-size: 0.75rem;
		}
		
		.add-to-cart {
			position: sticky;
			bottom: 0;
			background: white;
			padding: 1rem;
			border-top: 1px solid #e5e7eb;
			margin: 0 -1rem;
		}
	}
</style>