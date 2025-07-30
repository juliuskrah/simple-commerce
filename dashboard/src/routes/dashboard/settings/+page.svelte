<script lang="ts">
    import DashboardLayout from '$lib/components/DashboardLayout.svelte';
    import { onMount } from 'svelte';
    
    // Form data
    let profileForm = {
        firstName: 'Angel',
        lastName: 'King',
        email: 'angel.king@example.com',
        phone: '+233 50 123 4567',
        country: 'Ghana',
        city: 'Accra',
        address: '123 Main Street, Osu',
        postalCode: 'GA-123-4567',
        bio: 'E-commerce store owner specializing in fashion accessories and electronics.'
    };
    
    let storeForm = {
        storeName: 'Angel\'s Store',
        storeEmail: 'contact@angelsstore.com',
        storePhone: '+233 50 123 4568',
        storeAddress: '123 Main Street, Osu, Accra',
        currency: 'GHS',
        taxRate: '15',
        shippingOptions: [
            { name: 'Standard Shipping', price: '15', days: '3-5' },
            { name: 'Express Shipping', price: '30', days: '1-2' }
        ]
    };
    
    let paymentForm = {
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
    };
    
    let notificationForm = {
        emailNotifications: true,
        smsNotifications: false,
        orderConfirmations: true,
        orderStatusUpdates: true,
        customerSignups: true,
        promotionalEmails: false
    };
    
    // Active tab tracking
    let activeTab = 'profile';
    
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
    
    // Add shipping option
    function addShippingOption() {
        storeForm.shippingOptions = [
            ...storeForm.shippingOptions,
            { name: '', price: '', days: '' }
        ];
    }
    
    // Remove shipping option
    function removeShippingOption(index: number) {
        storeForm.shippingOptions = storeForm.shippingOptions.filter((_, i) => i !== index);
    }
    
    onMount(() => {
        // Any initialization needed when the component mounts
    });
</script>

