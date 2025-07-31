<script lang="ts">
	let { children, data } = $props();
	const user = $derived(data.user);

	import '../app.css';
</script>

{#if user}
	<header class="bg-white shadow">
		<div class="mx-auto flex max-w-7xl items-center justify-between p-4">
			<h1 class="text-xl font-bold">Simple Commerce Dashboard</h1>
			<div class="flex items-center gap-4">
				<span class="text-gray-600">Welcome, {user.name || user.email || 'User'}</span>
				<a href="/auth/logout" class="rounded bg-red-500 px-4 py-2 text-white hover:bg-red-600">
					Logout
				</a>
			</div>
		</div>
	</header>
{/if}

<main class="container mx-auto p-4">
	{@render children()}
</main>

<style lang="postcss">
	@reference "tailwindcss";
	:global(html, body) {
		height: 100%;
		margin: 0;
		font-family:
			'Raleway',
			-apple-system,
			BlinkMacSystemFont,
			'Segoe UI',
			Roboto,
			Oxygen,
			Ubuntu,
			Cantarell,
			'Open Sans',
			'Helvetica Neue',
			sans-serif;
	}

	:global(body) {
		background-color: #f8fafc;
		color: #1e293b;
	}
</style>
