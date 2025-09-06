<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { ProductStatus } from '../validation/pricing';
	import Button from './Button.svelte';
	import Modal from './Modal.svelte';
	import { toast } from '../stores/notifications';

	// Props
	export let productId: string;
	export let currentStatus: ProductStatus;
	export let productTitle: string = '';
	export let disabled = false;

	// Events
	const dispatch = createEventDispatcher<{
		statusChange: { productId: string; newStatus: ProductStatus };
		error: { message: string };
	}>();

	// Local state
	let showConfirmModal = false;
	let pendingAction: 'publish' | 'archive' | 'reactivate' | null = null;
	let isLoading = false;

	// Reactive statements
	$: canPublish = currentStatus === 'DRAFT';
	$: canArchive = currentStatus === 'PUBLISHED';
	$: canReactivate = currentStatus === 'ARCHIVED';

	// Status configuration
	const statusConfig = {
		DRAFT: {
			label: 'Draft',
			color: 'gray',
			icon: '📝',
			description: 'Product is being prepared and not visible to customers'
		},
		PUBLISHED: {
			label: 'Published',
			color: 'green',
			icon: '✅',
			description: 'Product is live and available to customers'
		},
		ARCHIVED: {
			label: 'Archived',
			color: 'red',
			icon: '📦',
			description: 'Product is no longer active but preserved for records'
		}
	};

	// Action configurations
	const actionConfig = {
		publish: {
			label: 'Publish Product',
			description: 'Make this product visible and available to customers',
			confirmText: 'Are you sure you want to publish this product? It will become visible to all customers.',
			successMessage: 'Product published successfully!',
			errorMessage: 'Failed to publish product',
			mutation: 'publishProduct'
		},
		archive: {
			label: 'Archive Product',
			description: 'Remove this product from public view while preserving data',
			confirmText: 'Are you sure you want to archive this product? It will no longer be visible to customers.',
			successMessage: 'Product archived successfully!',
			errorMessage: 'Failed to archive product',
			mutation: 'archiveProduct'
		},
		reactivate: {
			label: 'Reactivate Product',
			description: 'Move this product back to draft status for editing',
			confirmText: 'Are you sure you want to reactivate this product? It will be moved to draft status.',
			successMessage: 'Product reactivated successfully!',
			errorMessage: 'Failed to reactivate product',
			mutation: 'reactivateProduct'
		}
	};

	// Methods
	function handleAction(action: 'publish' | 'archive' | 'reactivate') {
		pendingAction = action;
		showConfirmModal = true;
	}

	async function confirmAction() {
		if (!pendingAction) return;

		const action = actionConfig[pendingAction];
		isLoading = true;
		showConfirmModal = false;

		try {
			// Call GraphQL mutation
			const response = await callGraphQLMutation(action.mutation, { id: productId });
			
			if (response.errors) {
				throw new Error(response.errors[0].message);
			}

			// Update status based on action
			const newStatus = getNewStatus(pendingAction);
			dispatch('statusChange', { productId, newStatus });
			toast.success(action.successMessage);

		} catch (error: any) {
			console.error(`Error ${pendingAction}ing product:`, error);
			dispatch('error', { message: error.message || action.errorMessage });
			toast.error(action.errorMessage);
		} finally {
			isLoading = false;
			pendingAction = null;
		}
	}

	function cancelAction() {
		showConfirmModal = false;
		pendingAction = null;
	}

	function getNewStatus(action: 'publish' | 'archive' | 'reactivate'): ProductStatus {
		switch (action) {
			case 'publish': return 'PUBLISHED';
			case 'archive': return 'ARCHIVED';
			case 'reactivate': return 'DRAFT';
			default: return currentStatus;
		}
	}

	async function callGraphQLMutation(mutation: string, variables: any) {
		const query = `
			mutation ${mutation}($id: ID!) {
				${mutation}(id: $id) {
					id
					status
					title
				}
			}
		`;

		const response = await fetch('/graphql', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
				query,
				variables
			})
		});

		return await response.json();
	}

	// Status transition validation messages
	function getValidationMessage(action: 'publish' | 'archive' | 'reactivate'): string | null {
		switch (action) {
			case 'publish':
				// In a real app, you'd check if product has variants, pricing, etc.
				return 'Ensure product has at least one variant with pricing before publishing.';
			case 'archive':
				return 'Consider notifying customers who have this product in their cart or wishlist.';
			case 'reactivate':
				return 'Product will need to be reviewed and updated before publishing again.';
			default:
				return null;
		}
	}