<DashboardLayout title="Settings">
    <div class="mb-6">
        <h1 class="text-2xl font-semibold text-gray-800">Settings</h1>
        <p class="text-gray-500 mt-1">Manage your account and store settings</p>
    </div>
    
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
        <div class="flex border-b">
            <button 
                class="px-6 py-3 font-medium text-sm {activeTab === 'profile' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-500 hover:text-gray-700'}"
                on:click={() => setActiveTab('profile')}
            >
                Profile
            </button>
            <button 
                class="px-6 py-3 font-medium text-sm {activeTab === 'store' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-500 hover:text-gray-700'}"
                on:click={() => setActiveTab('store')}
            >
                Store
            </button>
            <button 
                class="px-6 py-3 font-medium text-sm {activeTab === 'payment' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-500 hover:text-gray-700'}"
                on:click={() => setActiveTab('payment')}
            >
                Payment
            </button>
            <button 
                class="px-6 py-3 font-medium text-sm {activeTab === 'notification' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-500 hover:text-gray-700'}"
                on:click={() => setActiveTab('notification')}
            >
                Notifications
            </button>
        </div>
        
        <div class="p-6">
            <!-- Profile Settings -->
            {#if activeTab === 'profile'}
                <form on:submit|preventDefault={saveProfile} class="space-y-6">
                    <div class="flex items-center space-x-6 mb-6">
                        <div class="h-24 w-24 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center text-2xl font-medium">
                            {profileForm.firstName[0]}{profileForm.lastName[0]}
                        </div>
                        <div>
                            <h3 class="text-lg font-medium text-gray-800">{profileForm.firstName} {profileForm.lastName}</h3>
                            <p class="text-gray-500">{profileForm.email}</p>
                            <button class="mt-2 text-primary-600 hover:text-primary-800 text-sm font-medium">
                                Change Profile Picture
                            </button>
                        </div>
                    </div>
                    
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label for="firstName" class="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                            <input 
                                type="text" 
                                id="firstName" 
                                bind:value={profileForm.firstName}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="lastName" class="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                            <input 
                                type="text" 
                                id="lastName" 
                                bind:value={profileForm.lastName}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
                            <input 
                                type="email" 
                                id="email" 
                                bind:value={profileForm.email}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">Phone Number</label>
                            <input 
                                type="text" 
                                id="phone" 
                                bind:value={profileForm.phone}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="country" class="block text-sm font-medium text-gray-700 mb-1">Country</label>
                            <input 
                                type="text" 
                                id="country" 
                                bind:value={profileForm.country}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="city" class="block text-sm font-medium text-gray-700 mb-1">City</label>
                            <input 
                                type="text" 
                                id="city" 
                                bind:value={profileForm.city}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="address" class="block text-sm font-medium text-gray-700 mb-1">Address</label>
                            <input 
                                type="text" 
                                id="address" 
                                bind:value={profileForm.address}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="postalCode" class="block text-sm font-medium text-gray-700 mb-1">Postal Code</label>
                            <input 
                                type="text" 
                                id="postalCode" 
                                bind:value={profileForm.postalCode}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                    </div>
                    
                    <div>
                        <label for="bio" class="block text-sm font-medium text-gray-700 mb-1">Bio</label>
                        <textarea 
                            id="bio" 
                            bind:value={profileForm.bio}
                            rows="4"
                            class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        ></textarea>
                    </div>
                    
                    <div class="flex justify-end">
                        <button type="submit" class="bg-primary-600 hover:bg-primary-700 text-white px-6 py-2 rounded-lg">
                            Save Changes
                        </button>
                    </div>
                </form>
            {/if}
            
            <!-- Store Settings -->
            {#if activeTab === 'store'}
                <form on:submit|preventDefault={saveStoreSettings} class="space-y-6">
                    <h3 class="text-lg font-medium text-gray-800 mb-4">Store Information</h3>
                    
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label for="storeName" class="block text-sm font-medium text-gray-700 mb-1">Store Name</label>
                            <input 
                                type="text" 
                                id="storeName" 
                                bind:value={storeForm.storeName}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="storeEmail" class="block text-sm font-medium text-gray-700 mb-1">Store Email</label>
                            <input 
                                type="email" 
                                id="storeEmail" 
                                bind:value={storeForm.storeEmail}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="storePhone" class="block text-sm font-medium text-gray-700 mb-1">Store Phone</label>
                            <input 
                                type="text" 
                                id="storePhone" 
                                bind:value={storeForm.storePhone}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="storeAddress" class="block text-sm font-medium text-gray-700 mb-1">Store Address</label>
                            <input 
                                type="text" 
                                id="storeAddress" 
                                bind:value={storeForm.storeAddress}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                        <div>
                            <label for="currency" class="block text-sm font-medium text-gray-700 mb-1">Currency</label>
                            <select 
                                id="currency" 
                                bind:value={storeForm.currency}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            >
                                <option value="GHS">Ghana Cedi (GHS)</option>
                                <option value="USD">US Dollar (USD)</option>
                                <option value="EUR">Euro (EUR)</option>
                                <option value="GBP">British Pound (GBP)</option>
                                <option value="NGN">Nigerian Naira (NGN)</option>
                            </select>
                        </div>
                        <div>
                            <label for="taxRate" class="block text-sm font-medium text-gray-700 mb-1">Tax Rate (%)</label>
                            <input 
                                type="text" 
                                id="taxRate" 
                                bind:value={storeForm.taxRate}
                                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                    </div>
                    
                    <div class="mt-6">
                        <h3 class="text-lg font-medium text-gray-800 mb-4">Shipping Options</h3>
                        
                        {#each storeForm.shippingOptions as option, index}
                            <div class="flex space-x-4 items-end mb-4">
                                <div class="flex-1">
                                    <label for={`shipping-name-${index}`} class="block text-sm font-medium text-gray-700 mb-1">Shipping Name</label>
                                    <input 
                                        type="text" 
                                        id={`shipping-name-${index}`}
                                        bind:value={option.name}
                                        class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                        placeholder="e.g. Standard Shipping"
                                    />
                                </div>
                                <div class="w-24">
                                    <label for={`shipping-price-${index}`} class="block text-sm font-medium text-gray-700 mb-1">Price</label>
                                    <input 
                                        type="text" 
                                        id={`shipping-price-${index}`}
                                        bind:value={option.price}
                                        class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                        placeholder="15"
                                    />
                                </div>
                                <div class="w-32">
                                    <label for={`shipping-days-${index}`} class="block text-sm font-medium text-gray-700 mb-1">Days</label>
                                    <input 
                                        type="text" 
                                        id={`shipping-days-${index}`}
                                        bind:value={option.days}
                                        class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                        placeholder="3-5"
                                    />
                                </div>
                                <button 
                                    type="button" 
                                    class="px-3 py-2 text-red-600 hover:text-red-800" 
                                    on:click={() => removeShippingOption(index)}
                                    aria-label="Remove shipping option"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                            </div>
                        {/each}
                        
                        <button 
                            type="button" 
                            class="text-primary-600 hover:text-primary-800 flex items-center" 
                            on:click={addShippingOption}
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                            </svg>
                            Add Shipping Option
                        </button>
                    </div>
                    
                    <div class="flex justify-end mt-6">
                        <button type="submit" class="bg-primary-600 hover:bg-primary-700 text-white px-6 py-2 rounded-lg">
                            Save Store Settings
                        </button>
                    </div>
                </form>
            {/if}
            
            <!-- Payment Settings -->
            {#if activeTab === 'payment'}
                <form on:submit|preventDefault={savePaymentSettings} class="space-y-6">
                    <h3 class="text-lg font-medium text-gray-800 mb-4">Payment Methods</h3>
                    
                    <div class="space-y-4">
                        <div class="flex items-center">
                            <input 
                                type="checkbox" 
                                id="acceptCreditCards" 
                                bind:checked={paymentForm.acceptCreditCards}
                                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
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
                                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
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
                                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                            />
                            <label for="acceptCashOnDelivery" class="ml-2 block text-sm text-gray-700">
                                Accept Cash on Delivery
                            </label>
                        </div>
                    </div>
                    
                    <div class="mt-6">
                        <h3 class="text-lg font-medium text-gray-800 mb-4">Bank Account Information</h3>
                        
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label for="bankName" class="block text-sm font-medium text-gray-700 mb-1">Bank Name</label>
                                <input 
                                    type="text" 
                                    id="bankName" 
                                    bind:value={paymentForm.bankAccount.bankName}
                                    class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label for="accountName" class="block text-sm font-medium text-gray-700 mb-1">Account Name</label>
                                <input 
                                    type="text" 
                                    id="accountName" 
                                    bind:value={paymentForm.bankAccount.accountName}
                                    class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label for="accountNumber" class="block text-sm font-medium text-gray-700 mb-1">Account Number</label>
                                <input 
                                    type="text" 
                                    id="accountNumber" 
                                    bind:value={paymentForm.bankAccount.accountNumber}
                                    class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label for="branchCode" class="block text-sm font-medium text-gray-700 mb-1">Branch Code</label>
                                <input 
                                    type="text" 
                                    id="branchCode" 
                                    bind:value={paymentForm.bankAccount.branchCode}
                                    class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                />
                            </div>
                        </div>
                    </div>
                    
                    <div class="flex justify-end mt-6">
                        <button type="submit" class="bg-primary-600 hover:bg-primary-700 text-white px-6 py-2 rounded-lg">
                            Save Payment Settings
                        </button>
                    </div>
                </form>
            {/if}
            
            <!-- Notification Settings -->
            {#if activeTab === 'notification'}
                <form on:submit|preventDefault={saveNotificationSettings} class="space-y-6">
                    <h3 class="text-lg font-medium text-gray-800 mb-4">Notification Preferences</h3>
                    
                    <div class="space-y-4">
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">Email Notifications</h4>
                                <p class="text-sm text-gray-500">Receive notifications via email</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="emailNotifications" 
                                    bind:checked={notificationForm.emailNotifications}
                                    class="sr-only"
                                />
                                <label 
                                    for="emailNotifications" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.emailNotifications ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.emailNotifications ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                        
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">SMS Notifications</h4>
                                <p class="text-sm text-gray-500">Receive notifications via text message</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="smsNotifications" 
                                    bind:checked={notificationForm.smsNotifications}
                                    class="sr-only"
                                />
                                <label 
                                    for="smsNotifications" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.smsNotifications ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.smsNotifications ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                        
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">Order Confirmations</h4>
                                <p class="text-sm text-gray-500">Receive notifications when orders are placed</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="orderConfirmations" 
                                    bind:checked={notificationForm.orderConfirmations}
                                    class="sr-only"
                                />
                                <label 
                                    for="orderConfirmations" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.orderConfirmations ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.orderConfirmations ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                        
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">Order Status Updates</h4>
                                <p class="text-sm text-gray-500">Receive notifications when order status changes</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="orderStatusUpdates" 
                                    bind:checked={notificationForm.orderStatusUpdates}
                                    class="sr-only"
                                />
                                <label 
                                    for="orderStatusUpdates" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.orderStatusUpdates ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.orderStatusUpdates ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                        
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">Customer Signups</h4>
                                <p class="text-sm text-gray-500">Receive notifications when new customers register</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="customerSignups" 
                                    bind:checked={notificationForm.customerSignups}
                                    class="sr-only"
                                />
                                <label 
                                    for="customerSignups" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.customerSignups ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.customerSignups ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                        
                        <div class="flex items-center justify-between py-3 border-b">
                            <div>
                                <h4 class="text-sm font-medium text-gray-700">Promotional Emails</h4>
                                <p class="text-sm text-gray-500">Receive marketing and promotional emails</p>
                            </div>
                            <div class="relative inline-block w-10 mr-2 align-middle select-none">
                                <input 
                                    type="checkbox" 
                                    id="promotionalEmails" 
                                    bind:checked={notificationForm.promotionalEmails}
                                    class="sr-only"
                                />
                                <label 
                                    for="promotionalEmails" 
                                    class="block overflow-hidden h-6 rounded-full bg-gray-300 cursor-pointer {notificationForm.promotionalEmails ? 'bg-primary-600' : ''}"
                                >
                                    <span class="dot block h-6 w-6 rounded-full bg-white shadow transform transition-transform duration-300 ease-in-out {notificationForm.promotionalEmails ? 'translate-x-4' : ''}"></span>
                                </label>
                            </div>
                        </div>
                    </div>
                    
                    <div class="flex justify-end mt-6">
                        <button type="submit" class="bg-primary-600 hover:bg-primary-700 text-white px-6 py-2 rounded-lg">
                            Save Notification Settings
                        </button>
                    </div>
                </form>
            {/if}
        </div>
    </div>
</DashboardLayout>
