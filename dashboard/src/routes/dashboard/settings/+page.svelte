<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';
	import { onMount } from 'svelte';

	let { data } = $props();
	const user = $derived(data.user);

	// Form data
	let profileForm = $state({
		firstName: 'Angel',
		lastName: 'King',
		email: 'angel.king@example.com',
		phone: '+233 50 123 4567',
		country: 'Ghana',
		city: 'Accra',
		address: '123 Main Street, Osu',
		postalCode: 'GA-123-4567',
		bio: 'E-commerce store owner specializing in fashion accessories and electronics.'
	});

	let storeForm = $state({
		storeName: "Angel's Store",
		storeEmail: 'contact@angelsstore.com',
		storePhone: '+233 50 123 4568',
		storeAddress: '123 Main Street, Osu, Accra',
		currency: 'GHS',
		taxRate: '15',
		shippingOptions: [
			{ name: 'Standard Shipping', price: '15', days: '3-5' },
			{ name: 'Express Shipping', price: '30', days: '1-2' }
		]
	});

	let paymentForm = $state({
		acceptCreditCards: true,
		acceptMobileMoney: true,
		acceptCashOnDelivery: true,
		paymentProcessors: ['Stripe', 'PayPal', 'Mobile Money'],
		bankAccount: {
			bankName: 'Ghana Commercial Bank',
			accountName: 'Angel King',
			accountNumber: '1234567890',
			branchCode: 'ACC-123'
		}
	});

	let notificationForm = $state({
		emailNotifications: true,
		smsNotifications: false,
		orderConfirmations: true,
		orderStatusUpdates: true,
		customerSignups: true,
		promotionalEmails: false
	});

	// Active tab tracking
	let activeTab = $state('profile');

	function setActiveTab(tab: string) {
		activeTab = tab;
	}

	// Form submission handlers
	function saveProfile() {
		// Here you would typically send the form data to your backend
		alert('Profile settings saved successfully!');
	}

	function saveStoreSettings() {
		alert('Store settings saved successfully!');
	}

	function savePaymentSettings() {
		alert('Payment settings saved successfully!');
	}

	function saveNotificationSettings() {
		alert('Notification settings saved successfully!');
	}

	function preventDefault(fn: Function) {
		return (event: Event) => {
			event.preventDefault();
			fn(event);
		};
	}

	// Add shipping option
	function addShippingOption() {
		storeForm.shippingOptions = [...storeForm.shippingOptions, { name: '', price: '', days: '' }];
	}

	// Remove shipping option
	function removeShippingOption(index: number) {
		storeForm.shippingOptions = storeForm.shippingOptions.filter((_, i) => i !== index);
	}

	onMount(() => {
		// Any initialization needed when the component mounts
	});
</script>

