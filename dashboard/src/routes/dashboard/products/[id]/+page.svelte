<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import type { PageData } from './$houdini';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { DeleteProductVariantStore } from '$houdini';
	import { notifications } from '$lib/stores/notifications';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);
	let { ProductDetail } = $derived(data);

	const product = $derived($ProductDetail.data?.product);
	const baseEdges = $derived(product?.variants?.edges || []);
	let variantEdges = $state<any[]>([]);
	$effect(() => {
		variantEdges = baseEdges;
	});

	let successMessage = $state<string | null>(null);
	let notificationRegion: HTMLElement | null = $state(null);
	let successBanner: HTMLElement | null = $state(null);

	// Refetch logic when coming from create/update operations & focus success banner
	$effect(() => {
		(async () => {
			const url = $page.url;
			if (!product) return;
			const created = url.searchParams.get('variantCreated');
			const updated = url.searchParams.get('updated');
			if (created || updated) {
				try {
					await ProductDetail.fetch({ policy: 'NetworkOnly' });
					successMessage = created
						? 'Variant created successfully.'
						: 'Variant updated successfully.';
					const clean = new URL(url.toString());
					clean.searchParams.delete('variantCreated');
					clean.searchParams.delete('updated');
					history.replaceState({}, '', clean.pathname + clean.search);
					setTimeout(() => successBanner?.focus(), 0);
				} catch (e) {
					console.error('Refetch after mutation failed', e);
				}
			}
		})();
	});

	// Initialize GraphQL mutation store for deleting variants
	const deleteVariantMutation = new DeleteProductVariantStore();

	// Helper function to format currency
	function formatCurrency(amount: number, currency: string = 'USD'): string {
		return new Intl.NumberFormat('en-US', {
			style: 'currency',
			currency: currency
		}).format(amount);
	}

	// Handle variant actions
	function editVariant(variantId: string) {
		const pid = encodeURIComponent($page.params.id);
		const vid = encodeURIComponent(variantId);
		goto(`/dashboard/products/${pid}/variants/${vid}/edit`);
	}

	function addVariant() {
		const pid = encodeURIComponent($page.params.id);
		goto(`/dashboard/products/${pid}/variants/new`);
	}

	// Auto-dismiss success banner after a few seconds
	$effect(() => {
		if (successMessage) {
			const timer = setTimeout(() => {
				successMessage = null;
			}, 5000);
			return () => clearTimeout(timer);
		}
	});

	async function deleteVariant(variantId: string) {
		if (!confirm('Are you sure you want to delete this variant? This action cannot be undone.')) {
			return;
		}

		try {
			const result = await deleteVariantMutation.mutate({
				id: variantId
			});

			if (result.errors) {
				throw new Error(result.errors.map((e: any) => e.message).join(', '));
			}
			// Optimistically remove from UI before refetch
			variantEdges = variantEdges.filter((edge) => edge?.node.id !== variantId);
			notifications.push({ type: 'success', message: 'Variant deleted.' });
			setTimeout(() => notificationRegion?.focus(), 0);
			// Background refetch to ensure consistency
			ProductDetail.fetch({ policy: 'NetworkOnly' }).catch((e) =>
				console.warn('Refetch after delete failed', e)
			);
		} catch (error) {
			console.error('Error deleting variant:', error);
			notifications.push({ type: 'error', message: 'Failed to delete variant.' });
			setTimeout(() => notificationRegion?.focus(), 0);
		}
	}
</script>

