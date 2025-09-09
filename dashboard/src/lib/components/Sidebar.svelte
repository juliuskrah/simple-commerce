<script lang="ts">
	import { page } from '$app/stores';

	export let user: any = null;

	// Derive active item from current route
	$: activeItem = (() => {
		const pathname = $page.url.pathname;
		if (pathname === '/dashboard' || pathname === '/dashboard/') return 'home';
		if (pathname.includes('/products')) return 'products';
		if (pathname.includes('/categories')) return 'categories';
		if (pathname.includes('/orders')) return 'orders';
		if (pathname.includes('/customers')) return 'customers';
		if (pathname.includes('/staff')) return 'staff';
		if (pathname.includes('/bots')) return 'bots';
		if (pathname.includes('/analytics')) return 'analytics';
		if (pathname.includes('/settings')) return 'settings';
		return 'home';
	})();

	const menuItems = [
		{ id: 'home', label: 'Home', icon: 'home' },
		{ id: 'products', label: 'Products', icon: 'shopping-bag' },
		{ id: 'categories', label: 'Categories', icon: 'folder-tree' },
		{ id: 'orders', label: 'Orders', icon: 'shopping-cart' },
		{ id: 'customers', label: 'Customers', icon: 'users' },
		{ id: 'staff', label: 'Staff', icon: 'user-check' },
		{ id: 'bots', label: 'Bots', icon: 'cpu' },
		{ id: 'analytics', label: 'Analytics', icon: 'bar-chart-2' },
		{ id: 'settings', label: 'Settings', icon: 'settings' }
	];
</script>

<div class="h-full w-64 bg-white shadow-lg">
	<div class="flex h-20 items-center justify-center border-b">
		<div class="text-2xl font-bold text-primary-600">Simple Commerce</div>
	</div>

	<div class="py-4">
		<ul>
			{#each menuItems as item}
				<li class="mb-1">
					<a
						href={`/dashboard/${item.id === 'home' ? '' : item.id}`}
						class="flex items-center px-6 py-3 {activeItem === item.id
							? 'border-r-4 border-primary-600 bg-primary-50 text-primary-600'
							: 'text-gray-600 hover:bg-gray-100'}"
					>
						<span class="mr-3">
							<i class="feather-{item.icon}"></i>
						</span>
						<span>{item.label}</span>
					</a>
				</li>
			{/each}
		</ul>
	</div>

	<div class="absolute bottom-0 w-64 border-t p-4">
		<div class="flex items-center">
			<div class="h-10 w-10 rounded-full bg-gray-300">
				{#if user?.picture}
					<img src={user.picture} alt="Profile" class="h-10 w-10 rounded-full" />
				{/if}
			</div>
			<div class="ml-3">
				<p class="text-sm font-medium">{user?.name || user?.email || 'Guest User'}</p>
				<p class="text-xs text-gray-500">{user?.role || 'User'}</p>
			</div>
		</div>
	</div>
</div>
