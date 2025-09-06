<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import ProductForm from '$lib/components/ProductForm.svelte';
	import { goto } from '$app/navigation';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
		form?: any;
	}
	let { data, form }: Props = $props();
	const user = $derived(data.user);

	function handleCancel() {
		goto('/dashboard/products');
	}
</script>

<DashboardLayout title="Create Product" {user}>
	<div class="mb-6">
		<nav class="mb-2 text-sm text-gray-500">
			<a href="/dashboard/products" class="hover:text-gray-700">Products</a>
			<span class="mx-2">/</span>
			<span class="text-gray-900">Create Product</span>
		</nav>
		<h1 class="text-2xl font-bold text-gray-900">Create New Product</h1>
	</div>

	<ProductForm onCancel={handleCancel} errors={form?.errors} />
</DashboardLayout>