<script lang="ts">
	import { DeleteCategoryStore } from '$houdini';
	import { notifications } from '$lib/stores/notifications';

	interface Props {
		isOpen: boolean;
		category: {
			id: string;
			title: string;
			breadCrumb: string;
			isLeaf: boolean;
		} | null;
		onClose: () => void;
		onDeleted: () => void;
	}

	let { isOpen, category, onClose, onDeleted }: Props = $props();

	const deleteCategory = new DeleteCategoryStore();
	let isDeleting = $state(false);

	async function handleDelete() {
		if (!category) return;
		
		isDeleting = true;
		
		try {
			const result = await deleteCategory.mutate({ id: category.id });
			
			if (result.data?.deleteCategory) {
				notifications.push({ type: 'success', message: `Category "${category.title}" deleted successfully` });
				onDeleted();
				onClose();
			}
		} catch (error: any) {
			console.error('Error deleting category:', error);
			
			// Handle specific error messages
			if (error.message?.includes('child categories')) {
				notifications.push({ type: 'error', message: 'Cannot delete category with child categories. Delete or move child categories first.' });
			} else {
				notifications.push({ type: 'error', message: 'Failed to delete category' });
			}
		} finally {
			isDeleting = false;
		}
	}

	// Close modal on escape key
	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			onClose();
		}
	}
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen && category}
	<!-- Modal backdrop -->
	<div 
		class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity z-50"
		onclick={onClose}
	></div>

	<!-- Modal -->
	<div class="fixed inset-0 z-50 overflow-y-auto">
		<div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
			<div 
				class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
				onclick={(e) => e.stopPropagation()}
			>
				<div class="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
					<div class="sm:flex sm:items-start">
						<div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
							<svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
							</svg>
						</div>
						<div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
							<h3 class="text-base font-semibold leading-6 text-gray-900">
								Delete Category
							</h3>
							<div class="mt-2">
								<p class="text-sm text-gray-500">
									Are you sure you want to delete the category "{category.title}"?
								</p>
								
								<div class="mt-3 p-3 bg-gray-50 rounded-md">
									<div class="text-sm text-gray-700">
										<p><strong>Path:</strong> {category.breadCrumb}</p>
										<p><strong>Type:</strong> {category.isLeaf ? 'Leaf Category' : 'Branch Category'}</p>
									</div>
								</div>

								{#if !category.isLeaf}
									<div class="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
										<div class="flex">
											<div class="flex-shrink-0">
												<svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
													<path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd" />
												</svg>
											</div>
											<div class="ml-3">
												<h4 class="text-sm font-medium text-yellow-800">
													Warning: Branch Category
												</h4>
												<div class="mt-1 text-sm text-yellow-700">
													<p>
														This category may have child categories. You can only delete categories that have no children.
													</p>
												</div>
											</div>
										</div>
									</div>
								{/if}

								<p class="mt-3 text-sm text-gray-500">
									This action cannot be undone.
								</p>
							</div>
						</div>
					</div>
				</div>
				<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
					<button
						type="button"
						onclick={handleDelete}
						disabled={isDeleting}
						class="inline-flex w-full justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 sm:ml-3 sm:w-auto disabled:opacity-50"
					>
						{isDeleting ? 'Deleting...' : 'Delete'}
					</button>
					<button
						type="button"
						onclick={onClose}
						disabled={isDeleting}
						class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
					>
						Cancel
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}