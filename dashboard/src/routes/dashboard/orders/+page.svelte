<script lang="ts">
    import DashboardLayout from '$lib/components/DashboardLayout.svelte';
    
    // Sample data for orders
    const orders = [
        {
            id: '#ORD-001',
            date: '2025-07-25',
            customer: 'Angel King',
            total: 'GHS 500',
            payment: 'Credit Card',
            status: 'Paid',
            items: [
                { name: 'Classy men wristwatch', quantity: 1, price: 'GHS 500' }
            ]
        },
        {
            id: '#ORD-002',
            date: '2025-07-24',
            customer: 'Sarah Johnson',
            total: 'GHS 300',
            payment: 'Mobile Money',
            status: 'Paid',
            items: [
                { name: 'Women\'s handbag', quantity: 1, price: 'GHS 300' }
            ]
        },
        {
            id: '#ORD-003',
            date: '2025-07-24',
            customer: 'Michael Brown',
            total: 'GHS 500',
            payment: 'Credit Card',
            status: 'Processing',
            items: [
                { name: 'Smartphone case', quantity: 5, price: 'GHS 100' }
            ]
        },
        {
            id: '#ORD-004',
            date: '2025-07-23',
            customer: 'Emma Wilson',
            total: 'GHS 250',
            payment: 'Cash on Delivery',
            status: 'Shipped',
            items: [
                { name: 'Wireless headphones', quantity: 1, price: 'GHS 250' }
            ]
        },
        {
            id: '#ORD-005',
            date: '2025-07-22',
            customer: 'James Taylor',
            total: 'GHS 180',
            payment: 'Mobile Money',
            status: 'Delivered',
            items: [
                { name: 'Summer dress', quantity: 1, price: 'GHS 180' }
            ]
        }
    ];
    
    // State for order details
    let selectedOrder: typeof orders[0] | null = null;
    
    function showOrderDetails(order: typeof orders[0]) {
        selectedOrder = order;
    }
    
    function closeOrderDetails() {
        selectedOrder = null;
    }
</script>

