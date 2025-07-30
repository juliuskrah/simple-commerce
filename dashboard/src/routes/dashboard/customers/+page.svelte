<script lang="ts">
    import DashboardLayout from '$lib/components/DashboardLayout.svelte';
    
    let { data } = $props();
    const user = $derived(data.user);
    
    // Sample data for customers
    const customers = [
        {
            id: 1,
            name: 'Angel King',
            email: 'angel.king@example.com',
            phone: '+233 50 123 4567',
            location: 'Accra, Ghana',
            orderCount: 5,
            totalSpent: 'GHS 1,500',
            lastOrder: '2025-07-25',
            status: 'Active'
        },
        {
            id: 2,
            name: 'Sarah Johnson',
            email: 'sarah.johnson@example.com',
            phone: '+233 24 789 0123',
            location: 'Kumasi, Ghana',
            orderCount: 3,
            totalSpent: 'GHS 900',
            lastOrder: '2025-07-24',
            status: 'Active'
        },
        {
            id: 3,
            name: 'Michael Brown',
            email: 'michael.brown@example.com',
            phone: '+233 55 456 7890',
            location: 'Takoradi, Ghana',
            orderCount: 2,
            totalSpent: 'GHS 600',
            lastOrder: '2025-07-23',
            status: 'Inactive'
        },
        {
            id: 4,
            name: 'Emma Wilson',
            email: 'emma.wilson@example.com',
            phone: '+233 27 901 2345',
            location: 'Tamale, Ghana',
            orderCount: 1,
            totalSpent: 'GHS 250',
            lastOrder: '2025-07-22',
            status: 'Active'
        },
        {
            id: 5,
            name: 'James Taylor',
            email: 'james.taylor@example.com',
            phone: '+233 54 567 8901',
            location: 'Cape Coast, Ghana',
            orderCount: 4,
            totalSpent: 'GHS 1,200',
            lastOrder: '2025-07-21',
            status: 'Active'
        }
    ];
    
    // State for customer details
    let selectedCustomer: typeof customers[0] | null = $state(null);

    function showCustomerDetails(customer: typeof customers[0]) {
        selectedCustomer = customer;
    }
    
    function closeCustomerDetails() {
        selectedCustomer = null;
    }
</script>

