<script lang="ts">
import DashboardLayout from '$lib/components/DashboardLayout.svelte';
import VariantForm from '$lib/components/VariantForm.svelte';
import { page } from '$app/stores';
import type { PageData } from './$houdini';
import { goto } from '$app/navigation';

interface Props { data: PageData; form?: any }
let { data, form }: Props = $props();
const user = $derived(data.user);
const productId = $page.params.id;

function handleCancel(){ goto(`/dashboard/products/${productId}`); }
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
	<VariantForm {productId} onCancel={handleCancel} errors={form?.errors} />
</DashboardLayout>
