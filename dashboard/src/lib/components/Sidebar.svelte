<script lang="ts">
	// Sidebar component for home
	let activeItem = 'home';

	export let user: any = null;

	const menuItems = [
		{ id: 'home', label: 'Home', icon: 'home' },
		{ id: 'products', label: 'Products', icon: 'shopping-bag' },
		{ id: 'orders', label: 'Orders', icon: 'shopping-cart' },
		{ id: 'customers', label: 'Customers', icon: 'users' },
		{ id: 'analytics', label: 'Analytics', icon: 'bar-chart-2' },
		{ id: 'settings', label: 'Settings', icon: 'settings' }
	];

	function setActive(id: string) {
		activeItem = id;
	}
</script>

<div class="h-full w-64 bg-white shadow-lg">
	<div class="flex h-20 items-center justify-center border-b">
		<div class="text-primary-600 text-2xl font-bold">Simple Commerce</div>
	</div>

	<div class="py-4">
		<ul>
			{#each menuItems as item}
				<li class="mb-1">
					<a
						href={`/dashboard/${item.id === 'home' ? '' : item.id}`}
						class="flex items-center px-6 py-3 {activeItem === item.id
							? 'bg-primary-50 text-primary-600 border-primary-600 border-r-4'
							: 'text-gray-600 hover:bg-gray-100'}"
						onclick={() => setActive(item.id)}
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
