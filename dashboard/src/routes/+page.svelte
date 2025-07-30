<script lang="ts">
	let { data } = $props();
	const user = $derived(data.user);
</script>

<div class="py-8">
	<h1 class="mb-6 text-3xl font-bold">Welcome to Simple Commerce Dashboard</h1>
	
	{#if user}
		<div class="mb-6 rounded-lg bg-white p-6 shadow">
			<h2 class="mb-4 text-xl font-semibold">User Information</h2>
			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<div>
					<p class="text-sm text-gray-500">Name</p>
					<p class="font-medium">{user.name || 'Not provided'}</p>
				</div>
				<div>
					<p class="text-sm text-gray-500">Email</p>
					<p class="font-medium">{user.email || 'Not provided'}</p>
				</div>
				<div>
					<p class="text-sm text-gray-500">User ID</p>
					<p class="font-medium">{user.sub || 'Unknown'}</p>
				</div>
			</div>
		</div>
		
		<div class="grid grid-cols-1 gap-6 md:grid-cols-3">
			<div class="rounded-lg bg-white p-6 shadow">
				<h3 class="mb-2 text-lg font-semibold">Orders</h3>
				<p class="text-gray-600">Manage your store orders</p>
				<a href="/dashboard/orders" class="mt-4 inline-block text-blue-600 hover:underline">
					View Orders →
				</a>
			</div>
			
			<div class="rounded-lg bg-white p-6 shadow">
				<h3 class="mb-2 text-lg font-semibold">Products</h3>
				<p class="text-gray-600">Manage your product catalog</p>
				<a href="/dashboard/products" class="mt-4 inline-block text-blue-600 hover:underline">
					View Products →
				</a>
			</div>
			
			<div class="rounded-lg bg-white p-6 shadow">
				<h3 class="mb-2 text-lg font-semibold">Customers</h3>
				<p class="text-gray-600">Manage your customer accounts</p>
				<a href="/dashboard/customers" class="mt-4 inline-block text-blue-600 hover:underline">
					View Customers →
				</a>
			</div>
		</div>
	{:else}
		<p>Loading...</p>
	{/if}
</div>
