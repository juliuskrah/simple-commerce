<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import VariantForm from '$lib/components/VariantForm.svelte';
	import { page } from '$app/stores';
	import type { PageData } from './$types';
	import { goto } from '$app/navigation';

	interface Props {
		data: PageData;
		form?: any;
	}
	let { data, form }: Props = $props();
	const user = $derived(data.user);
	let { ProductVariantDetail } = $derived(data);
	const variant = $derived($ProductVariantDetail.data?.variant);
	const productId = $page.params.id;

	function handleCancel() {
		goto(`/dashboard/products/${productId}`);
	}
</script>

<DashboardLayout title="Edit Product Variant" {user}>
	{#if variant}
		<div class="mb-6">
			<nav class="mb-2 text-sm text-gray-500">
				<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
				<span class="mx-2">/</span>
				<a href="/dashboard/products/{productId}" class="hover:text-gray-700"
					>{variant.product?.title || 'Product Details'}</a
				>
				<span class="mx-2">/</span>
				<span class="text-gray-900">Edit Variant</span>
			</nav>
			<h1 class="text-2xl font-bold text-gray-900">Edit Product Variant</h1>
		</div>

		<VariantForm {variant} {productId} onCancel={handleCancel} errors={form?.errors} />
	{:else}
		<!-- Loading or Error State -->
		<div class="flex items-center justify-center py-12">
			<div class="text-center">
				<h3 class="text-lg font-medium text-gray-900">Variant not found</h3>
				<p class="mt-2 text-sm text-gray-500">
					The variant you're looking for doesn't exist or you don't have permission to view it.
				</p>
				<a
					href="/dashboard/products/{productId}"
					class="mt-4 inline-flex items-center rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
				>
					Back to Product
				</a>
			</div>
		</div>
	{/if}
</DashboardLayout>
