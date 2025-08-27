<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';

	interface VariantFormData {
		title: string;
		sku: string;
		price: {
			amount: number;
			currency: string;
		} | null;
	}

	interface Props {
		variant?: any | null;
		productId: string;
		onSubmit: (data: VariantFormData) => Promise<void>;
		onCancel: () => void;
		isSubmitting?: boolean;
	}

	let { variant = null, productId, onSubmit, onCancel, isSubmitting = false }: Props = $props();

	// Form state
	let formData: VariantFormData = $state({
		title: variant?.title || '',
		sku: variant?.sku || '',
		price: variant?.price ? {
			amount: variant.price.amount,
			currency: variant.price.currency
		} : null
	});

	// Form state with additional helper for amount input
	let amountInput = $state(formData.price?.amount || 0);
	let currencyInput = $state(formData.price?.currency || 'USD');

	// Update formData when inputs change
	$effect(() => {
		if (amountInput > 0 || currencyInput !== 'USD') {
			formData.price = {
				amount: amountInput,
				currency: currencyInput
			};
		} else {
			formData.price = null;
		}
	});

	// Form submission
	async function handleSubmit(event: Event) {
		event.preventDefault();
		await onSubmit(formData);
	}
</script>

<form onsubmit={handleSubmit} class="space-y-6">
	<!-- Basic Information -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Basic Information</h3>
		<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
			<div>
				<label for="title" class="block text-sm font-medium text-gray-700">Title *</label>
				<input
					type="text"
					id="title"
					bind:value={formData.title}
					required
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="e.g., Large / Red"
				/>
			</div>
			<div>
				<label for="sku" class="block text-sm font-medium text-gray-700">SKU *</label>
				<input
					type="text"
					id="sku"
					bind:value={formData.sku}
					required
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="e.g., PROD-001-LG-RED"
				/>
			</div>
		</div>
	</div>

	<!-- Pricing -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Pricing</h3>
		<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
			<div>
				<label for="amount" class="block text-sm font-medium text-gray-700">Price Amount</label>
				<input
					type="number"
					id="amount"
					bind:value={amountInput}
					step="0.01"
					min="0"
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="0.00"
				/>
			</div>
			<div>
				<label for="currency" class="block text-sm font-medium text-gray-700">Currency</label>
				<select
					id="currency"
					bind:value={currencyInput}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
				>
					<option value="USD">USD</option>
					<option value="EUR">EUR</option>
					<option value="GBP">GBP</option>
					<option value="JPY">JPY</option>
					<option value="CAD">CAD</option>
					<option value="AUD">AUD</option>
				</select>
			</div>
		</div>
	</div>

	<!-- Form Actions -->
	<div class="flex justify-end space-x-3">
		<button
			type="button"
			onclick={onCancel}
			class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
			disabled={isSubmitting}
		>
			Cancel
		</button>
		<button
			type="submit"
			class="bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 rounded-md px-4 py-2 text-sm font-medium text-white"
			disabled={isSubmitting}
		>
			{isSubmitting ? 'Saving...' : variant ? 'Update Variant' : 'Create Variant'}
		</button>
	</div>
</form>
