<script lang="ts">
	import DashboardLayout from '$lib/components/DashboardLayout.svelte';

	let { data } = $props();
	const user = $derived(data.user);

	// Sample data for analytics
	const monthlySales = [
		{ month: 'Jan', amount: 12000 },
		{ month: 'Feb', amount: 15000 },
		{ month: 'Mar', amount: 18000 },
		{ month: 'Apr', amount: 16000 },
		{ month: 'May', amount: 21000 },
		{ month: 'Jun', amount: 25000 },
		{ month: 'Jul', amount: 30000 },
		{ month: 'Aug', amount: 28000 },
		{ month: 'Sep', amount: 32000 },
		{ month: 'Oct', amount: 35000 },
		{ month: 'Nov', amount: 38000 },
		{ month: 'Dec', amount: 42000 }
	];

	const trafficSources = [
		{ source: 'Direct', percentage: 35, color: 'bg-primary-500' },
		{ source: 'Search', percentage: 25, color: 'bg-blue-500' },
		{ source: 'Social', percentage: 20, color: 'bg-green-500' },
		{ source: 'Referral', percentage: 15, color: 'bg-yellow-500' },
		{ source: 'Other', percentage: 5, color: 'bg-gray-500' }
	];

	const topSellingProducts = [
		{ name: 'Classy men wristwatch', sales: 120, revenue: 'GHS 60,000', percentage: 15 },
		{ name: "Women's handbag", sales: 95, revenue: 'GHS 28,500', percentage: 12 },
		{ name: 'Wireless headphones', sales: 85, revenue: 'GHS 21,250', percentage: 10 },
		{ name: 'Smartphone case', sales: 78, revenue: 'GHS 7,800', percentage: 8 },
		{ name: 'Summer dress', sales: 65, revenue: 'GHS 11,700', percentage: 7 }
	];

	const locationData = [
		{ location: 'Accra', sales: 'GHS 85,000', percentage: 42 },
		{ location: 'Kumasi', sales: 'GHS 45,000', percentage: 22 },
		{ location: 'Takoradi', sales: 'GHS 30,000', percentage: 15 },
		{ location: 'Tamale', sales: 'GHS 25,000', percentage: 12 },
		{ location: 'Cape Coast', sales: 'GHS 15,000', percentage: 9 }
	];

	// Calculate max value for the chart
	const maxSales = Math.max(...monthlySales.map((item) => item.amount));

	// Filter periods
	const periods = [
		'Last 7 days',
		'Last 30 days',
		'Last 90 days',
		'Last 6 months',
		'Last year',
		'All time'
	];
	let selectedPeriod = $state(periods[4]); // Last year by default
</script>

