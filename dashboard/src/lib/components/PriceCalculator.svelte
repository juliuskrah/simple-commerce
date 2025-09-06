<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PriceContextInput } from '../validation/pricing';
	import { priceContextInputSchema, formatMoney } from '../validation/pricing';
	import FormField from './FormField.svelte';
	import Button from './Button.svelte';
	import Card from './Card.svelte';

	// Props
	export let variantId: string;
	export let disabled = false;

	// Events
	const dispatch = createEventDispatcher<{
		calculate: { variantId: string; context: PriceContextInput };
	}>();

	// Local state
	let context: PriceContextInput = {
		customerGroup: undefined,
		region: undefined,
		currency: 'USD',
		quantity: 1
	};
	let calculatedPrice: { amount: number; currency: string } | null = null;
	let isLoading = false;
	let error: string | null = null;

	// Predefined options
	const currencies = ['USD', 'EUR', 'GBP', 'CAD', 'AUD', 'JPY'];
	const customerGroups = [
		{ value: 'B2C', label: 'B2C (Business to Consumer)' },
		{ value: 'B2B', label: 'B2B (Business to Business)' },
		{ value: 'VIP', label: 'VIP Customers' },
		{ value: 'WHOLESALE', label: 'Wholesale' },
		{ value: 'RETAIL', label: 'Retail' }
	];
	const regions = [
		{ value: 'US', label: 'United States' },
		{ value: 'EU', label: 'European Union' },
		{ value: 'UK', label: 'United Kingdom' },
		{ value: 'CA', label: 'Canada' },
		{ value: 'AU', label: 'Australia' },
		{ value: 'JP', label: 'Japan' }
	];

	// Methods
	async function calculatePrice() {
		try {
			// Validate input
			priceContextInputSchema.parse(context);
			error = null;
			isLoading = true;

			// Call GraphQL query to get resolved price
			const response = await fetch('/graphql', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({
					query: `
						query GetResolvedPrice($id: ID!, $context: PriceContextInput!) {
							variant(id: $id) {
								id
								title
								sku
								resolvedPrice(context: $context) {
									amount
									currency
								}
							}
						}
					`,
					variables: {
						id: variantId,
						context: context
					}
				})
			});

			const result = await response.json();

			if (result.errors) {
				throw new Error(result.errors[0].message);
			}

			calculatedPrice = result.data.variant.resolvedPrice;
			dispatch('calculate', { variantId, context });

		} catch (err: any) {
			error = err.message || 'Failed to calculate price';
			calculatedPrice = null;
		} finally {
			isLoading = false;
		}
	}

	function resetCalculator() {
		context = {
			customerGroup: undefined,
			region: undefined,
			currency: 'USD',
			quantity: 1
		};
		calculatedPrice = null;
		error = null;
	}

	// Reactive statements
	$: canCalculate = context.currency && context.quantity > 0;
</script>

