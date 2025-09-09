<!-- Customer Account Management Page -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { useAuth } from '$lib/stores/auth.svelte.js';
	
	const auth = useAuth();
	
	let orders = $state([]);
	let activeTab = $state('profile');
	let editingProfile = $state(false);
	let profileForm = $state({
		firstName: '',
		lastName: '',
		email: '',
		phone: '',
		dateOfBirth: ''
	});
	
	onMount(() => {
		// Wait for auth to initialize, then setup profile form
		if (auth.user) {
			setupProfileForm();
		}
		loadOrderHistory();
	});
	
	function setupProfileForm() {
		if (auth.user) {
			profileForm = {
				firstName: auth.user.firstName || '',
				lastName: auth.user.lastName || '',
				email: auth.user.email || '',
				phone: auth.user.phone || '',
				dateOfBirth: auth.user.dateOfBirth || ''
			};
		}
	}
	
	async function loadOrderHistory() {
		try {
			const response = await fetch('/api/orders/history');
			if (response.ok) {
				orders = await response.json();
			}
		} catch (err) {
			console.error('Failed to load order history:', err);
		}
	}
	
	async function updateProfile(event) {
		event.preventDefault();
		
		const result = await auth.updateProfile(profileForm);
		
		if (result.success) {
			editingProfile = false;
			alert('Profile updated successfully!');
		} else {
			alert(result.error || 'Failed to update profile');
		}
	}
	
	function cancelEdit() {
		editingProfile = false;
		setupProfileForm();
	}
	
	function formatDate(dateString) {
		if (!dateString) return 'N/A';
		return new Date(dateString).toLocaleDateString();
	}
	
	function formatPrice(amount, currency = 'USD') {
		return new Intl.NumberFormat('en-US', {
			style: 'currency',
			currency: currency
		}).format(amount);
	}
	
	function getOrderStatusClass(status) {
		switch (status?.toLowerCase()) {
			case 'completed': return 'status-completed';
			case 'pending': return 'status-pending';
			case 'processing': return 'status-processing';
			case 'cancelled': return 'status-cancelled';
			default: return 'status-unknown';
		}
	}
</script>

<svelte:head>
	<title>My Account - Simple Commerce</title>
	<meta name="description" content="Manage your account, view order history, and update your profile" />
</svelte:head>

