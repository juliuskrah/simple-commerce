<!-- Customer Categories Page -->
<script lang="ts">
	import { onMount } from 'svelte';
	
	let categories = $state([]);
	let loading = $state(true);
	
	onMount(() => {
		loadCategories();
	});
	
	async function loadCategories() {
		try {
			const response = await fetch('/graphql', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					query: `
						query StorefrontCategories {
							categories {
								edges {
									node {
										id
										name
										description
										slug
										parent {
											id
											name
										}
										children {
											edges {
												node {
													id
													name
													slug
												}
											}
										}
									}
								}
							}
						}
					`
				})
			});
			
			const data = await response.json();
			if (data.data) {
				categories = data.data.categories.edges.map(edge => edge.node);
			}
		} catch (error) {
			console.error('Error loading categories:', error);
		} finally {
			loading = false;
		}
	}
	
	function getTopLevelCategories() {
		return categories.filter(category => !category.parent);
	}
	
	function getCategoryIcon(category) {
		// Simple icon based on first letter of category name
		return category.name.charAt(0).toUpperCase();
	}
	
	function getCategoryUrl(category) {
		return `/storefront/products?category=${category.id}`;
	}
</script>

<svelte:head>
	<title>Categories - Simple Commerce</title>
	<meta name="description" content="Browse our product categories to find exactly what you're looking for. Shop by category for a better shopping experience." />
</svelte:head>