<DashboardLayout title="Settings" {user}>
	<div class="mb-6">
		<h1 class="text-2xl font-semibold text-gray-800">Settings</h1>
		<p class="mt-1 text-gray-500">Manage your account and store settings</p>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow-md">
		<div class="flex border-b">
			<button
				class="px-6 py-3 text-sm font-medium {activeTab === 'profile'
					? 'text-primary-600 border-primary-600 border-b-2'
					: 'text-gray-500 hover:text-gray-700'}"
				onclick={() => setActiveTab('profile')}
			>
				Profile
			</button>
			<button
				class="px-6 py-3 text-sm font-medium {activeTab === 'store'
					? 'text-primary-600 border-primary-600 border-b-2'
					: 'text-gray-500 hover:text-gray-700'}"
				onclick={() => setActiveTab('store')}
			>
				Store
			</button>
			<button
				class="px-6 py-3 text-sm font-medium {activeTab === 'payment'
					? 'text-primary-600 border-primary-600 border-b-2'
					: 'text-gray-500 hover:text-gray-700'}"
				onclick={() => setActiveTab('payment')}
			>
				Payment
			</button>
			<button
				class="px-6 py-3 text-sm font-medium {activeTab === 'notification'
					? 'text-primary-600 border-primary-600 border-b-2'
					: 'text-gray-500 hover:text-gray-700'}"
				onclick={() => setActiveTab('notification')}
			>
				Notifications
			</button>
		</div>

		<div class="p-6">
			<!-- Profile Settings -->
			{#if activeTab === 'profile'}
				<form onsubmit={preventDefault(saveProfile)} class="space-y-6">
					<div class="mb-6 flex items-center space-x-6">
						<div
							class="bg-primary-100 text-primary-700 flex h-24 w-24 items-center justify-center rounded-full text-2xl font-medium"
						>
							{profileForm.firstName[0]}{profileForm.lastName[0]}
						</div>
						<div>
							<h3 class="text-lg font-medium text-gray-800">
								{profileForm.firstName}
								{profileForm.lastName}
							</h3>
							<p class="text-gray-500">{profileForm.email}</p>
							<button class="text-primary-600 hover:text-primary-800 mt-2 text-sm font-medium">
								Change Profile Picture
							</button>
						</div>
					</div>

					<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
						<div>
							<label for="firstName" class="mb-1 block text-sm font-medium text-gray-700"
								>First Name</label
							>
							<input
								type="text"
								id="firstName"
								bind:value={profileForm.firstName}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="lastName" class="mb-1 block text-sm font-medium text-gray-700"
								>Last Name</label
							>
							<input
								type="text"
								id="lastName"
								bind:value={profileForm.lastName}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="email" class="mb-1 block text-sm font-medium text-gray-700"
								>Email Address</label
							>
							<input
								type="email"
								id="email"
								bind:value={profileForm.email}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="phone" class="mb-1 block text-sm font-medium text-gray-700"
								>Phone Number</label
							>
							<input
								type="text"
								id="phone"
								bind:value={profileForm.phone}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="country" class="mb-1 block text-sm font-medium text-gray-700"
								>Country</label
							>
							<input
								type="text"
								id="country"
								bind:value={profileForm.country}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="city" class="mb-1 block text-sm font-medium text-gray-700">City</label>
							<input
								type="text"
								id="city"
								bind:value={profileForm.city}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="address" class="mb-1 block text-sm font-medium text-gray-700"
								>Address</label
							>
							<input
								type="text"
								id="address"
								bind:value={profileForm.address}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="postalCode" class="mb-1 block text-sm font-medium text-gray-700"
								>Postal Code</label
							>
							<input
								type="text"
								id="postalCode"
								bind:value={profileForm.postalCode}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
					</div>

					<div>
						<label for="bio" class="mb-1 block text-sm font-medium text-gray-700">Bio</label>
						<textarea
							id="bio"
							bind:value={profileForm.bio}
							rows="4"
							class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
						></textarea>
					</div>

					<div class="flex justify-end">
						<button
							type="submit"
							class="bg-primary-600 hover:bg-primary-700 rounded-lg px-6 py-2 text-white"
						>
							Save Changes
						</button>
					</div>
				</form>
			{/if}

			<!-- Store Settings -->
			{#if activeTab === 'store'}
				<form onsubmit={preventDefault(saveStoreSettings)} class="space-y-6">
					<h3 class="mb-4 text-lg font-medium text-gray-800">Store Information</h3>

					<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
						<div>
							<label for="storeName" class="mb-1 block text-sm font-medium text-gray-700"
								>Store Name</label
							>
							<input
								type="text"
								id="storeName"
								bind:value={storeForm.storeName}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="storeEmail" class="mb-1 block text-sm font-medium text-gray-700"
								>Store Email</label
							>
							<input
								type="email"
								id="storeEmail"
								bind:value={storeForm.storeEmail}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="storePhone" class="mb-1 block text-sm font-medium text-gray-700"
								>Store Phone</label
							>
							<input
								type="text"
								id="storePhone"
								bind:value={storeForm.storePhone}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="storeAddress" class="mb-1 block text-sm font-medium text-gray-700"
								>Store Address</label
							>
							<input
								type="text"
								id="storeAddress"
								bind:value={storeForm.storeAddress}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
						<div>
							<label for="currency" class="mb-1 block text-sm font-medium text-gray-700"
								>Currency</label
							>
							<select
								id="currency"
								bind:value={storeForm.currency}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							>
								<option value="GHS">Ghana Cedi (GHS)</option>
								<option value="USD">US Dollar (USD)</option>
								<option value="EUR">Euro (EUR)</option>
								<option value="GBP">British Pound (GBP)</option>
								<option value="NGN">Nigerian Naira (NGN)</option>
							</select>
						</div>
						<div>
							<label for="taxRate" class="mb-1 block text-sm font-medium text-gray-700"
								>Tax Rate (%)</label
							>
							<input
								type="text"
								id="taxRate"
								bind:value={storeForm.taxRate}
								class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
							/>
						</div>
					</div>

					<div class="mt-6">
						<h3 class="mb-4 text-lg font-medium text-gray-800">Shipping Options</h3>

						{#each storeForm.shippingOptions as option, index}
							<div class="mb-4 flex items-end space-x-4">
								<div class="flex-1">
									<label
										for={`shipping-name-${index}`}
										class="mb-1 block text-sm font-medium text-gray-700">Shipping Name</label
									>
									<input
										type="text"
										id={`shipping-name-${index}`}
										bind:value={option.name}
										class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
										placeholder="e.g. Standard Shipping"
									/>
								</div>
								<div class="w-24">
									<label
										for={`shipping-price-${index}`}
										class="mb-1 block text-sm font-medium text-gray-700">Price</label
									>
									<input
										type="text"
										id={`shipping-price-${index}`}
										bind:value={option.price}
										class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
										placeholder="15"
									/>
								</div>
								<div class="w-32">
									<label
										for={`shipping-days-${index}`}
										class="mb-1 block text-sm font-medium text-gray-700">Days</label
									>
									<input
										type="text"
										id={`shipping-days-${index}`}
										bind:value={option.days}
										class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
										placeholder="3-5"
									/>
								</div>
								<button
									type="button"
									class="px-3 py-2 text-red-600 hover:text-red-800"
									onclick={() => removeShippingOption(index)}
									aria-label="Remove shipping option"
								>
									<svg
										xmlns="http://www.w3.org/2000/svg"
										class="h-5 w-5"
										viewBox="0 0 20 20"
										fill="currentColor"
									>
										<path
											fill-rule="evenodd"
											d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
											clip-rule="evenodd"
										/>
									</svg>
								</button>
							</div>
						{/each}

						<button
							type="button"
							class="text-primary-600 hover:text-primary-800 flex items-center"
							onclick={addShippingOption}
						>
							<svg
								xmlns="http://www.w3.org/2000/svg"
								class="mr-1 h-5 w-5"
								viewBox="0 0 20 20"
								fill="currentColor"
							>
								<path
									fill-rule="evenodd"
									d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
									clip-rule="evenodd"
								/>
							</svg>
							Add Shipping Option
						</button>
					</div>

					<div class="mt-6 flex justify-end">
						<button
							type="submit"
							class="bg-primary-600 hover:bg-primary-700 rounded-lg px-6 py-2 text-white"
						>
							Save Store Settings
						</button>
					</div>
				</form>
			{/if}

			<!-- Payment Settings -->
			{#if activeTab === 'payment'}
				<form onsubmit={preventDefault(savePaymentSettings)} class="space-y-6">
					<h3 class="mb-4 text-lg font-medium text-gray-800">Payment Methods</h3>

					<div class="space-y-4">
						<div class="flex items-center">
							<input
								type="checkbox"
								id="acceptCreditCards"
								bind:checked={paymentForm.acceptCreditCards}
								class="text-primary-600 focus:ring-primary-500 h-4 w-4 rounded border-gray-300"
							/>
							<label for="acceptCreditCards" class="ml-2 block text-sm text-gray-700">
								Accept Credit/Debit Cards
							</label>
						</div>

						<div class="flex items-center">
							<input
								type="checkbox"
								id="acceptMobileMoney"
								bind:checked={paymentForm.acceptMobileMoney}
								class="text-primary-600 focus:ring-primary-500 h-4 w-4 rounded border-gray-300"
							/>
							<label for="acceptMobileMoney" class="ml-2 block text-sm text-gray-700">
								Accept Mobile Money
							</label>
						</div>

						<div class="flex items-center">
							<input
								type="checkbox"
								id="acceptCashOnDelivery"
								bind:checked={paymentForm.acceptCashOnDelivery}
								class="text-primary-600 focus:ring-primary-500 h-4 w-4 rounded border-gray-300"
							/>
							<label for="acceptCashOnDelivery" class="ml-2 block text-sm text-gray-700">
								Accept Cash on Delivery
							</label>
						</div>
					</div>

					<div class="mt-6">
						<h3 class="mb-4 text-lg font-medium text-gray-800">Bank Account Information</h3>

						<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
							<div>
								<label for="bankName" class="mb-1 block text-sm font-medium text-gray-700"
									>Bank Name</label
								>
								<input
									type="text"
									id="bankName"
									bind:value={paymentForm.bankAccount.bankName}
									class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
								/>
							</div>
							<div>
								<label for="accountName" class="mb-1 block text-sm font-medium text-gray-700"
									>Account Name</label
								>
								<input
									type="text"
									id="accountName"
									bind:value={paymentForm.bankAccount.accountName}
									class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
								/>
							</div>
							<div>
								<label for="accountNumber" class="mb-1 block text-sm font-medium text-gray-700"
									>Account Number</label
								>
								<input
									type="text"
									id="accountNumber"
									bind:value={paymentForm.bankAccount.accountNumber}
									class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
								/>
							</div>
							<div>
								<label for="branchCode" class="mb-1 block text-sm font-medium text-gray-700"
									>Branch Code</label
								>
								<input
									type="text"
									id="branchCode"
									bind:value={paymentForm.bankAccount.branchCode}
									class="focus:ring-primary-500 w-full rounded-lg border px-3 py-2 focus:border-transparent focus:ring-2 focus:outline-none"
								/>
							</div>
						</div>
					</div>

					<div class="mt-6 flex justify-end">
						<button
							type="submit"
							class="bg-primary-600 hover:bg-primary-700 rounded-lg px-6 py-2 text-white"
						>
							Save Payment Settings
						</button>
					</div>
				</form>
			{/if}

			<!-- Notification Settings -->
			{#if activeTab === 'notification'}
				<form onsubmit={preventDefault(saveNotificationSettings)} class="space-y-6">
					<h3 class="mb-4 text-lg font-medium text-gray-800">Notification Preferences</h3>

					<div class="space-y-4">
						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">Email Notifications</h4>
								<p class="text-sm text-gray-500">Receive notifications via email</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="emailNotifications"
									bind:checked={notificationForm.emailNotifications}
									class="sr-only"
								/>
								<label
									for="emailNotifications"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.emailNotifications
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.emailNotifications
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>

						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">SMS Notifications</h4>
								<p class="text-sm text-gray-500">Receive notifications via text message</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="smsNotifications"
									bind:checked={notificationForm.smsNotifications}
									class="sr-only"
								/>
								<label
									for="smsNotifications"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.smsNotifications
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.smsNotifications
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>

						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">Order Confirmations</h4>
								<p class="text-sm text-gray-500">Receive notifications when orders are placed</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="orderConfirmations"
									bind:checked={notificationForm.orderConfirmations}
									class="sr-only"
								/>
								<label
									for="orderConfirmations"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.orderConfirmations
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.orderConfirmations
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>

						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">Order Status Updates</h4>
								<p class="text-sm text-gray-500">Receive notifications when order status changes</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="orderStatusUpdates"
									bind:checked={notificationForm.orderStatusUpdates}
									class="sr-only"
								/>
								<label
									for="orderStatusUpdates"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.orderStatusUpdates
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.orderStatusUpdates
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>

						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">Customer Signups</h4>
								<p class="text-sm text-gray-500">
									Receive notifications when new customers register
								</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="customerSignups"
									bind:checked={notificationForm.customerSignups}
									class="sr-only"
								/>
								<label
									for="customerSignups"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.customerSignups
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.customerSignups
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>

						<div class="flex items-center justify-between border-b py-3">
							<div>
								<h4 class="text-sm font-medium text-gray-700">Promotional Emails</h4>
								<p class="text-sm text-gray-500">Receive marketing and promotional emails</p>
							</div>
							<div class="relative mr-2 inline-block w-10 align-middle select-none">
								<input
									type="checkbox"
									id="promotionalEmails"
									bind:checked={notificationForm.promotionalEmails}
									class="sr-only"
								/>
								<label
									for="promotionalEmails"
									class="block h-6 cursor-pointer overflow-hidden rounded-full bg-gray-300 {notificationForm.promotionalEmails
										? 'bg-primary-600'
										: ''}"
								>
									<span
										class="dot block h-6 w-6 transform rounded-full bg-white shadow transition-transform duration-300 ease-in-out {notificationForm.promotionalEmails
											? 'translate-x-4'
											: ''}"
									></span>
								</label>
							</div>
						</div>
					</div>

					<div class="mt-6 flex justify-end">
						<button
							type="submit"
							class="bg-primary-600 hover:bg-primary-700 rounded-lg px-6 py-2 text-white"
						>
							Save Notification Settings
						</button>
					</div>
				</form>
			{/if}
		</div>
	</div>
</DashboardLayout>