</script>

<div class="product-status-manager">
	<div class="current-status">
		<div class="status-indicator" class:draft={currentStatus === 'DRAFT'} class:published={currentStatus === 'PUBLISHED'} class:archived={currentStatus === 'ARCHIVED'}>
			<span class="status-icon">{statusConfig[currentStatus].icon}</span>
			<div class="status-info">
				<span class="status-label">{statusConfig[currentStatus].label}</span>
				<span class="status-description">{statusConfig[currentStatus].description}</span>
			</div>
		</div>
	</div>

	<div class="actions">
		{#if canPublish}
			<Button 
				variant="success" 
				on:click={() => handleAction('publish')} 
				{disabled}
				loading={isLoading && pendingAction === 'publish'}
			>
				📤 Publish
			</Button>
		{/if}

		{#if canArchive}
			<Button 
				variant="warning" 
				on:click={() => handleAction('archive')} 
				{disabled}
				loading={isLoading && pendingAction === 'archive'}
			>
				📦 Archive
			</Button>
		{/if}

		{#if canReactivate}
			<Button 
				variant="primary" 
				on:click={() => handleAction('reactivate')} 
				{disabled}
				loading={isLoading && pendingAction === 'reactivate'}
			>
				🔄 Reactivate
			</Button>
		{/if}
	</div>

	<!-- State Transition Timeline -->
	<div class="transition-timeline">
		<h4>Product Lifecycle</h4>
		<div class="timeline">
			<div class="timeline-item" class:active={currentStatus === 'DRAFT'} class:completed={currentStatus !== 'DRAFT'}>
				<div class="timeline-marker">📝</div>
				<div class="timeline-content">
					<span class="timeline-label">Draft</span>
					<span class="timeline-description">Product creation and editing</span>
				</div>
			</div>
			
			<div class="timeline-connector" class:completed={currentStatus === 'PUBLISHED' || currentStatus === 'ARCHIVED'}></div>
			
			<div class="timeline-item" class:active={currentStatus === 'PUBLISHED'} class:completed={currentStatus === 'ARCHIVED'}>
				<div class="timeline-marker">✅</div>
				<div class="timeline-content">
					<span class="timeline-label">Published</span>
					<span class="timeline-description">Live and available to customers</span>
				</div>
			</div>
			
			<div class="timeline-connector" class:completed={currentStatus === 'ARCHIVED'}></div>
			
			<div class="timeline-item" class:active={currentStatus === 'ARCHIVED'}>
				<div class="timeline-marker">📦</div>
				<div class="timeline-content">
					<span class="timeline-label">Archived</span>
					<span class="timeline-description">Preserved but not active</span>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- Confirmation Modal -->
{#if showConfirmModal && pendingAction}
	<Modal title="Confirm Action" on:close={cancelAction}>
		<div class="confirmation-content">
			<div class="confirmation-icon">
				{#if pendingAction === 'publish'}🚀
				{:else if pendingAction === 'archive'}⚠️
				{:else if pendingAction === 'reactivate'}🔄{/if}
			</div>
			
			<h3>{actionConfig[pendingAction].label}</h3>
			<p class="product-title">"{productTitle}"</p>
			
			<p class="confirmation-text">{actionConfig[pendingAction].confirmText}</p>
			
			{#if getValidationMessage(pendingAction)}
				<div class="validation-warning">
					<strong>Note:</strong> {getValidationMessage(pendingAction)}
				</div>
			{/if}
		</div>

		<div slot="actions">
			<Button variant="secondary" on:click={cancelAction} {disabled}>
				Cancel
			</Button>
			<Button 
				variant={pendingAction === 'archive' ? 'danger' : 'primary'} 
				on:click={confirmAction}
				loading={isLoading}
				disabled={disabled || isLoading}
			>
				{actionConfig[pendingAction].label}
			</Button>
		</div>
	</Modal>
{/if}

<style>
	.product-status-manager {
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
		padding: 1rem;
		border: 1px solid var(--color-border);
		border-radius: 0.5rem;
		background-color: var(--color-white);
	}

	.current-status {
		display: flex;
		align-items: center;
	}

	.status-indicator {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 1rem;
		border-radius: 0.5rem;
		width: 100%;
	}

	.status-indicator.draft {
		background-color: var(--color-gray-50);
		border: 1px solid var(--color-gray-200);
	}

	.status-indicator.published {
		background-color: var(--color-green-50);
		border: 1px solid var(--color-green-200);
	}

	.status-indicator.archived {
		background-color: var(--color-red-50);
		border: 1px solid var(--color-red-200);
	}

	.status-icon {
		font-size: 1.5rem;
	}

	.status-info {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.status-label {
		font-weight: 600;
		font-size: 1.1rem;
	}

	.status-description {
		font-size: 0.875rem;
		color: var(--color-gray-600);
	}

	.actions {
		display: flex;
		gap: 0.75rem;
		flex-wrap: wrap;
	}

	.transition-timeline {
		border-top: 1px solid var(--color-border);
		padding-top: 1rem;
	}

	.transition-timeline h4 {
		margin: 0 0 1rem 0;
		font-size: 1rem;
		font-weight: 600;
		color: var(--color-gray-700);
	}

	.timeline {
		display: flex;
		align-items: center;
		gap: 0;
	}

	.timeline-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 0.5rem;
		flex: 1;
		text-align: center;
		opacity: 0.5;
		transition: opacity 0.2s;
	}

	.timeline-item.active {
		opacity: 1;
		transform: scale(1.05);
	}

	.timeline-item.completed {
		opacity: 0.8;
	}

	.timeline-marker {
		width: 2.5rem;
		height: 2.5rem;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: var(--color-gray-100);
		border: 2px solid var(--color-gray-300);
		font-size: 1.2rem;
		transition: all 0.2s;
	}

	.timeline-item.active .timeline-marker {
		background-color: var(--color-blue-100);
		border-color: var(--color-blue-500);
		box-shadow: 0 4px 12px var(--color-blue-200);
	}

	.timeline-item.completed .timeline-marker {
		background-color: var(--color-green-100);
		border-color: var(--color-green-500);
	}

	.timeline-content {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.timeline-label {
		font-weight: 600;
		font-size: 0.875rem;
	}

	.timeline-description {
		font-size: 0.75rem;
		color: var(--color-gray-500);
	}

	.timeline-connector {
		height: 2px;
		background-color: var(--color-gray-200);
		flex: 1;
		margin: 0 -0.5rem;
		transition: background-color 0.2s;
	}

	.timeline-connector.completed {
		background-color: var(--color-green-400);
	}

	.confirmation-content {
		text-align: center;
		padding: 1rem;
	}

	.confirmation-icon {
		font-size: 3rem;
		margin-bottom: 1rem;
	}

	.confirmation-content h3 {
		margin: 0 0 0.5rem 0;
		font-size: 1.25rem;
		font-weight: 600;
	}

	.product-title {
		font-style: italic;
		color: var(--color-gray-600);
		margin-bottom: 1rem;
	}

	.confirmation-text {
		margin-bottom: 1rem;
		line-height: 1.5;
	}

	.validation-warning {
		background-color: var(--color-yellow-50);
		border: 1px solid var(--color-yellow-200);
		border-radius: 0.375rem;
		padding: 0.75rem;
		margin-top: 1rem;
		text-align: left;
	}

	.validation-warning strong {
		color: var(--color-yellow-800);
	}

	@media (max-width: 768px) {
		.timeline {
			flex-direction: column;
			gap: 1rem;
		}

		.timeline-connector {
			width: 2px;
			height: 1rem;
			margin: -0.5rem 0;
		}

		.timeline-item {
			flex-direction: row;
			text-align: left;
			width: 100%;
		}

		.actions {
			flex-direction: column;
		}
	}
</style>