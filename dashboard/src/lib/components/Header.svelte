<script lang="ts">
	// Header component for dashboard
	interface Props {
		title?: string;
	}
	
	let { title = 'Home' }: Props = $props();
</script>

<header class="flex h-20 items-center justify-between border-b bg-white px-8">
	<div>
		<h1 class="text-xl font-semibold text-gray-800">{title}</h1>
	</div>

	<div class="flex items-center space-x-4">
		<div class="relative">
			<button class="relative p-2 text-gray-500 hover:text-gray-700" aria-label="Notifications">
				<i class="feather-bell"></i>
				<span class="absolute right-0 top-0 h-2 w-2 rounded-full bg-red-500"></span>
			</button>
		</div>
	</div>
</header>