<DashboardLayout title="Product Details" {user}>
	{#if product}
		<!-- Product Header -->
		<div class="mb-6">
			<!-- Inline success after create/update -->
			{#if successMessage}
				<div
					class="mb-4 flex items-start justify-between gap-4 rounded-md border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800 outline-none"
					role="status"
					aria-live="polite"
					tabindex="-1"
					bind:this={successBanner}
				>
					<span>{successMessage}</span>
					<button
						class="ml-auto text-xs font-medium text-green-700 hover:text-green-900 focus:outline-none"
						type="button"
						aria-label="Dismiss success message"
						onclick={() => (successMessage = null)}
					>
						Dismiss
					</button>
				</div>
			{/if}
			<!-- Notifications stack -->
			<div
				class="space-y-2 outline-none"
				aria-live="polite"
				aria-relevant="additions"
				role="status"
				tabindex="-1"
				bind:this={notificationRegion}
			>
				{#each $notifications as note (note.id)}
					<div
						class="flex items-start justify-between rounded-md border px-4 py-3 text-sm {note.type ===
						'success'
							? 'border-green-200 bg-green-50 text-green-800'
							: note.type === 'error'
								? 'border-red-200 bg-red-50 text-red-800'
								: 'border-blue-200 bg-blue-50 text-blue-800'}"
					>
						<p>{note.message}</p>
						<button
							class="ml-4 text-xs opacity-70 hover:opacity-100"
							onclick={() => notifications.dismiss(note.id)}>Dismiss</button
						>
					</div>
				{/each}
			</div>
			<div class="flex items-center justify-between">
				<div>
					<nav class="mb-2 text-sm text-gray-500">
						<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
						<span class="mx-2">/</span>
						<span class="text-gray-900">{product.title}</span>
					</nav>
					<h1 class="text-2xl font-bold text-gray-900">{product.title}</h1>
				</div>
				<div class="flex space-x-3">
					<button
						class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
					>
						Edit Product
					</button>
					<button
						onclick={addVariant}
						class="flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="mr-2 h-4 w-4"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								fill-rule="evenodd"
								d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
								clip-rule="evenodd"
							/>
						</svg>
						Add Variant
					</button>
				</div>
			</div>
		</div>

		<!-- Product Information Cards -->
		<div class="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
			<!-- Basic Info Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Basic Information</h3>
				<div class="space-y-3">
					<div>
						<span class="text-sm font-medium text-gray-500">Name</span>
						<p class="text-gray-900">{product.title}</p>
					</div>
					{#if product.description}
						<div>
							<span class="text-sm font-medium text-gray-500">Description</span>
							<p class="text-gray-900">{product.description}</p>
						</div>
					{/if}
					<div>
						<span class="text-sm font-medium text-gray-500">Slug</span>
						<p class="text-gray-900">{product.slug}</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Status</span>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-medium {product.status ===
							'PUBLISHED'
								? 'bg-green-100 text-green-800'
								: product.status === 'DRAFT'
									? 'bg-yellow-100 text-yellow-800'
									: 'bg-red-100 text-red-800'}"
						>
							{product.status}
						</span>
					</div>
				</div>
			</div>

			<!-- Pricing Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Pricing</h3>
				<div class="space-y-3">
					{#if product.priceRange}
						<div>
							<span class="text-sm font-medium text-gray-500">Price Range</span>
							<p class="text-lg font-semibold text-gray-900">
								{#if product.priceRange.start && product.priceRange.stop}
									{formatCurrency(
										product.priceRange.start.amount,
										product.priceRange.start.currency
									)} -
									{formatCurrency(product.priceRange.stop.amount, product.priceRange.stop.currency)}
								{:else if product.priceRange.start}
									From {formatCurrency(
										product.priceRange.start.amount,
										product.priceRange.start.currency
									)}
								{:else}
									Not set
								{/if}
							</p>
						</div>
					{/if}
					{#if product.category}
						<div>
							<span class="text-sm font-medium text-gray-500">Category</span>
							<p class="text-gray-900">{product.category.title}</p>
						</div>
					{/if}
				</div>
			</div>

			<!-- Metadata Card -->
			<div class="rounded-lg bg-white p-6 shadow-md">
				<h3 class="mb-4 text-lg font-semibold text-gray-900">Metadata</h3>
				<div class="space-y-3">
					<div>
						<span class="text-sm font-medium text-gray-500">Created</span>
						<p class="text-gray-900">
							{new Date(product.createdAt).toLocaleDateString()}
						</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Updated</span>
						<p class="text-gray-900">
							{new Date(product.updatedAt).toLocaleDateString()}
						</p>
					</div>
					<div>
						<span class="text-sm font-medium text-gray-500">Variants</span>
						<p class="text-lg font-semibold text-gray-900">{variantEdges.length}</p>
					</div>
				</div>
			</div>
		</div>

		<!-- Product Variants Section -->
		<div class="rounded-lg bg-white shadow-md">
			<div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
				<h2 class="text-lg font-semibold text-gray-800">Product Variants</h2>
				<button
					onclick={addVariant}
					class="flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
							clip-rule="evenodd"
						/>
					</svg>
					Add Variant
				</button>
			</div>

			{#if variantEdges.length > 0}
				<!-- Variants Table -->
				<div class="overflow-x-auto">
					<table class="w-full" aria-describedby="variant-table-caption">
						<caption id="variant-table-caption" class="sr-only">Product variants list</caption>
						<thead>
							<tr class="bg-gray-50 text-left">
								<th scope="col" class="px-6 py-3 text-sm font-medium text-gray-500">Variant</th>
								<th scope="col" class="px-6 py-3 text-sm font-medium text-gray-500">SKU</th>
								<th scope="col" class="px-6 py-3 text-sm font-medium text-gray-500">Price</th>
								<th scope="col" class="px-6 py-3 text-sm font-medium text-gray-500">Actions</th>
							</tr>
						</thead>
						<tbody>
							{#each variantEdges as variantEdge (variantEdge?.node.id)}
								{#if variantEdge?.node}
									{@const variant = variantEdge.node}
									<tr class="border-t border-gray-100 hover:bg-gray-50">
										<td class="px-6 py-4">
											<div>
												<p class="font-medium text-gray-800">{variant.title}</p>
											</div>
										</td>
										<td class="px-6 py-4 text-gray-800">{variant.sku || 'N/A'}</td>
										<td class="px-6 py-4 text-gray-800">
											{variant.price
												? formatCurrency(variant.price.amount, variant.price.currency)
												: 'N/A'}
										</td>

										<td class="px-6 py-4">
											<div class="flex space-x-2">
												<button
													onclick={() => editVariant(variant.id)}
													class="text-gray-400 hover:text-primary-600"
													aria-label="Edit variant"
												>
													<svg
														xmlns="http://www.w3.org/2000/svg"
														class="h-5 w-5"
														viewBox="0 0 20 20"
														fill="currentColor"
													>
														<path
															d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
														/>
													</svg>
												</button>
												<button
													onclick={() => deleteVariant(variant.id)}
													class="text-gray-400 hover:text-red-600"
													aria-label="Delete variant"
												>
													<svg
														xmlns="http://www.w3.org/2000/svg"
														class="h-5 w-5"
														viewBox="0 0 20 20"
														fill="currentColor"
													>
														<path
															fill-rule="evenodd"
															d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
															clip-rule="evenodd"
														/>
													</svg>
												</button>
											</div>
										</td>
									</tr>
								{/if}
							{/each}
						</tbody>
					</table>
				</div>
			{:else}
				<!-- Empty State -->
				<div class="flex flex-col items-center justify-center py-12">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-12 w-12 text-gray-400"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
						/>
					</svg>
					<h3 class="mt-2 text-sm font-medium text-gray-900">No variants</h3>
					<p class="mt-1 text-sm text-gray-500">Get started by creating a new variant.</p>
					<button
						onclick={addVariant}
						class="mt-4 inline-flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="mr-2 h-4 w-4"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								fill-rule="evenodd"
								d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
								clip-rule="evenodd"
							/>
						</svg>
						Add Variant
					</button>
				</div>
			{/if}
		</div>
	{:else}
		<!-- Loading or Error State -->
		<div class="flex items-center justify-center py-12">
			<div class="text-center">
				<h3 class="text-lg font-medium text-gray-900">Product not found</h3>
				<p class="mt-2 text-sm text-gray-500">
					The product you're looking for doesn't exist or you don't have permission to view it.
				</p>
				<a
					href="/dashboard/products"
					class="mt-4 inline-flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
				>
					Back to Products
				</a>
			</div>
		</div>
	{/if}
</DashboardLayout>
