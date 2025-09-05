<!-- routes/auth/+page.svelte -->
<script lang="ts">
</script>

<div class="flex min-h-screen items-center justify-center bg-gray-50">
	<div class="w-full max-w-md">
		<div class="rounded-lg bg-white p-8 shadow-md">
			<h1 class="mb-6 text-2xl font-bold text-gray-900">Sign in</h1>
			<p class="mb-4 text-gray-600">Sign in to access the dashboard.</p>

			<div class="mt-6">
				<a
					href="/auth/login"
					class="flex w-full items-center justify-center rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
				>
					Sign in with DEX
				</a>
			</div>
		</div>
	</div>
</div>
