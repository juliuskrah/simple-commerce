<!-- Customer Shopping Cart Page -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { useCart } from '$lib/stores/cart.svelte.js';
	
	// Cart state
	let loading = $state(true);
	let updating = $state(false);
	
	const cart = useCart();
	
	// Cart totals
	let taxRate = 0.08; // 8% tax rate
	let subtotal = $derived(cart.total);
	let tax = $derived(subtotal * taxRate);
	let shipping = $derived(subtotal > 50 ? 0 : 9.99); // Free shipping over $50
	let total = $derived(subtotal + tax + shipping);
	
	onMount(() => {
		// Cart is automatically initialized by the store
		loading = false;
	});
	
	function updateQuantity(itemId, newQuantity) {
		cart.updateQuantity(itemId, newQuantity);
	}
	
	function removeItem(itemId) {
		cart.removeFromCart(itemId);
	}
	
	function clearCart() {
		cart.clearCart();
	}
	
	async function proceedToCheckout() {
		if (cart.items.length === 0) return;
		
		updating = true;
		try {
			// TODO: Implement actual checkout process
			// This would typically create an order, process payment, etc.
			await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API call
			
			// For now, just redirect to a success page
			alert('Checkout functionality coming soon!');
		} catch (error) {
			alert('Checkout failed. Please try again.');
		} finally {
			updating = false;
		}
	}
	
	function formatPrice(price) {
		return new Intl.NumberFormat('en-US', {
			style: 'currency',
			currency: 'USD'
		}).format(price);
	}
	
	function getItemTotal(item) {
		return item.price * item.quantity;
	}
</script>

<svelte:head>
	<title>Shopping Cart - Simple Commerce</title>
	<meta name="description" content="Review your shopping cart and proceed to checkout." />
</svelte:head>

