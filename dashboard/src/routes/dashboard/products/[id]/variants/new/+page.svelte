<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import VariantForm from '$lib/components/VariantForm.svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import type { PageData } from './$houdini';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
	const user = $derived(data.user);
	
	const productId = $page.params.id;

	let isSubmitting = $state(false);

	async function handleSubmit(formData: any) {
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

			// For now, just show a success message since we need to implement the actual mutation
			alert('Variant would be created with: ' + JSON.stringify(input));
			goto(`/dashboard/products/${productId}`);
		} catch (error) {
			console.error('Error creating variant:', error);
			alert('Failed to create variant. Please try again.');
		} finally {
			isSubmitting = false;
		}
	}

	function handleCancel() {
		goto(`/dashboard/products/${productId}`);
	}
</script>

<DashboardLayout title="Add Product Variant" {user}>
	<div class="mb-6">
		<nav class="mb-2 text-sm text-gray-500">
			<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
			<span class="mx-2">/</span>
			<a href="/dashboard/products/{productId}" class="hover:text-gray-700">Product Details</a>
			<span class="mx-2">/</span>
			<span class="text-gray-900">Add Variant</span>
		</nav>
		<h1 class="text-2xl font-bold text-gray-900">Add Product Variant</h1>
	</div>

	<VariantForm
		{productId}
		onSubmit={handleSubmit}
		onCancel={handleCancel}
		{isSubmitting}
	/>
</DashboardLayout>