<div class="categories-page">
	<div class="page-header">
		<h1>Product Categories</h1>
		<p class="page-description">Browse our organized collection of product categories</p>
	</div>
	
	{#if loading}
		<div class="loading">
			<div class="loading-spinner"></div>
			<p>Loading categories...</p>
		</div>
	{:else if categories.length === 0}
		<div class="empty-state">
			<h2>No Categories Available</h2>
			<p>We're organizing our products. Check back soon for categorized browsing!</p>
			<a href="/storefront/products" class="btn-primary">Browse All Products</a>
		</div>
	{:else}
		<!-- Main Categories Grid -->
		<div class="categories-grid">
			{#each getTopLevelCategories() as category}
				<div class="category-card">
					<a href={getCategoryUrl(category)} class="category-link">
						<div class="category-icon">
							<span>{getCategoryIcon(category)}</span>
						</div>
						<div class="category-info">
							<h3>{category.name}</h3>
							{#if category.description}
								<p class="category-description">{category.description}</p>
							{/if}
						</div>
						<div class="category-arrow">
							<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
								<polyline points="9,18 15,12 9,6"></polyline>
							</svg>
						</div>
					</a>
					
					<!-- Subcategories -->
					{#if category.children?.edges?.length > 0}
						<div class="subcategories">
							<h4>Subcategories</h4>
							<div class="subcategory-links">
								{#each category.children.edges as { node: subcategory }}
									<a href={getCategoryUrl(subcategory)} class="subcategory-link">
										{subcategory.name}
									</a>
								{/each}
							</div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
		
		<!-- Category Tree View -->
		{#if categories.some(cat => cat.children?.edges?.length > 0)}
			<div class="category-tree-section">
				<h2>Category Tree</h2>
				<div class="category-tree">
					{#each getTopLevelCategories() as category}
						<div class="tree-item">
							<div class="tree-node">
								<a href={getCategoryUrl(category)} class="tree-link main-category">
									<span class="tree-icon">{getCategoryIcon(category)}</span>
									{category.name}
								</a>
							</div>
							{#if category.children?.edges?.length > 0}
								<div class="tree-children">
									{#each category.children.edges as { node: subcategory }}
										<div class="tree-child">
											<a href={getCategoryUrl(subcategory)} class="tree-link subcategory">
												<span class="tree-connector">└</span>
												{subcategory.name}
											</a>
										</div>
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				</div>
			</div>
		{/if}
	{/if}
</div>

<style>
	.categories-page {
		max-width: 1200px;
		margin: 0 auto;
		padding: 0 1rem;
	}
	
	.page-header {
		text-align: center;
		margin-bottom: 3rem;
	}
	
	.page-header h1 {
		font-size: 2.5rem;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 0.5rem;
	}
	
	.page-description {
		color: #6b7280;
		font-size: 1.125rem;
	}
	
	.loading {
		text-align: center;
		padding: 4rem 0;
	}
	
	.loading-spinner {
		width: 40px;
		height: 40px;
		border: 4px solid #f3f4f6;
		border-top: 4px solid #3b82f6;
		border-radius: 50%;
		animation: spin 1s linear infinite;
		margin: 0 auto 1rem;
	}
	
	@keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	}
	
	.empty-state {
		text-align: center;
		padding: 4rem 0;
	}
	
	.empty-state h2 {
		font-size: 1.5rem;
		color: #6b7280;
		margin-bottom: 1rem;
	}
	
	.empty-state p {
		color: #9ca3af;
		margin-bottom: 2rem;
	}
	
	.btn-primary {
		background-color: #3b82f6;
		color: white;
		text-decoration: none;
		padding: 0.75rem 1.5rem;
		border-radius: 0.5rem;
		font-weight: 600;
		display: inline-block;
	}
	
	.btn-primary:hover {
		background-color: #2563eb;
	}
	
	.categories-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
		gap: 1.5rem;
		margin-bottom: 3rem;
	}
	
	.category-card {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		overflow: hidden;
		transition: all 0.2s;
	}
	
	.category-card:hover {
		transform: translateY(-2px);
		box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
	}
	
	.category-link {
		display: flex;
		align-items: center;
		padding: 1.5rem;
		text-decoration: none;
		color: inherit;
		gap: 1rem;
		transition: background-color 0.2s;
	}
	
	.category-link:hover {
		background-color: #f9fafb;
	}
	
	.category-icon {
		width: 60px;
		height: 60px;
		border-radius: 50%;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: white;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 1.5rem;
		font-weight: 700;
		flex-shrink: 0;
	}
	
	.category-info {
		flex: 1;
	}
	
	.category-info h3 {
		font-size: 1.25rem;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 0.25rem;
	}
	
	.category-description {
		color: #6b7280;
		font-size: 0.875rem;
		line-height: 1.4;
		margin: 0;
	}
	
	.category-arrow {
		color: #9ca3af;
		flex-shrink: 0;
	}
	
	.subcategories {
		border-top: 1px solid #f3f4f6;
		padding: 1rem 1.5rem;
	}
	
	.subcategories h4 {
		font-size: 0.875rem;
		font-weight: 600;
		color: #6b7280;
		margin-bottom: 0.75rem;
		text-transform: uppercase;
		letter-spacing: 0.025em;
	}
	
	.subcategory-links {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
	}
	
	.subcategory-link {
		background-color: #f3f4f6;
		color: #6b7280;
		text-decoration: none;
		padding: 0.5rem 0.75rem;
		border-radius: 0.375rem;
		font-size: 0.875rem;
		font-weight: 500;
		transition: all 0.2s;
	}
	
	.subcategory-link:hover {
		background-color: #e5e7eb;
		color: #1f2937;
	}
	
	.category-tree-section {
		margin-top: 4rem;
		padding-top: 2rem;
		border-top: 1px solid #e5e7eb;
	}
	
	.category-tree-section h2 {
		font-size: 1.5rem;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 1.5rem;
		text-align: center;
	}
	
	.category-tree {
		background: white;
		border: 1px solid #e5e7eb;
		border-radius: 0.75rem;
		padding: 1.5rem;
	}
	
	.tree-item {
		margin-bottom: 1.5rem;
	}
	
	.tree-item:last-child {
		margin-bottom: 0;
	}
	
	.tree-node {
		margin-bottom: 0.5rem;
	}
	
	.tree-link {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		text-decoration: none;
		color: inherit;
		padding: 0.5rem;
		border-radius: 0.375rem;
		transition: background-color 0.2s;
	}
	
	.tree-link:hover {
		background-color: #f9fafb;
	}
	
	.tree-link.main-category {
		font-weight: 600;
		color: #1f2937;
	}
	
	.tree-link.subcategory {
		color: #6b7280;
		font-size: 0.875rem;
		margin-left: 1rem;
	}
	
	.tree-icon {
		width: 24px;
		height: 24px;
		border-radius: 50%;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: white;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 0.75rem;
		font-weight: 700;
		flex-shrink: 0;
	}
	
	.tree-connector {
		color: #d1d5db;
		font-family: monospace;
		font-weight: normal;
	}
	
	.tree-children {
		padding-left: 1rem;
		border-left: 2px solid #f3f4f6;
		margin-left: 12px;
	}
	
	.tree-child {
		margin-bottom: 0.25rem;
	}
	
	@media (max-width: 768px) {
		.page-header h1 {
			font-size: 2rem;
		}
		
		.categories-grid {
			grid-template-columns: 1fr;
		}
		
		.category-link {
			padding: 1rem;
		}
		
		.category-icon {
			width: 50px;
			height: 50px;
			font-size: 1.25rem;
		}
		
		.subcategories {
			padding: 1rem;
		}
		
		.category-tree {
			padding: 1rem;
		}
	}
</style>