<Card class="price-calculator">
	<div class="calculator-header">
		<h3>💰 Price Calculator</h3>
		<p>Calculate contextual pricing for different customer scenarios</p>
	</div>

	<div class="calculator-form">
		<div class="form-grid">
			<FormField label="Customer Group" help="Leave empty for default pricing">
				<select bind:value={context.customerGroup} {disabled} class="form-select">
					<option value={undefined}>Default (no specific group)</option>
					{#each customerGroups as group}
						<option value={group.value}>{group.label}</option>
					{/each}
				</select>
			</FormField>

			<FormField label="Region" help="Geographic pricing context">
				<select bind:value={context.region} {disabled} class="form-select">
					<option value={undefined}>Default (no specific region)</option>
					{#each regions as region}
						<option value={region.value}>{region.label}</option>
					{/each}
				</select>
			</FormField>

			<FormField label="Currency" help="Currency for price calculation">
				<select bind:value={context.currency} {disabled} class="form-select">
					{#each currencies as currency}
						<option value={currency}>{currency}</option>
					{/each}
				</select>
			</FormField>

			<FormField label="Quantity" help="Quantity for tiered pricing">
				<input
					type="number"
					bind:value={context.quantity}
					min="1"
					max="10000"
					{disabled}
					class="form-input"
				/>
			</FormField>
		</div>

		<div class="calculator-actions">
			<Button variant="secondary" on:click={resetCalculator} {disabled}>
				Reset
			</Button>
			<Button 
				variant="primary" 
				on:click={calculatePrice} 
				loading={isLoading}
				disabled={!canCalculate || disabled}
			>
				Calculate Price
			</Button>
		</div>
	</div>

	<!-- Results Section -->
	<div class="results-section">
		{#if error}
			<div class="error-result">
				<div class="error-icon">❌</div>
				<div class="error-content">
					<h4>Calculation Error</h4>
					<p>{error}</p>
				</div>
			</div>
		{:else if calculatedPrice}
			<div class="price-result">
				<div class="price-icon">✅</div>
				<div class="price-content">
					<h4>Calculated Price</h4>
					<div class="price-display">
						{formatMoney(calculatedPrice.amount, calculatedPrice.currency)}
					</div>
					<div class="context-summary">
						<span class="context-item">
							{context.customerGroup || 'Default'} customer
						</span>
						{#if context.region}
							<span class="context-item">in {context.region}</span>
						{/if}
						<span class="context-item">
							{context.quantity} {context.quantity === 1 ? 'unit' : 'units'}
						</span>
					</div>
				</div>
			</div>
		{:else}
			<div class="empty-result">
				<div class="empty-icon">🧮</div>
				<p>Configure pricing context and click "Calculate Price" to see the result</p>
			</div>
		{/if}
	</div>

	<!-- Pricing Context Explanation -->
	<div class="context-explanation">
		<h4>How Contextual Pricing Works</h4>
		<div class="explanation-grid">
			<div class="explanation-item">
				<strong>🏷️ Customer Groups</strong>
				<p>Different pricing for B2B, B2C, VIP, and wholesale customers</p>
			</div>
			<div class="explanation-item">
				<strong>🌍 Geographic Pricing</strong>
				<p>Region-specific pricing based on local market conditions</p>
			</div>
			<div class="explanation-item">
				<strong>💱 Multi-Currency</strong>
				<p>Prices can be set in different currencies for global markets</p>
			</div>
			<div class="explanation-item">
				<strong>📊 Quantity Tiers</strong>
				<p>Volume discounts based on purchase quantity</p>
			</div>
		</div>
	</div>
</Card>

<style>
	.price-calculator {
		max-width: 600px;
		margin: 0 auto;
	}

	.calculator-header {
		text-align: center;
		margin-bottom: 2rem;
		padding-bottom: 1rem;
		border-bottom: 1px solid var(--color-border);
	}

	.calculator-header h3 {
		margin: 0 0 0.5rem 0;
		font-size: 1.5rem;
		font-weight: 600;
	}

	.calculator-header p {
		margin: 0;
		color: var(--color-gray-600);
		font-size: 0.875rem;
	}

	.calculator-form {
		margin-bottom: 2rem;
	}

	.form-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
		margin-bottom: 1.5rem;
	}

	.form-select,
	.form-input {
		width: 100%;
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		font-size: 0.875rem;
	}

	.form-select:focus,
	.form-input:focus {
		outline: none;
		border-color: var(--color-blue-500);
		box-shadow: 0 0 0 3px var(--color-blue-100);
	}

	.form-select:disabled,
	.form-input:disabled {
		background-color: var(--color-gray-50);
		cursor: not-allowed;
	}

	.calculator-actions {
		display: flex;
		justify-content: center;
		gap: 1rem;
	}

	.results-section {
		margin-bottom: 2rem;
		min-height: 120px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.price-result,
	.error-result,
	.empty-result {
		display: flex;
		align-items: center;
		gap: 1rem;
		padding: 1.5rem;
		border-radius: 0.5rem;
		width: 100%;
	}

	.price-result {
		background-color: var(--color-green-50);
		border: 1px solid var(--color-green-200);
	}

	.error-result {
		background-color: var(--color-red-50);
		border: 1px solid var(--color-red-200);
	}

	.empty-result {
		background-color: var(--color-gray-50);
		border: 1px solid var(--color-gray-200);
		justify-content: center;
		text-align: center;
		flex-direction: column;
	}

	.price-icon,
	.error-icon,
	.empty-icon {
		font-size: 2rem;
		flex-shrink: 0;
	}

	.price-content h4,
	.error-content h4 {
		margin: 0 0 0.5rem 0;
		font-size: 1.1rem;
		font-weight: 600;
	}

	.price-content h4 {
		color: var(--color-green-800);
	}

	.error-content h4 {
		color: var(--color-red-800);
	}

	.price-display {
		font-size: 2rem;
		font-weight: 700;
		color: var(--color-green-600);
		margin-bottom: 0.5rem;
	}

	.context-summary {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
		font-size: 0.875rem;
		color: var(--color-gray-600);
	}

	.context-item {
		background-color: var(--color-gray-100);
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
	}

	.error-content p {
		margin: 0;
		color: var(--color-red-700);
	}

	.empty-result p {
		margin: 0.5rem 0 0 0;
		color: var(--color-gray-600);
	}

	.context-explanation {
		border-top: 1px solid var(--color-border);
		padding-top: 1.5rem;
	}

	.context-explanation h4 {
		margin: 0 0 1rem 0;
		font-size: 1.1rem;
		font-weight: 600;
		text-align: center;
		color: var(--color-gray-700);
	}

	.explanation-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
		gap: 1rem;
	}

	.explanation-item {
		padding: 1rem;
		background-color: var(--color-blue-50);
		border-radius: 0.375rem;
		border: 1px solid var(--color-blue-100);
	}

	.explanation-item strong {
		display: block;
		margin-bottom: 0.5rem;
		color: var(--color-blue-800);
		font-size: 0.875rem;
	}

	.explanation-item p {
		margin: 0;
		font-size: 0.8rem;
		color: var(--color-blue-700);
		line-height: 1.4;
	}

	@media (max-width: 768px) {
		.form-grid {
			grid-template-columns: 1fr;
		}

		.calculator-actions {
			flex-direction: column;
		}

		.price-result,
		.error-result {
			flex-direction: column;
			text-align: center;
		}

		.price-display {
			font-size: 1.5rem;
		}

		.explanation-grid {
			grid-template-columns: 1fr;
		}
	}
</style>