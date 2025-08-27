<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import VariantForm from '$lib/components/VariantForm.svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);
	let { ProductVariantDetail } = $derived(data);
	
	const variant = $derived($ProductVariantDetail.data?.variant);
	const productId = $page.params.id;

	let isSubmitting = $state(false);

	async function handleSubmit(formData: any) {
		if (!variant) return;
		
		isSubmitting = true;
		try {
			// Convert form data to GraphQL input
			const input: any = {
				title: formData.title,
				sku: formData.sku
			};

			// Add price if provided
			if (formData.price && formData.price.amount > 0) {
				input.price = {
					amount: formData.price.amount,
					currency: formData.price.currency
				};
			}

			// For now, simulate the mutation - TODO: implement actual GraphQL call
			console.log('Would update variant with:', { id: variant.id, input });
			alert('Variant updated successfully!');

			goto(`/dashboard/products/${productId}`);
		} catch (error) {
			console.error('Error updating variant:', error);
			alert('Failed to update variant. Please try again.');
		} finally {
			isSubmitting = false;
		}
	}

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
				<a href="/dashboard/products/{productId}" class="hover:text-gray-700">{variant.product?.title || 'Product Details'}</a>
				<span class="mx-2">/</span>
				<span class="text-gray-900">Edit Variant</span>
			</nav>
			<h1 class="text-2xl font-bold text-gray-900">Edit Product Variant</h1>
		</div>

		<VariantForm
			{variant}
			{productId}
			onSubmit={handleSubmit}
			onCancel={handleCancel}
			{isSubmitting}
		/>
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
					class="bg-primary-600 hover:bg-primary-700 mt-4 inline-flex items-center rounded-md px-4 py-2 text-sm font-medium text-white"
				>
					Back to Product
				</a>
			</div>
		</div>
	{/if}
</DashboardLayout>