<div class="cart-page">
	<div class="page-header">
		<h1>Shopping Cart</h1>
		{#if !loading && cart.items.length > 0}
			<p class="items-count">{cart.items.length} item{cart.items.length !== 1 ? 's' : ''} in your cart</p>
		{/if}
	</div>
	
	{#if loading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading your cart...</p>
		</div>
	{:else if cart.items.length === 0}
		<div class="empty-cart">
			<div class="empty-cart-icon">
				<svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
					<circle cx="9" cy="21" r="1"></circle>
					<circle cx="20" cy="21" r="1"></circle>
					<path d="m1 1 4 4 16 1 2 13H6M6 10l11 1"></path>
				</svg>
			</div>
			<h2>Your cart is empty</h2>
			<p>Add some amazing products to get started!</p>
			<a href="/storefront/products" class="btn-primary">Continue Shopping</a>
		</div>
	{:else}
		<div class="cart-content">
			<!-- Cart Items -->
			<div class="cart-items">
				<div class="cart-header">
					<h2>Items in your cart</h2>
					<button onclick={clearCart} class="clear-cart-btn">Clear Cart</button>
				</div>
				
				<div class="items-list">
					{#each cart.items as item (item.id)}
						<div class="cart-item">
							<div class="item-image">
								<img src={item.image || '/placeholder-product.jpg'} alt={item.name} />
							</div>
							
							<div class="item-details">
								<h3>
									<a href="/storefront/products/{item.productId}">{item.name}</a>
								</h3>
								{#if item.variantName && item.variantName !== item.name}
									<p class="variant-name">{item.variantName}</p>
								{/if}
								{#if item.sku}
									<p class="item-sku">SKU: {item.sku}</p>
								{/if}
								<p class="item-price">{formatPrice(item.price)} each</p>
							</div>
							
							<div class="item-controls">
								<div class="quantity-controls">
									<button 
										onclick={() => updateQuantity(item.id, item.quantity - 1)}
										disabled={item.quantity <= 1}
										class="quantity-btn"
									>−</button>
									<input 
										type="number" 
										value={item.quantity}
										min="1" 
										max={item.maxStock}
										onchange={(e) => updateQuantity(item.id, parseInt(e.target.value))}
										class="quantity-input"
									/>
									<button 
										onclick={() => updateQuantity(item.id, item.quantity + 1)}
										disabled={item.quantity >= item.maxStock}
										class="quantity-btn"
									>+</button>
								</div>
								
								<div class="item-total">
									{formatPrice(getItemTotal(item))}
								</div>
								
								<button 
									onclick={() => removeItem(item.id)}
									class="remove-btn"
									title="Remove item"
								>
									<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
										<polyline points="3,6 5,6 21,6"></polyline>
										<path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"></path>
										<line x1="10" y1="11" x2="10" y2="17"></line>
										<line x1="14" y1="11" x2="14" y2="17"></line>
									</svg>
								</button>
							</div>
						</div>
					{/each}
				</div>
			</div>
			
			<!-- Cart Summary -->
			<div class="cart-summary">
				<h3>Order Summary</h3>
				
				<div class="summary-line">
					<span>Subtotal</span>
					<span>{formatPrice(subtotal)}</span>
				</div>
				
				<div class="summary-line">
					<span>Tax</span>
					<span>{formatPrice(tax)}</span>
				</div>
				
				<div class="summary-line">
					<span>Shipping</span>
					<span>
						{#if shipping === 0}
							<span class="free-shipping">FREE</span>
						{:else}
							{formatPrice(shipping)}
						{/if}
					</span>
				</div>
				
				{#if shipping > 0}
					<div class="shipping-notice">
						<p>💡 Add {formatPrice(50 - subtotal)} more for free shipping!</p>
					</div>
				{/if}
				
				<div class="summary-line total-line">
					<span>Total</span>
					<span class="total-amount">{formatPrice(total)}</span>
				</div>
				
				<div class="checkout-actions">
					<button 
						onclick={proceedToCheckout}
						disabled={updating || cart.items.length === 0}
						class="btn-checkout"
					>
						{#if updating}
							Processing...
						{:else}
							Continue to Checkout
						{/if}
					</button>
					
					<a href="/storefront/products" class="continue-shopping">
						← Continue Shopping
					</a>
				</div>
			</div>
		</div>
	{/if}
</div>

<style>
	.cart-page {
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
	
	.items-count {
		color: #6b7280;
		font-size: 1.125rem;
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
	
	.empty-cart {
		text-align: center;
		padding: 4rem 0;
	}
	
	.empty-cart-icon {
		color: #9ca3af;
		margin-bottom: 1rem;
	}
	
	.empty-cart h2 {
		font-size: 1.5rem;
		color: #6b7280;
		margin-bottom: 1rem;
	}
	
	.empty-cart p {
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
		transition: background-color 0.2s;
	}
	
	.btn-primary:hover {
		background-color: #2563eb;
	}
	
	.cart-content {
		display: grid;
		grid-template-columns: 2fr 1fr;
		gap: 3rem;
		align-items: start;
	}
	
	.cart-items {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		overflow: hidden;
	}
	
	.cart-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 1.5rem;
		border-bottom: 1px solid #e5e7eb;
	}
	
	.cart-header h2 {
		font-size: 1.25rem;
		font-weight: 600;
		color: #1f2937;
		margin: 0;
	}
	
	.clear-cart-btn {
		background: none;
		border: 1px solid #d1d5db;
		color: #6b7280;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		cursor: pointer;
		font-size: 0.875rem;
	}
	
	.clear-cart-btn:hover {
		background-color: #f3f4f6;
		color: #1f2937;
	}
	
	.items-list {
		/* Add border between items */
	}
	
	.cart-item:not(:last-child) {
		border-bottom: 1px solid #e5e7eb;
	}
	
	.cart-item {
		display: flex;
		gap: 1rem;
		padding: 1.5rem;
		align-items: flex-start;
	}
	
	.item-image {
		width: 80px;
		height: 80px;
		flex-shrink: 0;
		border-radius: 0.5rem;
		overflow: hidden;
		background-color: #f3f4f6;
	}
	
	.item-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	
	.item-details {
		flex: 1;
		min-width: 0;
	}
	
	.item-details h3 {
		margin: 0 0 0.25rem 0;
		font-size: 1rem;
		font-weight: 600;
	}
	
	.item-details h3 a {
		color: #1f2937;
		text-decoration: none;
	}
	
	.item-details h3 a:hover {
		color: #3b82f6;
	}
	
	.variant-name {
		color: #6b7280;
		font-size: 0.875rem;
		margin: 0 0 0.25rem 0;
	}
	
	.item-sku {
		color: #9ca3af;
		font-size: 0.75rem;
		margin: 0 0 0.5rem 0;
	}
	
	.item-price {
		color: #059669;
		font-weight: 600;
		margin: 0;
	}
	
	.item-controls {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 0.75rem;
	}
	
	.quantity-controls {
		display: flex;
		align-items: center;
		border: 1px solid #d1d5db;
		border-radius: 0.375rem;
		overflow: hidden;
	}
	
	.quantity-btn {
		background: white;
		border: none;
		padding: 0.5rem 0.75rem;
		cursor: pointer;
		font-size: 1rem;
		font-weight: 600;
		color: #6b7280;
		transition: background-color 0.2s;
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
		outline: none;
	}
	
	.item-total {
		font-size: 1.125rem;
		font-weight: 700;
		color: #1f2937;
	}
	
	.remove-btn {
		background: none;
		border: none;
		color: #dc2626;
		cursor: pointer;
		padding: 0.25rem;
		border-radius: 0.25rem;
		transition: background-color 0.2s;
	}
	
	.remove-btn:hover {
		background-color: #fee2e2;
	}
	
	.cart-summary {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		padding: 1.5rem;
		position: sticky;
		top: 2rem;
	}
	
	.cart-summary h3 {
		font-size: 1.25rem;
		font-weight: 600;
		color: #1f2937;
		margin: 0 0 1rem 0;
	}
	
	.summary-line {
		display: flex;
		justify-content: space-between;
		margin-bottom: 0.75rem;
		font-size: 0.875rem;
	}
	
	.summary-line span:first-child {
		color: #6b7280;
	}
	
	.summary-line span:last-child {
		color: #1f2937;
		font-weight: 500;
	}
	
	.free-shipping {
		color: #059669;
		font-weight: 600;
	}
	
	.shipping-notice {
		background-color: #f0f9ff;
		border: 1px solid #bae6fd;
		border-radius: 0.375rem;
		padding: 0.75rem;
		margin: 0.75rem 0;
	}
	
	.shipping-notice p {
		margin: 0;
		font-size: 0.875rem;
		color: #0369a1;
	}
	
	.total-line {
		border-top: 1px solid #e5e7eb;
		padding-top: 0.75rem;
		margin-top: 0.75rem;
		font-size: 1rem;
		font-weight: 600;
	}
	
	.total-amount {
		font-size: 1.25rem;
		color: #059669;
	}
	
	.checkout-actions {
		margin-top: 2rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
	
	.btn-checkout {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 1rem 2rem;
		border-radius: 0.5rem;
		font-size: 1.125rem;
		font-weight: 600;
		cursor: pointer;
		transition: background-color 0.2s;
		width: 100%;
	}
	
	.btn-checkout:hover:not(:disabled) {
		background-color: #2563eb;
	}
	
	.btn-checkout:disabled {
		opacity: 0.7;
		cursor: not-allowed;
	}
	
	.continue-shopping {
		color: #6b7280;
		text-decoration: none;
		text-align: center;
		font-size: 0.875rem;
	}
	
	.continue-shopping:hover {
		color: #3b82f6;
	}
	
	@media (max-width: 768px) {
		.page-header h1 {
			font-size: 2rem;
		}
		
		.cart-content {
			grid-template-columns: 1fr;
			gap: 2rem;
		}
		
		.cart-item {
			flex-direction: column;
			gap: 1rem;
		}
		
		.item-controls {
			flex-direction: row;
			align-items: center;
			justify-content: space-between;
			width: 100%;
		}
		
		.cart-summary {
			position: static;
			order: -1;
		}
	}
</style>