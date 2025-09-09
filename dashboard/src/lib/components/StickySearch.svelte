<script lang="ts">
	interface Props {
		placeholder?: string;
		value?: string;
		onSearch?: (query: string) => void;
		class?: string;
	}

	let {
		placeholder = "Search...",
		value = "",
		onSearch = () => {},
		class: className = ""
	}: Props = $props();

	let searchQuery = $state(value);

	function handleInput(event: Event) {
		const target = event.target as HTMLInputElement;
		searchQuery = target.value;
		if (onSearch) {
			onSearch(searchQuery);
		}
	}

	function handleKeyDown(event: KeyboardEvent) {
		if (event.key === 'Enter' && onSearch) {
			onSearch(searchQuery);
		}
	}

	function handleSearchClick() {
		if (onSearch) {
			onSearch(searchQuery);
		}
	}
</script>

<div class="sticky top-0 z-40 bg-white border-b border-gray-200 {className}">
	<div class="px-6 py-3">
		<div class="relative max-w-md">
			<input
				type="text"
				bind:value={searchQuery}
				oninput={handleInput}
				onkeydown={handleKeyDown}
				{placeholder}
				class="w-full rounded-md border border-gray-300 bg-gray-50 px-3 py-1.5 pr-8 text-sm placeholder-gray-500 transition-colors focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-1 focus:ring-blue-500"
			/>
			<button
				onclick={handleSearchClick}
				class="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-gray-400 hover:text-gray-600 focus:outline-none focus:text-gray-600"
				aria-label="Search"
			>
				<svg
					class="h-4 w-4"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
					></path>
				</svg>
			</button>
		</div>
	</div>
</div>