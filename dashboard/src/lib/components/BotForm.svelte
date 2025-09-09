<script lang="ts">
	interface Props {
		bot?: any | null;
		onCancel: () => void;
		isSubmitting?: boolean;
		errors?: Record<string, string[] | string> | null;
	}

	let {
		bot = null,
		onCancel,
		isSubmitting = false,
		errors = null
	}: Props = $props();

	let usernameVal = $state(bot?.username || '');
	let emailVal = $state(bot?.email || '');
	let appIdVal = $state(bot?.appId || '');
	let permissionsVal = $state(bot?.permissions?.join(', ') || '');

	$effect(() => {
		if (!errors) return;
		const order = ['username', 'email', 'appId', 'permissions'];
		for (const field of order) {
			if (errors[field]) {
				const el = document.getElementById(field);
				if (el) (el as HTMLElement).focus();
				break;
			}
		}
	});

	function getFieldError(field: string): string | null {
		if (!errors || !errors[field]) return null;
		const error = errors[field];
		return Array.isArray(error) ? error[0] : error;
	}
</script>

<form class="space-y-6" method="post">
	<!-- Username -->
	<div>
		<label for="username" class="block text-sm font-medium text-gray-700 mb-1">
			Username <span class="text-red-500">*</span>
		</label>
		<input
			id="username"
			name="username"
			type="text"
			bind:value={usernameVal}
			class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			class:border-red-500={getFieldError('username')}
			class:ring-red-500={getFieldError('username')}
			placeholder="Enter bot username"
			required
		/>
		{#if getFieldError('username')}
			<p class="mt-1 text-sm text-red-600">{getFieldError('username')}</p>
		{/if}
	</div>

	<!-- Email -->
	<div>
		<label for="email" class="block text-sm font-medium text-gray-700 mb-1">
			Email
		</label>
		<input
			id="email"
			name="email"
			type="email"
			bind:value={emailVal}
			class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			class:border-red-500={getFieldError('email')}
			class:ring-red-500={getFieldError('email')}
			placeholder="Enter email address"
		/>
		{#if getFieldError('email')}
			<p class="mt-1 text-sm text-red-600">{getFieldError('email')}</p>
		{/if}
	</div>

	<!-- App ID -->
	<div>
		<label for="appId" class="block text-sm font-medium text-gray-700 mb-1">
			App ID
		</label>
		<input
			id="appId"
			name="appId"
			type="text"
			bind:value={appIdVal}
			class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			class:border-red-500={getFieldError('appId')}
			class:ring-red-500={getFieldError('appId')}
			placeholder="Enter application ID"
		/>
		{#if getFieldError('appId')}
			<p class="mt-1 text-sm text-red-600">{getFieldError('appId')}</p>
		{/if}
	</div>

	<!-- Permissions -->
	<div>
		<label for="permissions" class="block text-sm font-medium text-gray-700 mb-1">
			Permissions <span class="text-red-500">*</span>
		</label>
		<textarea
			id="permissions"
			name="permissions"
			bind:value={permissionsVal}
			rows="4"
			class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			class:border-red-500={getFieldError('permissions')}
			class:ring-red-500={getFieldError('permissions')}
			placeholder="Enter permissions separated by commas (e.g., read:products, write:products, read:orders)"
			required
		></textarea>
		{#if getFieldError('permissions')}
			<p class="mt-1 text-sm text-red-600">{getFieldError('permissions')}</p>
		{/if}
		<p class="mt-1 text-sm text-gray-500">
			Enter permissions separated by commas. Common permissions include: read:products, write:products, read:orders, write:orders, read:customers, write:customers
		</p>
	</div>

	<!-- Form Actions -->
	<div class="flex justify-end space-x-3 pt-6">
		<button
			type="button"
			onclick={onCancel}
			class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500"
			disabled={isSubmitting}
		>
			Cancel
		</button>
		<button
			type="submit"
			class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
			disabled={isSubmitting}
		>
			{#if isSubmitting}
				<div class="flex items-center">
					<svg class="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
						<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
					</svg>
					{bot ? 'Updating...' : 'Creating...'}
				</div>
			{:else}
				{bot ? 'Update Bot' : 'Create Bot'}
			{/if}
		</button>
	</div>
</form>