<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { SearchFilters, ProductSort } from '../validation/search';
	import { searchFiltersSchema, buildSearchQuery, validateSearchQuery, searchExamples, searchHints } from '../validation/search';
	import Button from './Button.svelte';
	import FormField from './FormField.svelte';
	import Card from './Card.svelte';

	// Props
	export let filters: SearchFilters = {};
	export let query: string = '';
	export let disabled = false;

	// Events
	const dispatch = createEventDispatcher<{
		search: { query: string; filters: SearchFilters };
		clear: void;
	}>();

	// Local state
	let showFilters = false;
	let showExamples = false;
	let validationError = '';
	let isAdvancedMode = false;

	// Reactive statements
	$: queryValid = validateQuery(query);
	$: hasActiveFilters = hasFilters(filters);

	// Methods
	function validateQuery(q: string): boolean {
		const result = validateSearchQuery(q);
		validationError = result.errors.join(', ');
		return result.valid;
	}

	function hasFilters(f: SearchFilters): boolean {
		return !!(f.status?.length || f.categories?.length || f.priceRange || f.dateRange || f.tags?.length || f.textSearch);
	}

	function handleSearch() {
		let searchQuery = query;
		
		if (!isAdvancedMode && hasActiveFilters) {
			// Build query from filters
			searchQuery = buildSearchQuery(filters);
		}
		
		if (queryValid || !searchQuery.trim()) {
			dispatch('search', { query: searchQuery, filters });
		}
	}

	function handleClear() {
		query = '';
		filters = {};
		validationError = '';
		dispatch('clear');
	}

	function toggleAdvancedMode() {
		isAdvancedMode = !isAdvancedMode;
		if (isAdvancedMode) {
			// Convert filters to query string
			query = buildSearchQuery(filters);
			filters = {};
		} else {
			// Parse query string back to filters (simplified)
			parseQueryToFilters(query);
		}
	}

	function parseQueryToFilters(q: string) {
		// Simplified parsing - in a real app you'd use the ANTLR parser
		filters = {};
		
		// Extract status filter
		const statusMatch = q.match(/status:(\w+)/);
		if (statusMatch) {
			filters.status = [statusMatch[1].toUpperCase() as any];
		}
		
		// Extract price range
		const priceRangeMatch = q.match(/price:(\d+(?:\.\d+)?)\.\.(\d+(?:\.\d+)?)/);
		if (priceRangeMatch) {
			filters.priceRange = {
				min: parseFloat(priceRangeMatch[1]),
				max: parseFloat(priceRangeMatch[2]),
				currency: 'USD'
			};
		}
		
		// Extract text search from quotes
		const textMatch = q.match(/"([^"]+)"/);
		if (textMatch) {
			filters.textSearch = textMatch[1];
		}
	}

	function insertExample(example: string) {
		query = example;
	}

	function insertHint(hint: string) {
		const hintQuery = hint.split(' - ')[0];
		if (query.trim()) {
			query += ' AND ' + hintQuery;
		} else {
			query = hintQuery;
		}
	}

	// Predefined options
	const statusOptions = [
		{ value: 'DRAFT', label: 'Draft' },
		{ value: 'PUBLISHED', label: 'Published' },
		{ value: 'ARCHIVED', label: 'Archived' }
	];

	const currencies = ['USD', 'EUR', 'GBP', 'CAD', 'AUD'];
	const dateFields = [
		{ value: 'created', label: 'Created Date' },
		{ value: 'updated', label: 'Updated Date' }
	];
</script>

