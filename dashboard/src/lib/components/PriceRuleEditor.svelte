<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PriceRuleInput } from '../validation/pricing';
	import { priceRuleInputSchema, priceContextTypes, formatMoney } from '../validation/pricing';
	import Button from './Button.svelte';
	import FormField from './FormField.svelte';

	// Props
	export let rule: Partial<PriceRuleInput> = {
		contextType: 'DEFAULT',
		contextValue: '',
		price: { amount: 0, currency: 'USD' },
		minQuantity: undefined,
		maxQuantity: undefined,
		validFrom: undefined,
		validUntil: undefined,
		active: true
	};
	export let disabled = false;

	// Events
	const dispatch = createEventDispatcher<{
		change: PriceRuleInput;
		delete: void;
	}>();

	// Local state
	let validationErrors: Record<string, string> = {};
	let showAdvanced = false;

	// Reactive statements
	$: contextTypeChanged(rule.contextType);
	$: validateAndEmit(rule);

	// Methods
	function contextTypeChanged(contextType: string | undefined) {
		if (contextType === 'DEFAULT') {
			rule.contextValue = undefined;
		}
	}

	function validateAndEmit(currentRule: Partial<PriceRuleInput>) {
		try {
			const validRule = priceRuleInputSchema.parse(currentRule);
			validationErrors = {};
			dispatch('change', validRule);
		} catch (error: any) {
			if (error.errors) {
				validationErrors = error.errors.reduce((acc: any, err: any) => {
					const path = err.path.join('.');
					acc[path] = err.message;
					return acc;
				}, {});
			}
		}
	}

	function handleDelete() {
		dispatch('delete');
	}

	function getContextPlaceholder(contextType: string | undefined): string {
		switch (contextType) {
			case 'GEOGRAPHIC':
				return 'e.g., US, EU, UK, CA';
			case 'CURRENCY':
				return 'e.g., USD, EUR, GBP';
			case 'CUSTOMER_GROUP':
				return 'e.g., B2B, B2C, VIP, WHOLESALE';
			default:
				return '';
		}
	}

	function getContextLabel(contextType: string | undefined): string {
		switch (contextType) {
			case 'GEOGRAPHIC':
				return 'Region/Country Code';
			case 'CURRENCY':
				return 'Currency Code';
			case 'CUSTOMER_GROUP':
				return 'Customer Group';
			default:
				return 'Context Value';
		}
	}

	// Currency options (common ones)
	const currencies = ['USD', 'EUR', 'GBP', 'CAD', 'AUD', 'JPY', 'CHF', 'SEK', 'NOK', 'DKK'];
	const customerGroups = ['B2B', 'B2C', 'VIP', 'WHOLESALE', 'RETAIL'];
</script>

