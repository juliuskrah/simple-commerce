<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import VariantForm from '$lib/components/VariantForm.svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { enhance } from '$app/forms';
	import type { PageData, ActionData } from './$houdini';
	import { superForm } from 'sveltekit-superforms/client';

	interface Props {
		data: PageData;
		form?: any;
	}

	let { data, form }: Props = $props();
	const user = $derived(data.user);
	
	const productId = $page.params.id;

	let isSubmitting = $state(false);
	let formElement: HTMLFormElement;

	async function handleSubmit(formData: any) {
		if (!formElement) return;
		
		// Populate the form with the data and submit
		const form = formElement;
		const titleInput = form.querySelector('[name="title"]') as HTMLInputElement;
		const skuInput = form.querySelector('[name="sku"]') as HTMLInputElement;
		const amountInput = form.querySelector('[name="amount"]') as HTMLInputElement;
		const currencyInput = form.querySelector('[name="currency"]') as HTMLSelectElement;
		
		if (titleInput) titleInput.value = formData.title;
		if (skuInput) skuInput.value = formData.sku;
		if (amountInput) amountInput.value = formData.price?.amount || '0';
		if (currencyInput) currencyInput.value = formData.price?.currency || 'USD';
		
		form.requestSubmit();
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

	<!-- Hidden form for server action -->
<!-- Superform hidden form -->
{#if form}
	<form method="POST" use:enhance bind:this={formElement} style="display:none;">
		<input type="hidden" name="title" value={form?.data?.title}>
		<input type="hidden" name="sku" value={form?.data?.sku}>
		<input type="hidden" name="amount" value={form?.data?.amount}>
		<input type="hidden" name="currency" value={form?.data?.currency}>
	</form>
{/if}

	<VariantForm
		{productId}
		onSubmit={handleSubmit}
		onCancel={handleCancel}
		{isSubmitting}
		errors={form?.errors}
	/>
</DashboardLayout>
