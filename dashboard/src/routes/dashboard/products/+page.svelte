<script lang="ts">
    import DashboardLayout from '$lib/components/DashboardLayout.svelte';
    
    let { data } = $props();
    const user = $derived(data.user);
    
    // Sample data for products
    const products = [
        {
            id: 1,
            name: 'Classy men wristwatch',
            price: 'GHS 500',
            inventory: 25,
            category: 'Accessories',
            status: 'Active'
        },
        {
            id: 2,
            name: 'Women\'s handbag',
            price: 'GHS 300',
            inventory: 15,
            category: 'Accessories',
            status: 'Active'
        },
        {
            id: 3,
            name: 'Smartphone case',
            price: 'GHS 100',
            inventory: 50,
            category: 'Electronics',
            status: 'Active'
        },
        {
            id: 4,
            name: 'Wireless headphones',
            price: 'GHS 250',
            inventory: 8,
            category: 'Electronics',
            status: 'Low stock'
        },
        {
            id: 5,
            name: 'Summer dress',
            price: 'GHS 180',
            inventory: 0,
            category: 'Clothing',
            status: 'Out of stock'
        }
    ];
</script>

<DashboardLayout title="Products" {user}>
    <div class="bg-white rounded-lg shadow-md mb-8">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h2 class="font-semibold text-lg text-gray-800">Products</h2>
            <button class="bg-primary-600 text-white px-4 py-2 rounded-md text-sm flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                </svg>
                Add Product
            </button>
        </div>
    
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
        <div class="p-4 border-b flex justify-between items-center">
            <div class="relative">
                <input
                    type="text"
                    placeholder="Search products..."
                    class="pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-2.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                </svg>
            </div>
            
            <div class="flex space-x-2">
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>All Categories</option>
                    <option>Accessories</option>
                    <option>Electronics</option>
                    <option>Clothing</option>
                </select>
                <select class="border rounded-lg px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500">
                    <option>All Status</option>
                    <option>Active</option>
                    <option>Low stock</option>
                    <option>Out of stock</option>
                </select>
            </div>
        </div>
        
        <table class="w-full">
            <thead>
                <tr class="text-left bg-gray-50">
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Product</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Price</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Inventory</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Category</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Status</th>
                    <th class="px-6 py-3 text-gray-500 font-medium text-sm">Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each products as product}
                    <tr class="border-t border-gray-100 hover:bg-gray-50">
                        <td class="px-6 py-4">
                            <div class="flex items-center">
                                <div class="h-10 w-10 flex-shrink-0 bg-gray-200 rounded"></div>
                                <div class="ml-3">
                                    <p class="text-gray-800 font-medium">{product.name}</p>
                                </div>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-gray-800">{product.price}</td>
                        <td class="px-6 py-4 text-gray-800">{product.inventory}</td>
                        <td class="px-6 py-4 text-gray-800">{product.category}</td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-1 text-xs rounded-full {
                                product.status === 'Active' ? 'bg-green-100 text-green-800' : 
                                product.status === 'Low stock' ? 'bg-yellow-100 text-yellow-800' : 
                                'bg-red-100 text-red-800'
                            }">
                                {product.status}
                            </span>
                        </td>
                        <td class="px-6 py-4">
                            <div class="flex space-x-2">
                                <button class="text-gray-400 hover:text-primary-600" aria-label="Edit product">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                                    </svg>
                                </button>
                                <button class="text-gray-400 hover:text-red-600" aria-label="Delete product">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
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
</DashboardLayout>