<div class="price-rule-editor" class:has-errors={Object.keys(validationErrors).length > 0}>
	<div class="rule-header">
		<div class="rule-type">
			<span class="context-badge" class:default={rule.contextType === 'DEFAULT'}>
				{rule.contextType || 'DEFAULT'}
			</span>
			{#if rule.contextValue}
				<span class="context-value">{rule.contextValue}</span>
			{/if}
		</div>
		
		<div class="rule-actions">
			<button 
				class="toggle-advanced"
				on:click={() => showAdvanced = !showAdvanced}
				type="button"
				{disabled}
			>
				{showAdvanced ? '▼' : '▶'}
			</button>
			<Button variant="danger" size="xs" on:click={handleDelete} {disabled}>
				×
			</Button>
		</div>
	</div>

	<div class="rule-content">
		<!-- Main Rule Configuration -->
		<div class="main-config">
			<div class="config-row">
				<FormField 
					label="Context Type" 
					error={validationErrors['contextType']}
				>
					<select 
						bind:value={rule.contextType} 
						{disabled}
						class="form-select"
					>
						{#each priceContextTypes as contextType}
							<option value={contextType}>
								{contextType.replace('_', ' ')}
							</option>
						{/each}
					</select>
				</FormField>

				{#if rule.contextType !== 'DEFAULT'}
					<FormField 
						label={getContextLabel(rule.contextType)}
						error={validationErrors['contextValue']}
					>
						{#if rule.contextType === 'CUSTOMER_GROUP'}
							<select 
								bind:value={rule.contextValue} 
								{disabled}
								class="form-select"
							>
								<option value="">Select group...</option>
								{#each customerGroups as group}
									<option value={group}>{group}</option>
								{/each}
							</select>
						{:else}
							<input
								type="text"
								bind:value={rule.contextValue}
								placeholder={getContextPlaceholder(rule.contextType)}
								{disabled}
								class="form-input"
								class:error={validationErrors['contextValue']}
							/>
						{/if}
					</FormField>
				{/if}
			</div>

			<div class="config-row">
				<FormField 
					label="Price Amount" 
					error={validationErrors['price.amount']}
				>
					<input
						type="number"
						bind:value={rule.price.amount}
						min="0"
						step="0.01"
						placeholder="0.00"
						{disabled}
						class="form-input"
						class:error={validationErrors['price.amount']}
					/>
				</FormField>

				<FormField 
					label="Currency" 
					error={validationErrors['price.currency']}
				>
					<select 
						bind:value={rule.price.currency} 
						{disabled}
						class="form-select"
						class:error={validationErrors['price.currency']}
					>
						{#each currencies as currency}
							<option value={currency}>{currency}</option>
						{/each}
					</select>
				</FormField>

				<div class="price-preview">
					{#if rule.price?.amount && rule.price?.currency}
						<span class="formatted-price">
							{formatMoney(rule.price.amount, rule.price.currency)}
						</span>
					{:else}
						<span class="no-price">No price set</span>
					{/if}
				</div>
			</div>
		</div>

		<!-- Advanced Configuration -->
		{#if showAdvanced}
			<div class="advanced-config">
				<div class="config-section">
					<h5>Quantity Range</h5>
					<div class="config-row">
						<FormField 
							label="Min Quantity" 
							error={validationErrors['minQuantity']}
							help="Leave empty for no minimum"
						>
							<input
								type="number"
								bind:value={rule.minQuantity}
								min="1"
								placeholder="No minimum"
								{disabled}
								class="form-input"
							/>
						</FormField>

						<FormField 
							label="Max Quantity" 
							error={validationErrors['maxQuantity']}
							help="Leave empty for no maximum"
						>
							<input
								type="number"
								bind:value={rule.maxQuantity}
								min="1"
								placeholder="No maximum"
								{disabled}
								class="form-input"
							/>
						</FormField>
					</div>
				</div>

				<div class="config-section">
					<h5>Time Range</h5>
					<div class="config-row">
						<FormField 
							label="Valid From" 
							error={validationErrors['validFrom']}
							help="Leave empty for no start date"
						>
							<input
								type="datetime-local"
								bind:value={rule.validFrom}
								{disabled}
								class="form-input"
							/>
						</FormField>

						<FormField 
							label="Valid Until" 
							error={validationErrors['validUntil']}
							help="Leave empty for no end date"
						>
							<input
								type="datetime-local"
								bind:value={rule.validUntil}
								{disabled}
								class="form-input"
							/>
						</FormField>
					</div>
				</div>

				<div class="config-section">
					<FormField label="Status">
						<label class="checkbox-label">
							<input
								type="checkbox"
								bind:checked={rule.active}
								{disabled}
							/>
							Active
						</label>
					</FormField>
				</div>
			</div>
		{/if}

		<!-- Validation Errors -->
		{#if Object.keys(validationErrors).length > 0}
			<div class="rule-errors">
				<h6>Validation Errors:</h6>
				<ul>
					{#each Object.entries(validationErrors) as [path, message]}
						<li>{message}</li>
					{/each}
				</ul>
			</div>
		{/if}
	</div>
</div>

<style>
	.price-rule-editor {
		border: 1px solid var(--color-border);
		border-radius: 0.5rem;
		padding: 1rem;
		background-color: var(--color-white);
		transition: border-color 0.2s;
	}

	.price-rule-editor.has-errors {
		border-color: var(--color-red-300);
		background-color: var(--color-red-50);
	}

	.rule-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1rem;
	}

	.rule-type {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.context-badge {
		background-color: var(--color-blue-100);
		color: var(--color-blue-800);
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
		font-size: 0.75rem;
		font-weight: 600;
		text-transform: uppercase;
	}

	.context-badge.default {
		background-color: var(--color-gray-100);
		color: var(--color-gray-700);
	}

	.context-value {
		font-size: 0.875rem;
		font-weight: 500;
		color: var(--color-gray-700);
	}

	.rule-actions {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.toggle-advanced {
		background: none;
		border: none;
		color: var(--color-gray-500);
		cursor: pointer;
		padding: 0.25rem;
		font-size: 0.875rem;
	}

	.toggle-advanced:hover {
		color: var(--color-gray-700);
	}

	.rule-content {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.main-config,
	.advanced-config {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.config-row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
		gap: 1rem;
		align-items: end;
	}

	.config-section {
		border-top: 1px solid var(--color-border);
		padding-top: 1rem;
	}

	.config-section h5,
	.config-section h6 {
		margin: 0 0 0.75rem 0;
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--color-gray-700);
	}

	.form-input,
	.form-select {
		width: 100%;
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		font-size: 0.875rem;
		transition: border-color 0.2s;
	}

	.form-input:focus,
	.form-select:focus {
		outline: none;
		border-color: var(--color-blue-500);
		box-shadow: 0 0 0 3px var(--color-blue-100);
	}

	.form-input.error,
	.form-select.error {
		border-color: var(--color-red-500);
	}

	.form-input:disabled,
	.form-select:disabled {
		background-color: var(--color-gray-50);
		cursor: not-allowed;
	}

	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
		font-size: 0.875rem;
	}

	.price-preview {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.5rem;
		background-color: var(--color-gray-50);
		border-radius: 0.375rem;
		min-height: 2.5rem;
	}

	.formatted-price {
		font-size: 1rem;
		font-weight: 600;
		color: var(--color-green-600);
	}

	.no-price {
		font-size: 0.875rem;
		color: var(--color-gray-500);
		font-style: italic;
	}

	.rule-errors {
		background-color: var(--color-red-50);
		border: 1px solid var(--color-red-200);
		border-radius: 0.375rem;
		padding: 0.75rem;
		margin-top: 0.5rem;
	}

	.rule-errors h6 {
		color: var(--color-red-800);
		margin-bottom: 0.5rem;
	}

	.rule-errors ul {
		margin: 0;
		padding-left: 1.25rem;
	}

	.rule-errors li {
		color: var(--color-red-700);
		font-size: 0.75rem;
		margin-bottom: 0.25rem;
	}

	@media (max-width: 768px) {
		.config-row {
			grid-template-columns: 1fr;
		}
		
		.rule-header {
			flex-direction: column;
			gap: 0.5rem;
			align-items: stretch;
		}
		
		.rule-actions {
			justify-content: flex-end;
		}
	}
</style>