<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import StaffForm from '$lib/components/StaffForm.svelte';
	let { data } = $props();
	const user = $derived(data.user);

	// TODO: Load staff from GraphQL when backend is ready
	const mockStaffStore = $state({
		fetching: false,
		errors: null,
		data: {
			staff: {
				edges: [],
				pageInfo: {
					hasNextPage: false,
					hasPreviousPage: false
				}
			}
		}
	});

	// State management
	let showAddModal = $state(false);
	let selectedStaff: any = $state(null);
	let showDetailsModal = $state(false);

	function showAddStaff() {
		selectedStaff = null;
		showAddModal = true;
	}

	function showStaffDetails(staff: any) {
		selectedStaff = staff;
		showDetailsModal = true;
	}

	function closeModals() {
		showAddModal = false;
		showDetailsModal = false;
		selectedStaff = null;
	}

	function handleStaffSubmit() {
		// TODO: Implement staff creation/update
		closeModals();
		// Refresh the staff list
		// TODO: Implement staff refresh when GraphQL is ready
	}
</script>

<DashboardLayout title="Staff" {user}>
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-2xl font-semibold text-gray-800">Staff Members</h1>
		<button
			onclick={showAddStaff}
			class="flex items-center rounded-lg bg-primary-600 px-4 py-2 text-white hover:bg-primary-700"
		>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				class="mr-2 h-5 w-5"
				viewBox="0 0 20 20"
				fill="currentColor"
			>
				<path
					fill-rule="evenodd"
					d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
					clip-rule="evenodd"
				/>
			</svg>
			Add Staff Member
		</button>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow-md">
		<div class="flex items-center justify-between border-b p-4">
			<div class="relative">
				<input
					type="text"
					placeholder="Search staff members..."
					class="rounded-lg border py-2 pl-10 pr-4 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
				/>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="absolute left-3 top-2.5 h-5 w-5 text-gray-400"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
						clip-rule="evenodd"
					/>
				</svg>
			</div>

			<div class="flex space-x-2">
				<select
					class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
				>
					<option>All Departments</option>
					<option>Administration</option>
					<option>Sales</option>
					<option>Support</option>
					<option>Marketing</option>
				</select>
				<select
					class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
				>
					<option>All Roles</option>
					<option>Manager</option>
					<option>Employee</option>
					<option>Admin</option>
				</select>
			</div>
		</div>

		<table class="w-full">
			<thead>
				<tr class="bg-gray-50 text-left">
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Name</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Email</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Department</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Role</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Last Login</th>
					<th class="px-6 py-3 text-sm font-medium text-gray-500">Actions</th>
				</tr>
			</thead>
			<tbody>
				{#if mockStaffStore.fetching}
					<tr>
						<td colspan="6" class="px-6 py-8 text-center text-gray-500">
							Loading staff members...
						</td>
					</tr>
				{:else if mockStaffStore.errors}
					<tr>
						<td colspan="6" class="px-6 py-8 text-center text-red-500">
							Error loading staff: {mockStaffStore.errors.map(e => e.message).join(', ')}
						</td>
					</tr>
				{:else if !mockStaffStore.data?.staff?.edges?.length}
					<tr>
						<td colspan="6" class="px-6 py-8 text-center text-gray-500">
							No staff members found. Add your first staff member to get started.
						</td>
					</tr>
				{:else}
					{#each mockStaffStore.data.staff.edges as { node: staff }}
						<tr class="border-t border-gray-100 hover:bg-gray-50">
							<td class="px-6 py-4">
								<div class="flex items-center">
									<div
										class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-primary-100 font-medium text-primary-700"
									>
										{staff.username.slice(0, 2).toUpperCase()}
									</div>
									<div class="ml-4">
										<div class="font-medium text-gray-800">{staff.username}</div>
										<div class="text-sm text-gray-500">Staff ID: {staff.id}</div>
									</div>
								</div>
							</td>
							<td class="px-6 py-4 text-gray-800">{staff.email || 'N/A'}</td>
							<td class="px-6 py-4 text-gray-800">{staff.department || 'N/A'}</td>
							<td class="px-6 py-4 text-gray-800">{staff.role || 'N/A'}</td>
							<td class="px-6 py-4 text-gray-800">
								{staff.lastLogin ? new Date(staff.lastLogin).toLocaleDateString() : 'Never'}
							</td>
							<td class="px-6 py-4">
								<div class="flex space-x-2">
									<button
										class="text-gray-400 hover:text-primary-600"
										aria-label="View staff details"
										onclick={() => showStaffDetails(staff)}
									>
										<svg
											xmlns="http://www.w3.org/2000/svg"
											class="h-5 w-5"
											viewBox="0 0 20 20"
											fill="currentColor"
										>
											<path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
											<path
												fill-rule="evenodd"
												d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
												clip-rule="evenodd"
											/>
										</svg>
									</button>
									<button class="text-gray-400 hover:text-blue-600" aria-label="Edit staff member">
										<svg
											xmlns="http://www.w3.org/2000/svg"
											class="h-5 w-5"
											viewBox="0 0 20 20"
											fill="currentColor"
										>
											<path
												d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
											/>
										</svg>
									</button>
								</div>
							</td>
						</tr>
					{/each}
				{/if}
			</tbody>
		</table>

		{#if mockStaffStore.data?.staff?.pageInfo}
			<div class="flex items-center justify-between border-t px-6 py-4">
				<p class="text-sm text-gray-500">
					Showing staff members
				</p>
				<div class="flex space-x-1">
					<button 
						class="rounded-md px-3 py-1 text-gray-600 {mockStaffStore.data.staff.pageInfo.hasPreviousPage ? 'bg-gray-100 hover:bg-gray-200' : 'bg-gray-50 cursor-not-allowed'}" 
						disabled={!mockStaffStore.data.staff.pageInfo.hasPreviousPage}
					>
						Previous
					</button>
					<button class="rounded-md bg-primary-600 px-3 py-1 text-white">1</button>
					<button 
						class="rounded-md px-3 py-1 text-gray-600 {mockStaffStore.data.staff.pageInfo.hasNextPage ? 'bg-gray-100 hover:bg-gray-200' : 'bg-gray-50 cursor-not-allowed'}" 
						disabled={!mockStaffStore.data.staff.pageInfo.hasNextPage}
					>
						Next
					</button>
				</div>
			</div>
		{/if}
	</div>

	<!-- Add Staff Modal -->
	{#if showAddModal}
		<div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
			<div class="max-h-[90vh] w-full max-w-md overflow-y-auto rounded-lg bg-white shadow-xl">
				<div class="flex items-center justify-between border-b p-6">
					<h2 class="text-xl font-semibold text-gray-800">Add Staff Member</h2>
					<button
						class="text-gray-500 hover:text-gray-700"
						onclick={closeModals}
						aria-label="Close modal"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="h-6 w-6"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M6 18L18 6M6 6l12 12"
							/>
						</svg>
					</button>
				</div>
				<div class="p-6">
					<StaffForm onCancel={closeModals} />
				</div>
			</div>
		</div>
	{/if}

	<!-- Staff Details Modal -->
	{#if showDetailsModal && selectedStaff}
		<div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
			<div class="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-lg bg-white shadow-xl">
				<div class="flex items-center justify-between border-b p-6">
					<h2 class="text-xl font-semibold text-gray-800">Staff Details</h2>
					<button
						class="text-gray-500 hover:text-gray-700"
						onclick={closeModals}
						aria-label="Close modal"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="h-6 w-6"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M6 18L18 6M6 6l12 12"
							/>
						</svg>
					</button>
				</div>

				<div class="space-y-6 p-6">
					<div class="flex items-center">
						<div
							class="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-full bg-primary-100 text-xl font-medium text-primary-700"
						>
							{selectedStaff.username.slice(0, 2).toUpperCase()}
						</div>
						<div class="ml-4">
							<h3 class="text-lg font-medium text-gray-800">{selectedStaff.username}</h3>
							<p class="text-gray-500">{selectedStaff.email || 'No email provided'}</p>
						</div>
					</div>

					<div class="grid grid-cols-2 gap-6">
						<div>
							<h3 class="mb-2 text-sm font-medium text-gray-500">Contact Information</h3>
							<div class="space-y-2">
								<p class="text-sm text-gray-800">
									<span class="font-medium">Username:</span>
									{selectedStaff.username}
								</p>
								<p class="text-sm text-gray-800">
									<span class="font-medium">Email:</span>
									{selectedStaff.email || 'N/A'}
								</p>
								<p class="text-sm text-gray-800">
									<span class="font-medium">Actor Type:</span>
									{selectedStaff.actorType}
								</p>
							</div>
						</div>

						<div>
							<h3 class="mb-2 text-sm font-medium text-gray-500">Work Information</h3>
							<div class="space-y-2">
								<p class="text-sm text-gray-800">
									<span class="font-medium">Department:</span>
									{selectedStaff.department || 'N/A'}
								</p>
								<p class="text-sm text-gray-800">
									<span class="font-medium">Role:</span>
									{selectedStaff.role || 'N/A'}
								</p>
								<p class="text-sm text-gray-800">
									<span class="font-medium">Created:</span>
									{new Date(selectedStaff.createdAt).toLocaleDateString()}
								</p>
								<p class="text-sm text-gray-800">
									<span class="font-medium">Last Login:</span>
									{selectedStaff.lastLogin ? new Date(selectedStaff.lastLogin).toLocaleDateString() : 'Never'}
								</p>
							</div>
						</div>
					</div>
				</div>

				<div class="flex justify-end space-x-3 border-t bg-gray-50 p-6">
					<button
						class="rounded-lg bg-gray-100 px-4 py-2 text-gray-700 hover:bg-gray-200"
						onclick={closeModals}
					>
						Close
					</button>
					<button
						class="flex items-center rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="mr-2 h-5 w-5"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
							/>
						</svg>
						Edit Staff
					</button>
				</div>
			</div>
		</div>
	{/if}
</DashboardLayout>