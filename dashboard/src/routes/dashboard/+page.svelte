<script lang="ts">
    import DashboardLayout from '$lib/components/DashboardLayout.svelte';
    
    // Sample data for orders
    const recentOrders = [
        {
            id: 1,
            product: 'Classy men wristwatch',
            status: 'Not paid',
            quantity: 10,
            price: 'GHS500',
            customer: 'Angel King'
        },
        {
            id: 2,
            product: 'Women\'s handbag',
            status: 'Paid',
            quantity: 2,
            price: 'GHS300',
            customer: 'Sarah Johnson'
        },
        {
            id: 3,
            product: 'Smartphone case',
            status: 'Shipped',
            quantity: 5,
            price: 'GHS100',
            customer: 'Michael Brown'
        }
    ];
    
    // Sample data for stats cards
    const stats = [
        { 
            title: 'Total Sales', 
            value: 'GHS 12,500', 
            change: '+12.5%', 
            positive: true 
        },
        { 
            title: 'Total Orders', 
            value: '150', 
            change: '+8.2%', 
            positive: true 
        },
        { 
            title: 'Total Customers', 
            value: '85', 
            change: '+5.7%', 
            positive: true 
        },
        { 
            title: 'Avg. Order Value', 
            value: 'GHS 83', 
            change: '-2.3%', 
            positive: false 
        }
    ];
</script>

<DashboardLayout title="Home">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {#each stats as stat}
            <div class="bg-white rounded-lg shadow-md p-6">
                <h3 class="text-gray-500 text-sm font-medium mb-2">{stat.title}</h3>
                <div class="flex items-center justify-between">
                    <p class="text-2xl font-semibold">{stat.value}</p>
                    <span class="text-sm {stat.positive ? 'text-green-500' : 'text-red-500'}">
                        {stat.change}
                    </span>
                </div>
            </div>
        {/each}
    </div>
    
    <div class="bg-white rounded-lg shadow-md mb-8">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h2 class="font-semibold text-lg text-gray-800">Orders</h2>
            <a href="/dashboard/orders" class="text-primary-600 text-sm flex items-center">
                View more
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                </svg>
            </a>
        </div>
        
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead>
                    <tr class="text-left text-gray-500 text-sm border-b border-gray-100">
                        <th class="px-6 py-3 font-medium">Product</th>
                        <th class="px-6 py-3 font-medium">Status</th>
                        <th class="px-6 py-3 font-medium">Quantity</th>
                        <th class="px-6 py-3 font-medium">Price</th>
                        <th class="px-6 py-3 font-medium">Customer</th>
                        <th class="px-6 py-3 font-medium">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each recentOrders as order}
                        <tr class="border-b border-gray-100 hover:bg-gray-50">
                            <td class="px-6 py-4 text-primary-600 font-medium">{order.product}</td>
                            <td class="px-6 py-4">
                                <span class="px-2 py-1 text-xs rounded-full {
                                    order.status === 'Paid' ? 'bg-green-100 text-green-800' : 
                                    order.status === 'Shipped' ? 'bg-blue-100 text-blue-800' : 
                                    'bg-gray-100 text-gray-800'
                                }">
                                    {order.status}
                                </span>
                            </td>
                            <td class="px-6 py-4 text-gray-800">{order.quantity}</td>
                            <td class="px-6 py-4 text-gray-800">{order.price}</td>
                            <td class="px-6 py-4 text-gray-800">{order.customer}</td>
                            <td class="px-6 py-4">
                                <button class="text-gray-400 hover:text-gray-600" aria-label="View order details">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                        <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white rounded-lg shadow-md p-6">
            <h2 class="font-semibold text-lg text-gray-800 mb-4">Recent Activity</h2>
            <div class="space-y-4">
                <div class="flex items-start">
                    <div class="flex-shrink-0 w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center text-primary-600">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 2a4 4 0 00-4 4v1H5a1 1 0 00-.994.89l-1 9A1 1 0 004 18h12a1 1 0 00.994-1.11l-1-9A1 1 0 0015 7h-1V6a4 4 0 00-4-4zm2 5V6a2 2 0 10-4 0v1h4zm-6 3a1 1 0 112 0 1 1 0 01-2 0zm7-1a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-800">New order received</p>
                        <p class="text-xs text-gray-500">30 minutes ago</p>
                    </div>
                </div>
                
                <div class="flex items-start">
                    <div class="flex-shrink-0 w-10 h-10 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-800">Payment received from Sarah Johnson</p>
                        <p class="text-xs text-gray-500">1 hour ago</p>
                    </div>
                </div>
                
                <div class="flex items-start">
                    <div class="flex-shrink-0 w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z" />
                            <path d="M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1v-1h5a1 1 0 001-1v-3a1 1 0 00-.26-.7l-2.41-2.9A1 1 0 0013.59 6H10V3a1 1 0 00-1-1H3z" />
                        </svg>
                    </div>
                    <div class="ml-4">
                        <p class="text-sm font-medium text-gray-800">Order #12345 shipped</p>
                        <p class="text-xs text-gray-500">3 hours ago</p>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
            <h2 class="font-semibold text-lg text-gray-800 mb-4">Top Selling Products</h2>
            <ul class="space-y-4">
                <li class="flex items-center justify-between">
                    <div class="flex items-center">
                        <div class="w-12 h-12 bg-gray-200 rounded"></div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-800">Classy men wristwatch</p>
                            <p class="text-xs text-gray-500">234 sold</p>
                        </div>
                    </div>
                    <span class="text-sm font-medium">GHS 500</span>
                </li>
                
                <li class="flex items-center justify-between">
                    <div class="flex items-center">
                        <div class="w-12 h-12 bg-gray-200 rounded"></div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-800">Women's handbag</p>
                            <p class="text-xs text-gray-500">187 sold</p>
                        </div>
                    </div>
                    <span class="text-sm font-medium">GHS 300</span>
                </li>
                
                <li class="flex items-center justify-between">
                    <div class="flex items-center">
                        <div class="w-12 h-12 bg-gray-200 rounded"></div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-gray-800">Smartphone case</p>
                            <p class="text-xs text-gray-500">125 sold</p>
                        </div>
                    </div>
                    <span class="text-sm font-medium">GHS 100</span>
                </li>
            </ul>
        </div>
    </div>
</DashboardLayout>
