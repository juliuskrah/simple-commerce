<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PriceSetInput, PriceRuleInput, PriceContextInput } from '../validation/pricing';
	import { priceSetInputSchema, formatMoney } from '../validation/pricing';
	import PriceRuleEditor from './PriceRuleEditor.svelte';
	import Button from './Button.svelte';
	import Card from './Card.svelte';
	import FormField from './FormField.svelte';
	import { toast } from '../stores/notifications';

	// Props
	export let priceSet: Partial<PriceSetInput> = {
		name: '',
		priority: 0,
		active: true,
		rules: []
	};
	export let editMode = false;
	export let disabled = false;

	// Events
	const dispatch = createEventDispatcher<{
		save: PriceSetInput;
		cancel: void;
		delete: void;
	}>();

	// Local state
	let validationErrors: Record<string, string> = {};
	let showAdvanced = false;
	let isDirty = false;

	// Reactive statements
	$: hasDefaultRule = priceSet.rules?.some(rule => rule.contextType === 'DEFAULT') ?? false;
	$: canSave = priceSet.name && priceSet.rules && priceSet.rules.length > 0 && hasDefaultRule && !disabled;

	// Methods
	function validatePriceSet(): boolean {
		try {
			priceSetInputSchema.parse(priceSet);
			validationErrors = {};
			return true;
		} catch (error: any) {
			if (error.errors) {
				validationErrors = error.errors.reduce((acc: any, err: any) => {
					const path = err.path.join('.');
					acc[path] = err.message;
					return acc;
				}, {});
			}
			return false;
		}
	}

	function handleSave() {
		if (validatePriceSet()) {
			dispatch('save', priceSet as PriceSetInput);
			isDirty = false;
		} else {
			toast.error('Please fix validation errors before saving');
		}
	}

	function handleCancel() {
		if (isDirty) {
			if (confirm('You have unsaved changes. Are you sure you want to cancel?')) {
				dispatch('cancel');
				isDirty = false;
			}
		} else {
			dispatch('cancel');
		}
	}

	function handleDelete() {
		if (confirm('Are you sure you want to delete this price set? This action cannot be undone.')) {
			dispatch('delete');
		}
	}

	function addRule() {
		const newRule: Partial<PriceRuleInput> = {
			contextType: 'DEFAULT',
			contextValue: '',
			price: { amount: 0, currency: 'USD' },
			active: true
		};
		
		priceSet.rules = [...(priceSet.rules || []), newRule as PriceRuleInput];
		isDirty = true;
	}

	function removeRule(index: number) {
		if (priceSet.rules) {
			priceSet.rules = priceSet.rules.filter((_, i) => i !== index);
			isDirty = true;
		}
	}

	function handleRuleChange(index: number, rule: PriceRuleInput) {
		if (priceSet.rules) {
			priceSet.rules[index] = rule;
			isDirty = true;
		}
	}

	function handleInputChange() {
		isDirty = true;
	}

	// Preview pricing for different contexts
	function getPreviewPrice(context: PriceContextInput): string {
		if (!priceSet.rules) return 'N/A';
		
		// Find matching rule (simplified logic)
		const matchingRule = priceSet.rules.find(rule => 
			rule.contextType === 'DEFAULT' || 
			(rule.contextType === 'CURRENCY' && rule.contextValue === context.currency) ||
			(rule.contextType === 'CUSTOMER_GROUP' && rule.contextValue === context.customerGroup) ||
			(rule.contextType === 'GEOGRAPHIC' && rule.contextValue === context.region)
		);
		
		if (matchingRule) {
			return formatMoney(matchingRule.price.amount, matchingRule.price.currency);
		}
		
		return 'No matching rule';
	}
</script>

