<script lang="ts">
    // Header component for dashboard
    export let title = 'Home';
</script>

<header class="h-20 bg-white border-b flex items-center justify-between px-8">
    <div>
        <h1 class="text-xl font-semibold text-gray-800">{title}</h1>
    </div>
    
    <div class="flex items-center space-x-4">
        <div class="relative">
            <input
                type="text"
                placeholder="Search..."
                class="px-4 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
            <span class="absolute right-3 top-2.5 text-gray-400">
                <i class="feather-search"></i>
            </span>
        </div>
        
        <div class="relative">
            <button class="relative p-2 text-gray-500 hover:text-gray-700" aria-label="Notifications">
                <i class="feather-bell"></i>
                <span class="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
        </div>
    </div>
</header>
