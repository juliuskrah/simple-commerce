<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import ProductForm from '$lib/components/ProductForm.svelte';
	import { goto } from '$app/navigation';
	import type { PageData } from './$houdini';

	interface Props {
		data: PageData;
		form?: any;
	}
	let { data, form }: Props = $props();
	const user = $derived(data.user);
	let { ProductEdit } = $derived(data);
	const product = $derived($ProductEdit.data?.product);

	function handleCancel() {
		if (product) {
			goto(`/dashboard/products/${product.id}`);
		} else {
			goto('/dashboard/products');
		}
	}
</script>

<DashboardLayout title="Edit Product" {user}>
	{#if product}
		<div class="mb-6">
			<nav class="mb-2 text-sm text-gray-500">
				<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
				<span class="mx-2">/</span>
				<a href="/dashboard/products/{product.id}" class="hover:text-gray-700">{product.title}</a>
				<span class="mx-2">/</span>
				<span class="text-gray-900">Edit</span>
			</nav>
			<h1 class="text-2xl font-bold text-gray-900">Edit Product</h1>
		</div>

		<ProductForm {product} onCancel={handleCancel} errors={form?.errors} />
	{:else}
		<!-- Loading or Error State -->
		<div class="flex items-center justify-center py-12">
			<div class="text-center">
				<h3 class="text-lg font-medium text-gray-900">Product not found</h3>
				<p class="mt-2 text-sm text-gray-500">
					The product you're trying to edit doesn't exist or you don't have permission to view it.
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