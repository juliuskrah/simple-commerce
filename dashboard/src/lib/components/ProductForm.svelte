<script lang="ts">
	import { CategoriesStore } from '$houdini';

	interface Props {
		product?: any | null;
		onCancel: () => void;
		isSubmitting?: boolean;
		errors?: Record<string, string[] | string> | null;
	}

	let {
		product = null,
		onCancel,
		isSubmitting = false,
		errors = null
	}: Props = $props();

	// Load categories from database
	const categories = new CategoriesStore();
	categories.fetch();

	let titleVal = $state(product?.title || '');
	let descriptionVal = $state(product?.description || '');
	let priceVal = $state(product?.price || 0);
	let tagsVal = $state(product?.tags?.join(', ') || '');
	let statusVal = $state(product?.status || 'DRAFT');

	$effect(() => {
		if (!errors) return;
		const order = ['title', 'description', 'price', 'tags', 'status'];
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
<form method="POST" action={product ? '?/update' : '?/create'} class="space-y-6" novalidate>
	{#if errors && (errors.title || errors.description || errors.price || errors.tags || errors.status)}
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
				{#if errors.description}
					<li>
						<a href="#description" class="underline"
							>Description: {Array.isArray(errors.description) ? errors.description[0] : errors.description}</a
						>
					</li>
				{/if}
				{#if errors.price}
					<li>
						<a href="#price" class="underline"
							>Price: {Array.isArray(errors.price) ? errors.price[0] : errors.price}</a
						>
					</li>
				{/if}
				{#if errors.tags}
					<li>
						<a href="#tags" class="underline"
							>Tags: {Array.isArray(errors.tags) ? errors.tags[0] : errors.tags}</a
						>
					</li>
				{/if}
				{#if errors.status}
					<li>
						<a href="#status" class="underline"
							>Status: {Array.isArray(errors.status) ? errors.status[0] : errors.status}</a
						>
					</li>
				{/if}
			</ul>
		</div>
	{/if}

	<!-- Basic Information -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Basic Information</h3>
		<div class="space-y-4">
			<div>
				<label for="title" class="block text-sm font-medium text-gray-700">Product Title *</label>
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
					placeholder="e.g., Apple MacBook Pro 16-inch"
				/>
				{#if errors?.title}
					<p id="title-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.title) ? errors.title[0] : errors.title}
					</p>
				{/if}
			</div>

			<div>
				<label for="description" class="block text-sm font-medium text-gray-700">Description</label>
				<textarea
					id="description"
					name="description"
					bind:value={descriptionVal}
					rows="4"
					aria-invalid={errors?.description ? 'true' : 'false'}
					aria-describedby={errors?.description ? 'description-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="Describe your product features, benefits, and specifications..."
				></textarea>
				{#if errors?.description}
					<p id="description-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.description) ? errors.description[0] : errors.description}
					</p>
				{/if}
			</div>

			<div>
				<label for="tags" class="block text-sm font-medium text-gray-700">Tags</label>
				<input
					type="text"
					id="tags"
					name="tags"
					bind:value={tagsVal}
					aria-invalid={errors?.tags ? 'true' : 'false'}
					aria-describedby={errors?.tags ? 'tags-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="e.g., electronics, laptop, apple (comma-separated)"
				/>
				<p class="mt-1 text-xs text-gray-500">Separate tags with commas</p>
				{#if errors?.tags}
					<p id="tags-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.tags) ? errors.tags[0] : errors.tags}
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
				<label for="price" class="block text-sm font-medium text-gray-700">Base Price</label>
				<input
					type="number"
					id="price"
					name="price"
					bind:value={priceVal}
					step="0.01"
					min="0"
					aria-invalid={errors?.price ? 'true' : 'false'}
					aria-describedby={errors?.price ? 'price-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
					placeholder="0.00"
				/>
				<p class="mt-1 text-xs text-gray-500">This will be used for the default variant</p>
				{#if errors?.price}
					<p id="price-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.price) ? errors.price[0] : errors.price}
					</p>
				{/if}
			</div>

			<div>
				<label for="status" class="block text-sm font-medium text-gray-700">Status</label>
				<select
					id="status"
					name="status"
					bind:value={statusVal}
					aria-invalid={errors?.status ? 'true' : 'false'}
					aria-describedby={errors?.status ? 'status-error' : undefined}
					class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-primary-500"
				>
					<option value="DRAFT">Draft</option>
					<option value="PUBLISHED">Published</option>
					<option value="ARCHIVED">Archived</option>
				</select>
				{#if errors?.status}
					<p id="status-error" class="mt-1 text-xs text-red-600">
						{Array.isArray(errors.status) ? errors.status[0] : errors.status}
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
			{isSubmitting ? 'Saving...' : product ? 'Update Product' : 'Create Product'}
		</button>
	</div>
</form>