<DashboardLayout title="Analytics" {user}>
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-2xl font-semibold text-gray-800">Analytics Dashboard</h1>
		<div class="flex space-x-2">
			<select
				class="rounded-lg border px-3 py-2 text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
				bind:value={selectedPeriod}
			>
				{#each periods as period}
					<option value={period}>{period}</option>
				{/each}
			</select>
			<button
				class="flex items-center rounded-lg bg-gray-100 px-4 py-2 text-gray-700 hover:bg-gray-200"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="mr-2 h-5 w-5"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z"
						clip-rule="evenodd"
					/>
				</svg>
				Export Report
			</button>
		</div>
	</div>

	<!-- Overview Stats -->
	<div class="mb-6 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-start justify-between">
				<div>
					<p class="text-sm text-gray-500">Total Revenue</p>
					<h3 class="mt-1 text-2xl font-semibold text-gray-800">GHS 310,000</h3>
				</div>
				<div class="rounded-lg bg-primary-100 p-3">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-6 w-6 text-primary-600"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
						/>
					</svg>
				</div>
			</div>
			<div class="mt-4 flex items-center">
				<span class="flex items-center text-sm text-green-500">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z"
							clip-rule="evenodd"
						/>
					</svg>
					12.5%
				</span>
				<span class="ml-2 text-sm text-gray-500">vs previous period</span>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-start justify-between">
				<div>
					<p class="text-sm text-gray-500">Total Orders</p>
					<h3 class="mt-1 text-2xl font-semibold text-gray-800">850</h3>
				</div>
				<div class="rounded-lg bg-blue-100 p-3">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-6 w-6 text-blue-600"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
						/>
					</svg>
				</div>
			</div>
			<div class="mt-4 flex items-center">
				<span class="flex items-center text-sm text-green-500">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z"
							clip-rule="evenodd"
						/>
					</svg>
					8.2%
				</span>
				<span class="ml-2 text-sm text-gray-500">vs previous period</span>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-start justify-between">
				<div>
					<p class="text-sm text-gray-500">Average Order Value</p>
					<h3 class="mt-1 text-2xl font-semibold text-gray-800">GHS 365</h3>
				</div>
				<div class="rounded-lg bg-green-100 p-3">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-6 w-6 text-green-600"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
						/>
					</svg>
				</div>
			</div>
			<div class="mt-4 flex items-center">
				<span class="flex items-center text-sm text-green-500">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z"
							clip-rule="evenodd"
						/>
					</svg>
					4.3%
				</span>
				<span class="ml-2 text-sm text-gray-500">vs previous period</span>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<div class="flex items-start justify-between">
				<div>
					<p class="text-sm text-gray-500">Conversion Rate</p>
					<h3 class="mt-1 text-2xl font-semibold text-gray-800">3.2%</h3>
				</div>
				<div class="rounded-lg bg-yellow-100 p-3">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-6 w-6 text-yellow-600"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
						/>
					</svg>
				</div>
			</div>
			<div class="mt-4 flex items-center">
				<span class="flex items-center text-sm text-red-500">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="mr-1 h-4 w-4"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M12 13a1 1 0 100 2h5a1 1 0 001-1V9a1 1 0 10-2 0v2.586l-4.293-4.293a1 1 0 00-1.414 0L8 9.586 3.707 5.293a1 1 0 00-1.414 1.414l5 5a1 1 0 001.414 0L11 9.414 14.586 13H12z"
							clip-rule="evenodd"
						/>
					</svg>
					0.5%
				</span>
				<span class="ml-2 text-sm text-gray-500">vs previous period</span>
			</div>
		</div>
	</div>

	<!-- Sales Chart -->
	<div class="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
		<div class="rounded-lg bg-white p-6 shadow-md lg:col-span-2">
			<h3 class="mb-4 text-lg font-semibold text-gray-800">Monthly Sales</h3>
			<div class="h-80">
				<div class="flex h-full items-end space-x-2">
					{#each monthlySales as item}
						<div class="flex flex-1 flex-col items-center">
							<div
								class="w-full rounded-t-sm bg-primary-100 hover:bg-primary-200"
								style="height: {(item.amount / maxSales) * 100}%"
							>
								<div
									class="h-full w-full transform rounded-t-sm bg-primary-500 transition-transform hover:translate-y-1"
								></div>
							</div>
							<div class="mt-2 text-xs text-gray-500">{item.month}</div>
						</div>
					{/each}
				</div>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<h3 class="mb-4 text-lg font-semibold text-gray-800">Traffic Sources</h3>
			<div class="space-y-4">
				{#each trafficSources as source}
					<div>
						<div class="mb-1 flex justify-between">
							<span class="text-sm text-gray-700">{source.source}</span>
							<span class="text-sm text-gray-500">{source.percentage}%</span>
						</div>
						<div class="h-2.5 w-full rounded-full bg-gray-200">
							<div
								class="{source.color} h-2.5 rounded-full"
								style="width: {source.percentage}%"
							></div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>

	<!-- Top Selling Products and Location -->
	<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
		<div class="rounded-lg bg-white p-6 shadow-md">
			<h3 class="mb-4 text-lg font-semibold text-gray-800">Top Selling Products</h3>
			<div class="overflow-x-auto">
				<table class="w-full">
					<thead>
						<tr class="bg-gray-50 text-left">
							<th class="px-4 py-3 text-sm font-medium text-gray-500">Product</th>
							<th class="px-4 py-3 text-sm font-medium text-gray-500">Sales</th>
							<th class="px-4 py-3 text-sm font-medium text-gray-500">Revenue</th>
							<th class="px-4 py-3 text-sm font-medium text-gray-500">%</th>
						</tr>
					</thead>
					<tbody>
						{#each topSellingProducts as product}
							<tr class="border-t border-gray-100">
								<td class="px-4 py-3 text-gray-800">{product.name}</td>
								<td class="px-4 py-3 text-gray-800">{product.sales}</td>
								<td class="px-4 py-3 text-gray-800">{product.revenue}</td>
								<td class="px-4 py-3 text-gray-800">{product.percentage}%</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<h3 class="mb-4 text-lg font-semibold text-gray-800">Sales by Location</h3>
			<div class="space-y-4">
				{#each locationData as location}
					<div>
						<div class="mb-1 flex justify-between">
							<span class="text-sm text-gray-700">{location.location}</span>
							<span class="text-sm text-gray-500">{location.sales} ({location.percentage}%)</span>
						</div>
						<div class="h-2.5 w-full rounded-full bg-gray-200">
							<div
								class="h-2.5 rounded-full bg-blue-500"
								style="width: {location.percentage}%"
							></div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
</DashboardLayout>
