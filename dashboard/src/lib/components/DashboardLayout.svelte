<script lang="ts">
	import Sidebar from '$lib/components/Sidebar.svelte';
	import Header from '$lib/components/Header.svelte';

	export let title = 'Home';
	export let user: any = null;
</script>

<div class="flex h-screen bg-gray-50">
	<Sidebar {user} />

	<div class="flex flex-1 flex-col overflow-hidden">
		<Header {title} />

		<main class="flex-1 overflow-y-auto overflow-x-hidden p-6">
			<slot />
		</main>
	</div>
</div>