<div class="account-page">
	<div class="page-header">
		<h1>My Account</h1>
		{#if auth.user}
			<p class="welcome-message">Welcome back, {auth.user.firstName || auth.user.name || 'Customer'}!</p>
		{/if}
	</div>
	
	{#if auth.isLoading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading your account...</p>
		</div>
	{:else if !auth.isAuthenticated}
		<div class="error-state">
			<div class="error-icon">
				<svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
					<circle cx="12" cy="12" r="10"></circle>
					<line x1="12" y1="8" x2="12" y2="12"></line>
					<line x1="12" y1="16" x2="12.01" y2="16"></line>
				</svg>
			</div>
			<h2>Account Access Required</h2>
			<p>Please log in to view your account</p>
			<a href="/auth" class="btn-login">Login to Your Account</a>
		</div>
	{:else}
		<div class="account-content">
			<nav class="account-nav">
				<button 
					class="nav-tab" 
					class:active={activeTab === 'profile'}
					onclick={() => activeTab = 'profile'}
				>
					Profile
				</button>
				<button 
					class="nav-tab" 
					class:active={activeTab === 'orders'}
					onclick={() => activeTab = 'orders'}
				>
					Order History
				</button>
				<button 
					class="nav-tab" 
					class:active={activeTab === 'settings'}
					onclick={() => activeTab = 'settings'}
				>
					Settings
				</button>
			</nav>
			
			<div class="tab-content">
				{#if activeTab === 'profile'}
					<div class="profile-section">
						<div class="section-header">
							<h2>Profile Information</h2>
							{#if !editingProfile}
								<button onclick={() => editingProfile = true} class="btn-edit">
									Edit Profile
				 				</button>
							{/if}
						</div>
						
						{#if editingProfile}
							<form onsubmit={updateProfile} class="profile-form">
								<div class="form-row">
									<div class="form-group">
										<label for="firstName">First Name</label>
										<input 
											type="text" 
											id="firstName" 
											bind:value={profileForm.firstName}
											required 
										/>
									</div>
									<div class="form-group">
										<label for="lastName">Last Name</label>
										<input 
											type="text" 
											id="lastName" 
											bind:value={profileForm.lastName}
											required 
										/>
									</div>
								</div>
								
								<div class="form-group">
									<label for="email">Email Address</label>
									<input 
										type="email" 
										id="email" 
										bind:value={profileForm.email}
										required 
									/>
								</div>
								
								<div class="form-row">
									<div class="form-group">
										<label for="phone">Phone Number</label>
										<input 
											type="tel" 
											id="phone" 
											bind:value={profileForm.phone}
										/>
									</div>
									<div class="form-group">
										<label for="dateOfBirth">Date of Birth</label>
										<input 
											type="date" 
											id="dateOfBirth" 
											bind:value={profileForm.dateOfBirth}
										/>
									</div>
								</div>
								
								<div class="form-actions">
									<button type="submit" class="btn-save">Save Changes</button>
									<button type="button" onclick={cancelEdit} class="btn-cancel">Cancel</button>
								</div>
							</form>
						{:else}
							<div class="profile-display">
								<div class="profile-info">
									<div class="info-item">
										<span class="label">Name:</span>
										<span class="value">{auth.user?.firstName || ''} {auth.user?.lastName || ''}</span>
									</div>
									<div class="info-item">
										<span class="label">Email:</span>
										<span class="value">{auth.user?.email || 'Not provided'}</span>
									</div>
									<div class="info-item">
										<span class="label">Phone:</span>
										<span class="value">{auth.user?.phone || 'Not provided'}</span>
									</div>
									<div class="info-item">
										<span class="label">Date of Birth:</span>
										<span class="value">{formatDate(auth.user?.dateOfBirth)}</span>
									</div>
									<div class="info-item">
										<span class="label">Account Created:</span>
										<span class="value">{formatDate(auth.user?.createdAt)}</span>
									</div>
								</div>
							</div>
						{/if}
					</div>
				{:else if activeTab === 'orders'}
					<div class="orders-section">
						<h2>Order History</h2>
						{#if orders.length === 0}
							<div class="empty-orders">
								<div class="empty-icon">
									<svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
										<circle cx="9" cy="21" r="1"></circle>
										<circle cx="20" cy="21" r="1"></circle>
										<path d="m1 1 4 4 1 9 4-4h15"></path>
									</svg>
								</div>
								<h3>No Orders Yet</h3>
								<p>You haven't placed any orders yet. Start shopping to see your order history here!</p>
								<a href="/storefront/products" class="btn-shop">Start Shopping</a>
							</div>
						{:else}
							<div class="orders-list">
								{#each orders as order (order.id)}
									<div class="order-card">
										<div class="order-header">
											<div class="order-info">
												<h3>Order #{order.orderNumber || order.id}</h3>
												<p class="order-date">{formatDate(order.createdAt)}</p>
											</div>
											<div class="order-status">
												<span class="status-badge {getOrderStatusClass(order.status)}">
													{order.status || 'Unknown'}
												</span>
											</div>
										</div>
										
										<div class="order-details">
											<div class="order-items">
												{#if order.items && order.items.length > 0}
													{#each order.items.slice(0, 3) as item}
														<div class="order-item">
															<span class="item-name">{item.name}</span>
															<span class="item-quantity">× {item.quantity}</span>
														</div>
													{/each}
													{#if order.items.length > 3}
														<div class="more-items">
															+{order.items.length - 3} more items
														</div>
													{/if}
												{/if}
											</div>
											
											<div class="order-total">
												<span class="total-label">Total:</span>
												<span class="total-amount">{formatPrice(order.total, order.currency)}</span>
											</div>
										</div>
										
										<div class="order-actions">
											<button class="btn-view-order">View Details</button>
											{#if order.status === 'completed'}
												<button class="btn-reorder">Reorder</button>
											{/if}
										</div>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				{:else if activeTab === 'settings'}
					<div class="settings-section">
						<h2>Account Settings</h2>
						
						<div class="settings-group">
							<h3>Notifications</h3>
							<div class="setting-item">
								<label class="setting-label">
									<input type="checkbox" checked />
									Email notifications for order updates
								</label>
							</div>
							<div class="setting-item">
								<label class="setting-label">
									<input type="checkbox" checked />
									Marketing emails
								</label>
							</div>
							<div class="setting-item">
								<label class="setting-label">
									<input type="checkbox" />
									SMS notifications
								</label>
							</div>
						</div>
						
						<div class="settings-group">
							<h3>Privacy</h3>
							<div class="setting-item">
								<label class="setting-label">
									<input type="checkbox" checked />
									Save browsing history
								</label>
							</div>
							<div class="setting-item">
								<label class="setting-label">
									<input type="checkbox" />
									Share data for personalized recommendations
								</label>
							</div>
						</div>
						
						<div class="settings-group">
							<h3>Account Actions</h3>
							<button class="btn-danger">Delete Account</button>
							<p class="danger-text">This action cannot be undone.</p>
						</div>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</div>

<style>
	.account-page {
		max-width: 1200px;
		margin: 0 auto;
		padding: 2rem;
	}
	
	.page-header {
		margin-bottom: 2rem;
		text-align: center;
	}
	
	.page-header h1 {
		font-size: 2.5rem;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 0.5rem;
	}
	
	.welcome-message {
		font-size: 1.125rem;
		color: #6b7280;
	}
	
	.loading {
		text-align: center;
		padding: 3rem;
	}
	
	.loading-spinner {
		width: 48px;
		height: 48px;
		border: 3px solid #f3f4f6;
		border-top: 3px solid #3b82f6;
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
		padding: 3rem;
	}
	
	.error-icon {
		color: #ef4444;
		margin-bottom: 1rem;
	}
	
	.error-state h2 {
		font-size: 1.5rem;
		color: #1f2937;
		margin-bottom: 1rem;
	}
	
	.btn-login {
		display: inline-block;
		background-color: #3b82f6;
		color: white;
		padding: 0.75rem 1.5rem;
		text-decoration: none;
		border-radius: 0.375rem;
		font-weight: 500;
		margin-top: 1rem;
		transition: background-color 0.2s;
	}
	
	.btn-login:hover {
		background-color: #2563eb;
	}
	
	.account-content {
		display: grid;
		grid-template-columns: 250px 1fr;
		gap: 2rem;
	}
	
	.account-nav {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}
	
	.nav-tab {
		background: none;
		border: none;
		padding: 0.75rem 1rem;
		text-align: left;
		font-size: 1rem;
		font-weight: 500;
		color: #6b7280;
		border-radius: 0.375rem;
		cursor: pointer;
		transition: all 0.2s;
	}
	
	.nav-tab:hover {
		background-color: #f3f4f6;
		color: #1f2937;
	}
	
	.nav-tab.active {
		background-color: #3b82f6;
		color: white;
	}
	
	.tab-content {
		background-color: white;
		border-radius: 0.5rem;
		padding: 2rem;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;
	}
	
	.section-header h2 {
		font-size: 1.5rem;
		font-weight: 600;
		color: #1f2937;
	}
	
	.btn-edit {
		background-color: #3b82f6;
		color: white;
		border: none;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}
	
	.btn-edit:hover {
		background-color: #2563eb;
	}
	
	.profile-form {
		max-width: 600px;
	}
	
	.form-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
		margin-bottom: 1rem;
	}
	
	.form-group {
		margin-bottom: 1rem;
	}
	
	.form-group label {
		display: block;
		font-weight: 500;
		color: #374151;
		margin-bottom: 0.5rem;
	}
	
	.form-group input {
		width: 100%;
		padding: 0.75rem;
		border: 1px solid #d1d5db;
		border-radius: 0.375rem;
		font-size: 1rem;
		transition: border-color 0.2s;
	}
	
	.form-group input:focus {
		outline: none;
		border-color: #3b82f6;
		box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
	}
	
	.form-actions {
		display: flex;
		gap: 1rem;
		margin-top: 1.5rem;
	}
	
	.btn-save {
		background-color: #10b981;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}
	
	.btn-save:hover {
		background-color: #059669;
	}
	
	.btn-cancel {
		background-color: #6b7280;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}
	
	.btn-cancel:hover {
		background-color: #4b5563;
	}
	
	.profile-display {
		max-width: 600px;
	}
	
	.info-item {
		display: flex;
		padding: 1rem 0;
		border-bottom: 1px solid #f3f4f6;
	}
	
	.info-item:last-child {
		border-bottom: none;
	}
	
	.info-item .label {
		flex: 0 0 150px;
		font-weight: 500;
		color: #374151;
	}
	
	.info-item .value {
		color: #1f2937;
	}
	
	.empty-orders {
		text-align: center;
		padding: 3rem;
	}
	
	.empty-icon {
		color: #9ca3af;
		margin-bottom: 1rem;
	}
	
	.empty-orders h3 {
		font-size: 1.25rem;
		color: #1f2937;
		margin-bottom: 0.5rem;
	}
	
	.empty-orders p {
		color: #6b7280;
		margin-bottom: 1.5rem;
	}
	
	.btn-shop {
		display: inline-block;
		background-color: #3b82f6;
		color: white;
		padding: 0.75rem 1.5rem;
		text-decoration: none;
		border-radius: 0.375rem;
		font-weight: 500;
		transition: background-color 0.2s;
	}
	
	.btn-shop:hover {
		background-color: #2563eb;
	}
	
	.orders-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
	
	.order-card {
		border: 1px solid #e5e7eb;
		border-radius: 0.5rem;
		padding: 1.5rem;
		transition: box-shadow 0.2s;
	}
	
	.order-card:hover {
		box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
	}
	
	.order-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 1rem;
	}
	
	.order-info h3 {
		font-size: 1.125rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 0.25rem;
	}
	
	.order-date {
		color: #6b7280;
		font-size: 0.875rem;
	}
	
	.status-badge {
		padding: 0.25rem 0.75rem;
		border-radius: 9999px;
		font-size: 0.875rem;
		font-weight: 500;
		text-transform: capitalize;
	}
	
	.status-completed {
		background-color: #d1fae5;
		color: #065f46;
	}
	
	.status-pending {
		background-color: #fef3c7;
		color: #92400e;
	}
	
	.status-processing {
		background-color: #dbeafe;
		color: #1e40af;
	}
	
	.status-cancelled {
		background-color: #fee2e2;
		color: #991b1b;
	}
	
	.status-unknown {
		background-color: #f3f4f6;
		color: #374151;
	}
	
	.order-details {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
		margin-bottom: 1rem;
	}
	
	.order-items {
		flex: 1;
	}
	
	.order-item {
		display: flex;
		justify-content: space-between;
		color: #6b7280;
		font-size: 0.875rem;
		margin-bottom: 0.25rem;
	}
	
	.more-items {
		color: #9ca3af;
		font-size: 0.875rem;
		font-style: italic;
	}
	
	.order-total {
		text-align: right;
	}
	
	.total-label {
		color: #6b7280;
		font-size: 0.875rem;
		display: block;
	}
	
	.total-amount {
		font-size: 1.125rem;
		font-weight: 600;
		color: #1f2937;
	}
	
	.order-actions {
		display: flex;
		gap: 0.5rem;
	}
	
	.btn-view-order, .btn-reorder {
		background: none;
		border: 1px solid #d1d5db;
		padding: 0.5rem 1rem;
		border-radius: 0.375rem;
		font-size: 0.875rem;
		font-weight: 500;
		color: #374151;
		cursor: pointer;
		transition: all 0.2s;
	}
	
	.btn-view-order:hover, .btn-reorder:hover {
		background-color: #f9fafb;
		border-color: #9ca3af;
	}
	
	.settings-section h2 {
		font-size: 1.5rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 2rem;
	}
	
	.settings-group {
		margin-bottom: 2rem;
		padding-bottom: 2rem;
		border-bottom: 1px solid #e5e7eb;
	}
	
	.settings-group:last-child {
		border-bottom: none;
		margin-bottom: 0;
	}
	
	.settings-group h3 {
		font-size: 1.125rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 1rem;
	}
	
	.setting-item {
		margin-bottom: 0.75rem;
	}
	
	.setting-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-size: 1rem;
		color: #374151;
		cursor: pointer;
	}
	
	.setting-label input[type="checkbox"] {
		width: 1rem;
		height: 1rem;
	}
	
	.btn-danger {
		background-color: #ef4444;
		color: white;
		border: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}
	
	.btn-danger:hover {
		background-color: #dc2626;
	}
	
	.danger-text {
		color: #ef4444;
		font-size: 0.875rem;
		margin-top: 0.5rem;
	}
	
	@media (max-width: 768px) {
		.account-page {
			padding: 1rem;
		}
		
		.account-content {
			grid-template-columns: 1fr;
			gap: 1rem;
		}
		
		.account-nav {
			flex-direction: row;
			overflow-x: auto;
			white-space: nowrap;
		}
		
		.nav-tab {
			flex-shrink: 0;
		}
		
		.form-row {
			grid-template-columns: 1fr;
		}
		
		.order-header {
			flex-direction: column;
			gap: 0.5rem;
		}
		
		.order-details {
			flex-direction: column;
			align-items: flex-start;
			gap: 1rem;
		}
		
		.order-total {
			text-align: left;
		}
	}
</style>