<Card class="advanced-search">
	<div class="search-header">
		<h3>Product Search</h3>
		<div class="mode-toggle">
			<Button 
				variant={isAdvancedMode ? 'primary' : 'secondary'} 
				size="sm" 
				on:click={toggleAdvancedMode}
				{disabled}
			>
				{isAdvancedMode ? 'Query Mode' : 'Filter Mode'}
			</Button>
		</div>
	</div>

	{#if isAdvancedMode}
		<!-- Advanced Query Mode -->
		<div class="query-mode">
			<FormField 
				label="Search Query" 
				error={validationError}
				help="Use GitHub-style syntax: status:published price:100..200"
			>
				<div class="query-input-group">
					<textarea
						bind:value={query}
						placeholder="Enter search query... (e.g., status:published AND category:electronics)"
						{disabled}
						class="query-textarea"
						class:error={!queryValid && query.trim()}
						rows="3"
					></textarea>
					<div class="query-actions">
						<Button size="sm" on:click={() => showExamples = !showExamples} {disabled}>
							{showExamples ? 'Hide' : 'Show'} Examples
						</Button>
						<Button size="sm" on:click={() => showExamples = !showExamples} {disabled}>
							Hints
						</Button>
					</div>
				</div>
			</FormField>

			{#if showExamples}
				<div class="examples-section">
					<h4>Query Examples</h4>
					<div class="examples-grid">
						{#each searchExamples as example}
							<div class="example-item">
								<div class="example-description">{example.description}</div>
								<div class="example-query">
									<code>{example.query}</code>
									<Button 
										variant="secondary" 
										size="xs" 
										on:click={() => insertExample(example.query)}
										{disabled}
									>
										Use
									</Button>
								</div>
							</div>
						{/each}
					</div>

					<div class="hints-section">
						<h5>Quick Hints</h5>
						<div class="hints-list">
							{#each searchHints as hint}
								<button 
									class="hint-button"
									on:click={() => insertHint(hint)}
									{disabled}
								>
									{hint}
								</button>
							{/each}
						</div>
					</div>
				</div>
			{/if}
		</div>
	{:else}
		<!-- Filter Mode -->
		<div class="filter-mode">
			<div class="quick-search">
				<FormField label="Quick Text Search">
					<input
						type="text"
						bind:value={filters.textSearch}
						placeholder="Search in titles, descriptions..."
						{disabled}
						class="form-input"
					/>
				</FormField>
			</div>

			<div class="filter-toggle">
				<Button 
					variant="secondary" 
					size="sm" 
					on:click={() => showFilters = !showFilters}
					{disabled}
				>
					{showFilters ? 'Hide' : 'Show'} Filters
					{#if hasActiveFilters}
						<span class="filter-count">({Object.keys(filters).length})</span>
					{/if}
				</Button>
			</div>

			{#if showFilters}
				<div class="filters-grid">
					<!-- Status Filter -->
					<FormField label="Status">
						<div class="checkbox-group">
							{#each statusOptions as option}
								<label class="checkbox-label">
									<input
										type="checkbox"
										bind:group={filters.status}
										value={option.value}
										{disabled}
									/>
									{option.label}
								</label>
							{/each}
						</div>
					</FormField>

					<!-- Price Range Filter -->
					<FormField label="Price Range">
						<div class="price-range-inputs">
							<input
								type="number"
								bind:value={filters.priceRange.min}
								placeholder="Min price"
								min="0"
								step="0.01"
								{disabled}
								class="form-input"
							/>
							<span class="range-separator">to</span>
							<input
								type="number"
								bind:value={filters.priceRange.max}
								placeholder="Max price"
								min="0"
								step="0.01"
								{disabled}
								class="form-input"
							/>
							<select 
								bind:value={filters.priceRange.currency}
								{disabled}
								class="form-select"
							>
								{#each currencies as currency}
									<option value={currency}>{currency}</option>
								{/each}
							</select>
						</div>
					</FormField>

					<!-- Date Range Filter -->
					<FormField label="Date Range">
						<div class="date-range-inputs">
							<select 
								bind:value={filters.dateRange.field}
								{disabled}
								class="form-select"
							>
								{#each dateFields as field}
									<option value={field.value}>{field.label}</option>
								{/each}
							</select>
							<input
								type="date"
								bind:value={filters.dateRange.from}
								{disabled}
								class="form-input"
							/>
							<span class="range-separator">to</span>
							<input
								type="date"
								bind:value={filters.dateRange.to}
								{disabled}
								class="form-input"
							/>
						</div>
					</FormField>

					<!-- Categories Filter -->
					<FormField label="Categories">
						<input
							type="text"
							bind:value={filters.categories}
							placeholder="Enter category slugs (comma-separated)"
							{disabled}
							class="form-input"
						/>
					</FormField>

					<!-- Tags Filter -->
					<FormField label="Tags">
						<input
							type="text"
							bind:value={filters.tags}
							placeholder="Enter tags (comma-separated)"
							{disabled}
							class="form-input"
						/>
					</FormField>
				</div>
			{/if}
		</div>
	{/if}

	<!-- Search Actions -->
	<div class="search-actions">
		<Button variant="secondary" on:click={handleClear} {disabled}>
			Clear
		</Button>
		<Button 
			variant="primary" 
			on:click={handleSearch} 
			disabled={disabled || (!queryValid && query.trim())}
		>
			Search
		</Button>
	</div>

	<!-- Active Filters Summary -->
	{#if hasActiveFilters && !isAdvancedMode}
		<div class="active-filters">
			<h4>Active Filters:</h4>
			<div class="filter-tags">
				{#if filters.status?.length}
					<span class="filter-tag">
						Status: {filters.status.join(', ')}
						<button on:click={() => filters.status = undefined}>×</button>
					</span>
				{/if}
				{#if filters.priceRange}
					<span class="filter-tag">
						Price: {filters.priceRange.min || 0} - {filters.priceRange.max || '∞'} {filters.priceRange.currency}
						<button on:click={() => filters.priceRange = undefined}>×</button>
					</span>
				{/if}
				{#if filters.textSearch}
					<span class="filter-tag">
						Text: "{filters.textSearch}"
						<button on:click={() => filters.textSearch = undefined}>×</button>
					</span>
				{/if}
			</div>
		</div>
	{/if}
</Card>

<style>
	.advanced-search {
		margin-bottom: 1.5rem;
	}

	.search-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;
		padding-bottom: 1rem;
		border-bottom: 1px solid var(--color-border);
	}

	.search-header h3 {
		margin: 0;
		font-size: 1.25rem;
		font-weight: 600;
	}

	.query-textarea {
		width: 100%;
		padding: 0.75rem;
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
		font-size: 0.875rem;
		resize: vertical;
		min-height: 80px;
	}

	.query-textarea:focus {
		outline: none;
		border-color: var(--color-blue-500);
		box-shadow: 0 0 0 3px var(--color-blue-100);
	}

	.query-textarea.error {
		border-color: var(--color-red-500);
	}

	.query-input-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.query-actions {
		display: flex;
		gap: 0.5rem;
		justify-content: flex-end;
	}

	.examples-section {
		background-color: var(--color-gray-50);
		border-radius: 0.5rem;
		padding: 1rem;
		margin-top: 1rem;
	}

	.examples-section h4,
	.examples-section h5 {
		margin: 0 0 0.75rem 0;
		font-size: 1rem;
		font-weight: 600;
		color: var(--color-gray-700);
	}

	.examples-grid {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
		margin-bottom: 1.5rem;
	}

	.example-item {
		background-color: var(--color-white);
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		padding: 0.75rem;
	}

	.example-description {
		font-size: 0.875rem;
		color: var(--color-gray-600);
		margin-bottom: 0.5rem;
	}

	.example-query {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 0.5rem;
	}

	.example-query code {
		flex: 1;
		background-color: var(--color-gray-100);
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
		font-size: 0.75rem;
		font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
	}

	.hints-list {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
	}

	.hint-button {
		background-color: var(--color-blue-50);
		border: 1px solid var(--color-blue-200);
		border-radius: 0.25rem;
		padding: 0.25rem 0.5rem;
		font-size: 0.75rem;
		color: var(--color-blue-700);
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.hint-button:hover {
		background-color: var(--color-blue-100);
	}

	.filter-mode {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.quick-search {
		width: 100%;
	}

	.filter-toggle {
		display: flex;
		justify-content: flex-start;
	}

	.filter-count {
		background-color: var(--color-blue-500);
		color: white;
		border-radius: 50%;
		padding: 0.125rem 0.375rem;
		font-size: 0.75rem;
		margin-left: 0.25rem;
	}

	.filters-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
		gap: 1rem;
		padding: 1rem;
		background-color: var(--color-gray-50);
		border-radius: 0.5rem;
	}

	.checkbox-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
		font-size: 0.875rem;
	}

	.price-range-inputs,
	.date-range-inputs {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		flex-wrap: wrap;
	}

	.range-separator {
		color: var(--color-gray-500);
		font-size: 0.875rem;
	}

	.form-input,
	.form-select {
		padding: 0.5rem;
		border: 1px solid var(--color-border);
		border-radius: 0.375rem;
		font-size: 0.875rem;
		flex: 1;
		min-width: 100px;
	}

	.form-input:focus,
	.form-select:focus {
		outline: none;
		border-color: var(--color-blue-500);
		box-shadow: 0 0 0 3px var(--color-blue-100);
	}

	.search-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.5rem;
		margin-top: 1.5rem;
		padding-top: 1rem;
		border-top: 1px solid var(--color-border);
	}

	.active-filters {
		margin-top: 1rem;
		padding: 1rem;
		background-color: var(--color-blue-50);
		border-radius: 0.5rem;
	}

	.active-filters h4 {
		margin: 0 0 0.75rem 0;
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--color-blue-800);
	}

	.filter-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
	}

	.filter-tag {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		background-color: var(--color-blue-100);
		color: var(--color-blue-800);
		padding: 0.25rem 0.5rem;
		border-radius: 0.25rem;
		font-size: 0.75rem;
	}

	.filter-tag button {
		background: none;
		border: none;
		color: var(--color-blue-600);
		cursor: pointer;
		font-size: 1rem;
		line-height: 1;
		padding: 0;
		margin-left: 0.25rem;
	}

	.filter-tag button:hover {
		color: var(--color-blue-800);
	}

	@media (max-width: 768px) {
		.search-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.filters-grid {
			grid-template-columns: 1fr;
		}

		.price-range-inputs,
		.date-range-inputs {
			flex-direction: column;
			align-items: stretch;
		}

		.search-actions {
			flex-direction: column;
		}
	}
</style>