<DashboardLayout title="Customers" {user}>
    <div class="mb-6 flex justify-between items-center">
        <h1 class="text-2xl font-semibold text-gray-800">Customers</h1>
        <button class="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Add Customer
        </button>
    </div>
    
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
        <div class="p-4 border-b flex justify-between items-center">
            <div class="relative">
                <input
                    type="text"
                    placeholder="Search customers..."
                    class="pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-2.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                </svg>
            </div>
            
            <div class="flex space-x-2">
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>All Status</option>
                    <option>Active</option>
                    <option>Inactive</option>
                </select>
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>All Locations</option>
                    <option>Accra</option>
                    <option>Kumasi</option>
                    <option>Takoradi</option>
                    <option>Tamale</option>
                    <option>Cape Coast</option>
                </select>
            </div>
        </div>
        
        <table class="w-full">
            <thead>
                <tr class="text-left bg-gray-50">
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Name</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Email</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Location</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Orders</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Total Spent</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Status</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each customers as customer}
                    <tr class="border-t border-gray-100 hover:bg-gray-50">
                        <td class="px-6 py-4">
                            <div class="flex items-center">
                                <div class="h-10 w-10 flex-shrink-0 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center font-medium">
                                    {customer.name.split(' ').map(n => n[0]).join('')}
                                </div>
                                <div class="ml-4">
                                    <div class="font-medium text-gray-800">{customer.name}</div>
                                    <div class="text-sm text-gray-500">{customer.phone}</div>
                                </div>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-gray-800">{customer.email}</td>
                        <td class="px-6 py-4 text-gray-800">{customer.location}</td>
                        <td class="px-6 py-4 text-gray-800">{customer.orderCount}</td>
                        <td class="px-6 py-4 text-gray-800">{customer.totalSpent}</td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-1 text-xs rounded-full {
                                customer.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                            }">
                                {customer.status}
                            </span>
                        </td>
                        <td class="px-6 py-4">
                            <div class="flex space-x-2">
                                <button 
                                    class="text-gray-400 hover:text-primary-600" 
                                    aria-label="View customer details"
                                    onclick={() => showCustomerDetails(customer)}
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                        <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                                <button class="text-gray-400 hover:text-blue-600" aria-label="Edit customer">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                                    </svg>
                                </button>
                            </div>
                        </td>
                    </tr>
                {/each}
            </tbody>
        </table>
        
        <div class="px-6 py-4 border-t flex justify-between items-center">
            <p class="text-gray-500 text-sm">Showing 1 to 5 of 5 entries</p>
            <div class="flex space-x-1">
                <button class="px-3 py-1 bg-gray-100 text-gray-600 rounded-md disabled" disabled>Previous</button>
                <button class="px-3 py-1 bg-primary-600 text-white rounded-md">1</button>
                <button class="px-3 py-1 bg-gray-100 text-gray-600 rounded-md disabled" disabled>Next</button>
            </div>
        </div>
    </div>
    
    {#if selectedCustomer}
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
                <div class="p-6 border-b flex justify-between items-center">
                    <h2 class="text-xl font-semibold text-gray-800">Customer Details</h2>
                    <button class="text-gray-500 hover:text-gray-700" onclick={closeCustomerDetails} aria-label="Close customer details">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>
                
                <div class="p-6 space-y-6">
                    <div class="flex items-center">
                        <div class="h-16 w-16 flex-shrink-0 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center text-xl font-medium">
                            {selectedCustomer.name.split(' ').map(n => n[0]).join('')}
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-medium text-gray-800">{selectedCustomer.name}</h3>
                            <p class="text-gray-500">{selectedCustomer.email}</p>
                        </div>
                    </div>
                    
                    <div class="grid grid-cols-2 gap-6">
                        <div>
                            <h3 class="text-sm font-medium text-gray-500 mb-2">Contact Information</h3>
                            <div class="space-y-2">
                                <p class="text-sm text-gray-800"><span class="font-medium">Email:</span> {selectedCustomer.email}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Phone:</span> {selectedCustomer.phone}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Location:</span> {selectedCustomer.location}</p>
                            </div>
                        </div>
                        
                        <div>
                            <h3 class="text-sm font-medium text-gray-500 mb-2">Purchase Information</h3>
                            <div class="space-y-2">
                                <p class="text-sm text-gray-800"><span class="font-medium">Total Orders:</span> {selectedCustomer.orderCount}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Total Spent:</span> {selectedCustomer.totalSpent}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Last Order:</span> {selectedCustomer.lastOrder}</p>
                                <p class="text-sm text-gray-800">
                                    <span class="font-medium">Status:</span> 
                                    <span class="px-2 py-1 text-xs rounded-full {
                                        selectedCustomer.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                                    }">
                                        {selectedCustomer.status}
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>
                    
                    <div>
                        <h3 class="text-sm font-medium text-gray-500 mb-3">Recent Orders</h3>
                        <table class="w-full text-sm">
                            <thead>
                                <tr class="text-left bg-gray-50 text-gray-500">
                                    <th class="px-4 py-2 font-medium">Order ID</th>
                                    <th class="px-4 py-2 font-medium">Date</th>
                                    <th class="px-4 py-2 font-medium">Amount</th>
                                    <th class="px-4 py-2 font-medium">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr class="border-t border-gray-100">
                                    <td class="px-4 py-3 text-primary-600 font-medium">#ORD-001</td>
                                    <td class="px-4 py-3 text-gray-800">2025-07-25</td>
                                    <td class="px-4 py-3 text-gray-800">GHS 500</td>
                                    <td class="px-4 py-3">
                                        <span class="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">Paid</span>
                                    </td>
                                </tr>
                                {#if selectedCustomer.orderCount > 1}
                                    <tr class="border-t border-gray-100">
                                        <td class="px-4 py-3 text-primary-600 font-medium">#ORD-002</td>
                                        <td class="px-4 py-3 text-gray-800">2025-07-22</td>
                                        <td class="px-4 py-3 text-gray-800">GHS 350</td>
                                        <td class="px-4 py-3">
                                            <span class="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">Shipped</span>
                                        </td>
                                    </tr>
                                {/if}
                                {#if selectedCustomer.orderCount > 2}
                                    <tr class="border-t border-gray-100">
                                        <td class="px-4 py-3 text-primary-600 font-medium">#ORD-003</td>
                                        <td class="px-4 py-3 text-gray-800">2025-07-18</td>
                                        <td class="px-4 py-3 text-gray-800">GHS 250</td>
                                        <td class="px-4 py-3">
                                            <span class="px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">Delivered</span>
                                        </td>
                                    </tr>
                                {/if}
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <div class="p-6 border-t bg-gray-50 flex justify-end space-x-3">
                    <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg" onclick={closeCustomerDetails}>
                        Close
                    </button>
                    <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                        </svg>
                        Edit Customer
                    </button>
                </div>
            </div>
        </div>
    {/if}
</DashboardLayout>
