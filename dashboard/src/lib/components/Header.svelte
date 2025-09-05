<script lang="ts">
	// Header component for dashboard
	export let title = 'Home';
</script>

<header class="flex h-20 items-center justify-between border-b bg-white px-8">
	<div>
		<h1 class="text-xl font-semibold text-gray-800">{title}</h1>
	</div>

	<div class="flex items-center space-x-4">
		<div class="relative">
			<input
				type="text"
				placeholder="Search..."
				class="rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
			/>
			<span class="absolute right-3 top-2.5 text-gray-400">
				<i class="feather-search"></i>
			</span>
		</div>

		<div class="relative">
			<button class="relative p-2 text-gray-500 hover:text-gray-700" aria-label="Notifications">
				<i class="feather-bell"></i>
				<span class="absolute right-0 top-0 h-2 w-2 rounded-full bg-red-500"></span>
			</button>
		</div>
	</div>
</header>
