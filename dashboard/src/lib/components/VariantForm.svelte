<script lang="ts">
	interface Props {
		variant?: any | null;
		productId: string; // kept for potential future use
		onCancel: () => void;
		isSubmitting?: boolean;
		errors?: Record<string, string[] | string> | null;
	}

	let {
		variant = null,
		productId,
		onCancel,
		isSubmitting = false,
		errors = null
	}: Props = $props();

	let titleVal = $state(variant?.title || '');
	let skuVal = $state(variant?.sku || '');
	let amountVal = $state(variant?.price?.amount || 0);
	let currencyVal = $state(variant?.price?.currency || 'USD');

	$effect(() => {
		if (!errors) return;
		const order = ['title', 'sku', 'amount', 'currency'];
		for (const field of order) {
			if (errors[field]) {
				const el = document.getElementById(field);
				if (el) (el as HTMLElement).focus();
				break;
			}
		}
	});
</script>

<!-- Dynamic action: create vs update -->
<form method="POST" action={variant ? '?/update' : '?/create'} class="space-y-6" novalidate>
	{#if errors && (errors.title || errors.sku || errors.amount || errors.currency)}
		<div
			class="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-800"
			role="alert"
			aria-live="assertive"
		>
			<p class="mb-2 font-semibold">Please fix the following:</p>
			<ul class="list-inside list-disc space-y-1">
				{#if errors.title}
					<li>
						<a href="#title" class="underline"
							>Title: {Array.isArray(errors.title) ? errors.title[0] : errors.title}</a
						>
					</li>
				{/if}
				{#if errors.sku}
					<li>
						<a href="#sku" class="underline"
							>SKU: {Array.isArray(errors.sku) ? errors.sku[0] : errors.sku}</a
						>
					</li>
				{/if}
				{#if errors.amount}
					<li>
						<a href="#amount" class="underline"
							>Amount: {Array.isArray(errors.amount) ? errors.amount[0] : errors.amount}</a
						>
					</li>
				{/if}
				{#if errors.currency}
					<li>
						<a href="#currency" class="underline"
							>Currency: {Array.isArray(errors.currency) ? errors.currency[0] : errors.currency}</a
						>
					</li>
				{/if}
			</ul>
		</div>
	{/if}
	<!-- Basic Information -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Basic Information</h3>
		<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
			<div>
				<label for="title" class="block text-sm font-medium text-gray-700">Title *</label>
				<input
					type="text"
					id="title"
					name="title"
					bind:value={titleVal}
					required
					aria-required="true"
					aria-invalid={errors?.title ? 'true' : 'false'}
					aria-describedby={errors?.title ? 'title-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="e.g., Large / Red"
				/>
				{#if errors?.title}
					<p id="title-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.title) ? errors.title[0] : errors.title}
					</p>
				{/if}
			</div>
			<div>
				<label for="sku" class="block text-sm font-medium text-gray-700">SKU *</label>
				<input
					type="text"
					id="sku"
					name="sku"
					bind:value={skuVal}
					required
					aria-required="true"
					aria-invalid={errors?.sku ? 'true' : 'false'}
					aria-describedby={errors?.sku ? 'sku-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="e.g., PROD-001-LG-RED"
				/>
				{#if errors?.sku}
					<p id="sku-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.sku) ? errors.sku[0] : errors.sku}
					</p>
				{/if}
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
					name="amount"
					bind:value={amountVal}
					step="0.01"
					min="0"
					aria-invalid={errors?.amount ? 'true' : 'false'}
					aria-describedby={errors?.amount ? 'amount-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="0.00"
				/>
				{#if errors?.amount}
					<p id="amount-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.amount) ? errors.amount[0] : errors.amount}
					</p>
				{/if}
			</div>
			<div>
				<label for="currency" class="block text-sm font-medium text-gray-700">Currency</label>
				<select
					id="currency"
					name="currency"
					bind:value={currencyVal}
					aria-invalid={errors?.currency ? 'true' : 'false'}
					aria-describedby={errors?.currency ? 'currency-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
				>
					<option value="USD">USD</option>
					<option value="EUR">EUR</option>
					<option value="GBP">GBP</option>
					<option value="JPY">JPY</option>
					<option value="CAD">CAD</option>
					<option value="AUD">AUD</option>
				</select>
				{#if errors?.currency}
					<p id="currency-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.currency) ? errors.currency[0] : errors.currency}
					</p>
				{/if}
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
			class="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:bg-primary-400"
			disabled={isSubmitting}
		>
			{isSubmitting ? 'Saving...' : variant ? 'Update Variant' : 'Create Variant'}
		</button>
	</div>
</form>
