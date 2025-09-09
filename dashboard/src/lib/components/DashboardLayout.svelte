<script lang="ts">
	import Sidebar from '$lib/components/Sidebar.svelte';
	import Header from '$lib/components/Header.svelte';
	import StickySearch from '$lib/components/StickySearch.svelte';
	import { goto } from '$app/navigation';

	export let title = 'Home';
	export let user: any = null;

	function handleSearch(query: string) {
		if (query.trim()) {
			// Navigate to products page with search query
			goto(`/dashboard/products?query=${encodeURIComponent(query.trim())}`);
		}
	}
</script>

<div class="flex h-screen bg-gray-50">
	<Sidebar {user} />

	<div class="flex flex-1 flex-col overflow-hidden">
		<Header {title} />
		<StickySearch placeholder="Search products, customers, orders..." onSearch={handleSearch} />

		<main class="flex-1 overflow-y-auto overflow-x-hidden p-6">
			<slot />
		</main>
	</div>
</div>