<DashboardLayout title="Orders">
    <div class="mb-6 flex justify-between items-center">
        <h1 class="text-2xl font-semibold text-gray-800">Orders</h1>
        <div class="flex space-x-2">
            <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
                Export
            </button>
            <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M5 4v3H4a2 2 0 00-2 2v3a2 2 0 002 2h1v2a2 2 0 002 2h6a2 2 0 002-2v-2h1a2 2 0 002-2V9a2 2 0 00-2-2h-1V4a2 2 0 00-2-2H7a2 2 0 00-2 2zm8 0v3H7V4h6zm-5 7v4h4v-4H8z" clip-rule="evenodd" />
                </svg>
                Print
            </button>
        </div>
    </div>
    
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
        <div class="p-4 border-b flex justify-between items-center">
            <div class="relative">
                <input
                    type="text"
                    placeholder="Search orders..."
                    class="pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-2.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                </svg>
            </div>
            
            <div class="flex space-x-2">
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>All Status</option>
                    <option>Paid</option>
                    <option>Processing</option>
                    <option>Shipped</option>
                    <option>Delivered</option>
                </select>
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>Last 30 days</option>
                    <option>Last 7 days</option>
                    <option>Last 90 days</option>
                    <option>All time</option>
                </select>
            </div>
        </div>
        
        <table class="w-full">
            <thead>
                <tr class="text-left bg-gray-50">
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Order ID</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Date</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Customer</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Total</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Payment</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Status</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each orders as order}
                    <tr class="border-t border-gray-100 hover:bg-gray-50">
                        <td class="px-6 py-4 font-medium text-primary-600">{order.id}</td>
                        <td class="px-6 py-4 text-gray-800">{order.date}</td>
                        <td class="px-6 py-4 text-gray-800">{order.customer}</td>
                        <td class="px-6 py-4 text-gray-800">{order.total}</td>
                        <td class="px-6 py-4 text-gray-800">{order.payment}</td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-1 text-xs rounded-full {
                                order.status === 'Paid' ? 'bg-green-100 text-green-800' : 
                                order.status === 'Processing' ? 'bg-yellow-100 text-yellow-800' : 
                                order.status === 'Shipped' ? 'bg-blue-100 text-blue-800' : 
                                'bg-purple-100 text-purple-800'
                            }">
                                {order.status}
                            </span>
                        </td>
                        <td class="px-6 py-4">
                            <div class="flex space-x-2">
                                <button 
                                    class="text-gray-400 hover:text-primary-600" 
                                    aria-label="View order details"
                                    on:click={() => showOrderDetails(order)}
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                        <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                                <button class="text-gray-400 hover:text-green-600" aria-label="Print invoice">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M5 4v3H4a2 2 0 00-2 2v3a2 2 0 002 2h1v2a2 2 0 002 2h6a2 2 0 002-2v-2h1a2 2 0 002-2V9a2 2 0 00-2-2h-1V4a2 2 0 00-2-2H7a2 2 0 00-2 2zm8 0v3H7V4h6zm-5 7v4h4v-4H8z" clip-rule="evenodd" />
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
    
    {#if selectedOrder}
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
                <div class="p-6 border-b flex justify-between items-center">
                    <h2 class="text-xl font-semibold text-gray-800">Order Details - {selectedOrder.id}</h2>
                    <button class="text-gray-500 hover:text-gray-700" on:click={closeOrderDetails} aria-label="Close order details">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>
                
                <div class="p-6 space-y-6">
                    <div class="grid grid-cols-2 gap-6">
                        <div>
                            <h3 class="text-sm font-medium text-gray-500 mb-2">Order Information</h3>
                            <div class="space-y-2">
                                <p class="text-sm text-gray-800"><span class="font-medium">Order ID:</span> {selectedOrder.id}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Date:</span> {selectedOrder.date}</p>
                                <p class="text-sm text-gray-800">
                                    <span class="font-medium">Status:</span> 
                                    <span class="px-2 py-1 text-xs rounded-full {
                                        selectedOrder.status === 'Paid' ? 'bg-green-100 text-green-800' : 
                                        selectedOrder.status === 'Processing' ? 'bg-yellow-100 text-yellow-800' : 
                                        selectedOrder.status === 'Shipped' ? 'bg-blue-100 text-blue-800' : 
                                        'bg-purple-100 text-purple-800'
                                    }">
                                        {selectedOrder.status}
                                    </span>
                                </p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Payment Method:</span> {selectedOrder.payment}</p>
                            </div>
                        </div>
                        
                        <div>
                            <h3 class="text-sm font-medium text-gray-500 mb-2">Customer Information</h3>
                            <div class="space-y-2">
                                <p class="text-sm text-gray-800"><span class="font-medium">Name:</span> {selectedOrder.customer}</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Email:</span> customer@example.com</p>
                                <p class="text-sm text-gray-800"><span class="font-medium">Phone:</span> +233 5XX XXX XXX</p>
                            </div>
                        </div>
                    </div>
                    
                    <div>
                        <h3 class="text-sm font-medium text-gray-500 mb-3">Order Items</h3>
                        <table class="w-full text-sm">
                            <thead>
                                <tr class="text-left bg-gray-50 text-gray-500">
                                    <th class="px-4 py-2 font-medium">Product</th>
                                    <th class="px-4 py-2 font-medium">Quantity</th>
                                    <th class="px-4 py-2 font-medium">Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each selectedOrder.items as item}
                                    <tr class="border-t border-gray-100">
                                        <td class="px-4 py-3 text-gray-800">{item.name}</td>
                                        <td class="px-4 py-3 text-gray-800">{item.quantity}</td>
                                        <td class="px-4 py-3 text-gray-800">{item.price}</td>
                                    </tr>
                                {/each}
                            </tbody>
                            <tfoot>
                                <tr class="border-t border-gray-200">
                                    <td class="px-4 py-3" colspan="2"><span class="font-medium">Total</span></td>
                                    <td class="px-4 py-3 font-medium">{selectedOrder.total}</td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
                
                <div class="p-6 border-t bg-gray-50 flex justify-end space-x-3">
                    <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg flex items-center" on:click={closeOrderDetails}>
                        Close
                    </button>
                    <button class="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5 4v3H4a2 2 0 00-2 2v3a2 2 0 002 2h1v2a2 2 0 002 2h6a2 2 0 002-2v-2h1a2 2 0 002-2V9a2 2 0 00-2-2h-1V4a2 2 0 00-2-2H7a2 2 0 00-2 2zm8 0v3H7V4h6zm-5 7v4h4v-4H8z" clip-rule="evenodd" />
                        </svg>
                        Print Invoice
                    </button>
                </div>
            </div>
        </div>
    {/if}
</DashboardLayout>
