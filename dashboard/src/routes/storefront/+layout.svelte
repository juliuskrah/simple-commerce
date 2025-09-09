<!-- Customer Storefront Layout -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { useCart } from '$lib/stores/cart.svelte.js';
	import { useAuth } from '$lib/stores/auth.svelte.js';
	
	let { children } = $props();
	
	const cart = useCart();
	const auth = useAuth();
	
	onMount(() => {
		// Auth store automatically initializes
	});
	
	
	async function handleLogout() {
		await auth.logout();
		window.location.href = '/storefront';
	}
</script>

<div class="storefront-layout">
	<!-- Storefront Header -->
	<header class="storefront-header">
		<div class="container">
			<nav class="nav-primary">
				<div class="brand">
					<a href="/storefront" class="brand-link">
						<h1>Simple Commerce</h1>
					</a>
				</div>
				
				<div class="nav-links">
					<a href="/storefront">
						Home
					</a>
					<a href="/storefront/products">
						Products
					</a>
					<a href="/storefront/categories">
						Categories
					</a>
				</div>
				
				<div class="nav-actions">
					{#if auth.isAuthenticated}
						<div class="user-menu">
							<span class="user-name">Hello, {auth.user?.firstName || auth.user?.name || 'Customer'}</span>
							<a href="/storefront/account">Account</a>
							<a href="/storefront/cart" class="cart-link">
								Cart
								{#if cart.count > 0}
									<span class="cart-badge">{cart.count}</span>
								{/if}
							</a>
							<button onclick={handleLogout} class="logout-btn">Logout</button>
						</div>
					{:else}
						<div class="auth-links">
							<a href="/storefront/cart" class="cart-link">
								Cart
								{#if cart.count > 0}
									<span class="cart-badge">{cart.count}</span>
								{/if}
							</a>
							<a href="/auth" class="login-link">Login</a>
							<a href="/auth?register=true" class="register-link">Register</a>
						</div>
					{/if}
				</div>
			</nav>
		</div>
	</header>
	
	<!-- Main Content -->
	<main class="storefront-main">
		<div class="container">
			{@render children()}
		</div>
	</main>
	
	<!-- Storefront Footer -->
	<footer class="storefront-footer">
		<div class="container">
			<div class="footer-content">
				<div class="footer-section">
					<h3>Customer Service</h3>
					<ul>
						<li><a href="/storefront/help">Help Center</a></li>
						<li><a href="/storefront/contact">Contact Us</a></li>
						<li><a href="/storefront/returns">Returns</a></li>
					</ul>
				</div>
				<div class="footer-section">
					<h3>About</h3>
					<ul>
						<li><a href="/storefront/about">About Us</a></li>
						<li><a href="/storefront/privacy">Privacy Policy</a></li>
						<li><a href="/storefront/terms">Terms of Service</a></li>
					</ul>
				</div>
			</div>
			<div class="footer-bottom">
				<p>&copy; 2024 Simple Commerce. All rights reserved.</p>
			</div>
		</div>
	</footer>
</div>

<style>
	.storefront-layout {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}
	
	.storefront-header {
		background: white;
		border-bottom: 1px solid #e5e7eb;
		position: sticky;
		top: 0;
		z-index: 100;
	}
	
	.container {
		max-width: 1200px;
		margin: 0 auto;
		padding: 0 1rem;
	}
	
	.nav-primary {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 1rem 0;
	}
	
	.brand-link {
		text-decoration: none;
		color: inherit;
	}
	
	.brand h1 {
		margin: 0;
		font-size: 1.5rem;
		color: #1f2937;
	}
	
	.nav-links {
		display: flex;
		gap: 2rem;
	}
	
	.nav-links a {
		text-decoration: none;
		color: #6b7280;
		font-weight: 500;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		transition: all 0.2s;
	}
	
	.nav-links a:hover,
	.nav-links a.active {
		color: #1f2937;
		background-color: #f3f4f6;
	}
	
	.nav-actions {
		display: flex;
		align-items: center;
		gap: 1rem;
	}
	
	.user-menu {
		display: flex;
		align-items: center;
		gap: 1rem;
	}
	
	.user-name {
		color: #6b7280;
		font-size: 0.875rem;
	}
	
	.auth-links {
		display: flex;
		gap: 1rem;
	}
	
	.login-link,
	.register-link {
		text-decoration: none;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		font-weight: 500;
		transition: all 0.2s;
	}
	
	.login-link {
		color: #6b7280;
	}
	
	.login-link:hover {
		color: #1f2937;
		background-color: #f3f4f6;
	}
	
	.register-link {
		background-color: #3b82f6;
		color: white;
	}
	
	.register-link:hover {
		background-color: #2563eb;
	}
	
	.logout-btn {
		background: none;
		border: 1px solid #d1d5db;
		color: #6b7280;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		cursor: pointer;
		font-size: 0.875rem;
	}
	
	.logout-btn:hover {
		background-color: #f3f4f6;
		color: #1f2937;
	}
	
	.storefront-main {
		flex: 1;
		padding: 2rem 0;
	}
	
	.storefront-footer {
		background-color: #f9fafb;
		border-top: 1px solid #e5e7eb;
		padding: 2rem 0 1rem;
	}
	
	.footer-content {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 2rem;
		margin-bottom: 2rem;
	}
	
	.footer-section h3 {
		font-size: 1rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 1rem;
	}
	
	.footer-section ul {
		list-style: none;
		padding: 0;
		margin: 0;
	}
	
	.footer-section li {
		margin-bottom: 0.5rem;
	}
	
	.footer-section a {
		color: #6b7280;
		text-decoration: none;
		font-size: 0.875rem;
	}
	
	.footer-section a:hover {
		color: #1f2937;
	}
	
	.footer-bottom {
		border-top: 1px solid #e5e7eb;
		padding-top: 1rem;
		text-align: center;
	}
	
	.footer-bottom p {
		color: #6b7280;
		font-size: 0.875rem;
		margin: 0;
	}
	
	.cart-link {
		position: relative;
		text-decoration: none;
		color: #6b7280;
		font-weight: 500;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		transition: all 0.2s;
	}
	
	.cart-link:hover {
		color: #1f2937;
		background-color: #f3f4f6;
	}
	
	.cart-badge {
		position: absolute;
		top: 0.25rem;
		right: 0.25rem;
		background-color: #ef4444;
		color: white;
		font-size: 0.75rem;
		font-weight: 600;
		padding: 0.125rem 0.375rem;
		border-radius: 9999px;
		min-width: 1.25rem;
		text-align: center;
		line-height: 1;
	}
	
	@media (max-width: 768px) {
		.nav-primary {
			flex-direction: column;
			gap: 1rem;
		}
		
		.nav-links {
			flex-wrap: wrap;
			justify-content: center;
		}
		
		.container {
			padding: 0 0.5rem;
		}
	}
</style>