<Card class="price-set-manager">
	<div class="header">
		<h3>{editMode ? 'Edit Price Set' : 'Create Price Set'}</h3>
		<div class="actions">
			{#if editMode}
				<Button variant="danger" size="sm" on:click={handleDelete} {disabled}>
					Delete
				</Button>
			{/if}
			<Button variant="secondary" size="sm" on:click={handleCancel} {disabled}>
				Cancel
			</Button>
			<Button variant="primary" size="sm" on:click={handleSave} disabled={!canSave}>
				{editMode ? 'Update' : 'Create'}
			</Button>
		</div>
	</div>

	<div class="content">
		<!-- Basic Information -->
		<div class="basic-info">
			<FormField 
				label="Price Set Name" 
				error={validationErrors['name']}
			>
				<input
					type="text"
					bind:value={priceSet.name}
					on:input={handleInputChange}
					placeholder="e.g., B2B Pricing, Holiday Sale, Geographic Pricing"
					{disabled}
					class="form-input"
				/>
			</FormField>

			<div class="form-row">
				<FormField 
					label="Priority" 
					error={validationErrors['priority']}
					help="Higher priority price sets are evaluated first (0 = lowest)"
				>
					<input
						type="number"
						bind:value={priceSet.priority}
						on:input={handleInputChange}
						min="0"
						max="100"
						{disabled}
						class="form-input"
					/>
				</FormField>

				<FormField label="Status">
					<label class="checkbox-label">
						<input
							type="checkbox"
							bind:checked={priceSet.active}
							on:change={handleInputChange}
							{disabled}
						/>
						Active
					</label>
				</FormField>
			</div>
		</div>

		<!-- Pricing Rules -->
		<div class="rules-section">
			<div class="section-header">
				<h4>Pricing Rules</h4>
				<Button variant="secondary" size="sm" on:click={addRule} {disabled}>
					+ Add Rule
				</Button>
			</div>

			{#if !hasDefaultRule}
				<div class="warning">
					⚠️ At least one DEFAULT rule is required as a fallback
				</div>
			{/if}

			{#if priceSet.rules && priceSet.rules.length > 0}
				<div class="rules-list">
					{#each priceSet.rules as rule, index}
						<PriceRuleEditor
							bind:rule
							{disabled}
							on:change={(e) => handleRuleChange(index, e.detail)}
							on:delete={() => removeRule(index)}
						/>
					{/each}
				</div>
			{:else}
				<div class="empty-state">
					<p>No pricing rules defined.</p>
					<p>Add at least one DEFAULT rule to get started.</p>
				</div>
			{/if}
		</div>

		<!-- Advanced Options -->
		<div class="advanced-section">
			<button 
				class="toggle-advanced"
				on:click={() => showAdvanced = !showAdvanced}
				type="button"
			>
				{showAdvanced ? '▼' : '▶'} Advanced Options
			</button>

			{#if showAdvanced}
				<div class="advanced-content">
					<!-- Price Preview -->
					<div class="price-preview">
						<h5>Price Preview</h5>
						<div class="preview-grid">
							<div class="preview-item">
								<span class="context">B2C Customer (US)</span>
								<span class="price">{getPreviewPrice({ currency: 'USD', customerGroup: 'B2C', region: 'US', quantity: 1 })}</span>
							</div>
							<div class="preview-item">
								<span class="context">B2B Customer (EU)</span>
								<span class="price">{getPreviewPrice({ currency: 'EUR', customerGroup: 'B2B', region: 'EU', quantity: 1 })}</span>
							</div>
							<div class="preview-item">
								<span class="context">Default</span>
								<span class="price">{getPreviewPrice({ currency: 'USD', quantity: 1 })}</span>
							</div>
						</div>
					</div>

					<!-- Validation Summary -->
					{#if Object.keys(validationErrors).length > 0}
						<div class="validation-errors">
							<h5>Validation Errors</h5>
							<ul>
								{#each Object.entries(validationErrors) as [path, message]}
									<li>{path}: {message}</li>
								{/each}
							</ul>
						</div>
					{/if}
				</div>
			{/if}
		</div>
	</div>
</Card>

<style>
	.price-set-manager {
		max-width: 800px;
		margin: 0 auto;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;
		padding-bottom: 1rem;
		border-bottom: 1px solid var(--color-border);
	}

	.header h3 {
		margin: 0;
		font-size: 1.25rem;
		font-weight: 600;
	}

	.actions {
		display: flex;
		gap: 0.5rem;
	}

	.content {
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.basic-info {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.form-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
	}

	.form-input {
		width: 100%;
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		font-size: 0.875rem;
	}

	.form-input:disabled {
		background-color: var(--color-gray-50);
		cursor: not-allowed;
	}

	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
	}

	.rules-section {
		border: 1px solid var(--color-border);
		border-radius: 0.5rem;
		padding: 1rem;
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1rem;
	}

	.section-header h4 {
		margin: 0;
		font-size: 1.1rem;
		font-weight: 600;
	}

	.warning {
		background-color: var(--color-yellow-50);
		border: 1px solid var(--color-yellow-200);
		border-radius: 0.375rem;
		padding: 0.75rem;
		margin-bottom: 1rem;
		color: var(--color-yellow-800);
		font-size: 0.875rem;
	}

	.rules-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.empty-state {
		text-align: center;
		padding: 2rem;
		color: var(--color-gray-500);
	}

	.empty-state p {
		margin: 0.5rem 0;
	}

	.advanced-section {
		border-top: 1px solid var(--color-border);
		padding-top: 1rem;
	}

	.toggle-advanced {
		background: none;
		border: none;
		font-size: 0.875rem;
		color: var(--color-blue-600);
		cursor: pointer;
		padding: 0.5rem 0;
	}

	.toggle-advanced:hover {
		color: var(--color-blue-800);
	}

	.advanced-content {
		margin-top: 1rem;
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
	}

	.price-preview h5,
	.validation-errors h5 {
		margin: 0 0 0.75rem 0;
		font-size: 1rem;
		font-weight: 600;
	}

	.preview-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 0.75rem;
	}

	.preview-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0.75rem;
		background-color: var(--color-gray-50);
		border-radius: 0.375rem;
		font-size: 0.875rem;
	}

	.context {
		font-weight: 500;
	}

	.price {
		font-weight: 600;
		color: var(--color-green-600);
	}

	.validation-errors {
		background-color: var(--color-red-50);
		border: 1px solid var(--color-red-200);
		border-radius: 0.375rem;
		padding: 1rem;
	}

	.validation-errors ul {
		margin: 0;
		padding-left: 1.5rem;
	}

	.validation-errors li {
		color: var(--color-red-700);
		font-size: 0.875rem;
		margin-bottom: 0.25rem;
	}

	@media (max-width: 768px) {
		.form-row {
			grid-template-columns: 1fr;
		}
		
		.header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}
		
		.actions {
			justify-content: flex-end;
		}
	}
</style>