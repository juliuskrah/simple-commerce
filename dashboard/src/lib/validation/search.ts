import { z } from 'zod';

// Search field definitions that match backend capabilities
export const searchFields = [
	'status',
	'title',
	'description', 
	'slug',
	'price',
	'created',
	'updated',
	'category',
	'tags',
	'text' // Full-text search
] as const;

export const SearchField = z.enum(searchFields);

// Search operators matching backend implementation
export const searchOperators = [
	'equals',
	'not_equals',
	'greater_than',
	'less_than',
	'greater_than_or_equal',
	'less_than_or_equal',
	'contains',
	'starts_with',
	'ends_with',
	'range',
	'in',
	'not_in'
] as const;

export const SearchOperator = z.enum(searchOperators);

// Individual search term
export const searchTermSchema = z.object({
	field: SearchField,
	operator: SearchOperator,
	value: z.string().min(1, 'Search value cannot be empty'),
	negated: z.boolean().default(false)
});

// Complete search query
export const searchQueryObjectSchema = z.object({
	terms: z.array(searchTermSchema).min(1, 'At least one search term required'),
	operator: z.enum(['AND', 'OR']).default('AND'),
	raw: z.string().optional() // Original query string
});

// Search validation for raw query strings
export const rawSearchQuerySchema = z
	.string()
	.max(500, 'Search query too long')
	.refine(
		(query) => {
			if (!query.trim()) return true; // Empty queries are allowed
			
			// Check for balanced quotes
			const quoteMatches = query.match(/"/g);
			if (quoteMatches && quoteMatches.length % 2 !== 0) {
				return false;
			}
			
			// Check for balanced parentheses
			let parenCount = 0;
			for (const char of query) {
				if (char === '(') parenCount++;
				if (char === ')') parenCount--;
				if (parenCount < 0) return false;
			}
			return parenCount === 0;
		},
		'Invalid query syntax: unmatched quotes or parentheses'
	)
	.refine(
		(query) => {
			// Basic field:value pattern validation
			const fieldValuePattern = /(\w+):/g;
			const matches = [...query.matchAll(fieldValuePattern)];
			
			// Check if all field names are valid
			return matches.every(match => {
				const fieldName = match[1].toLowerCase();
				return searchFields.includes(fieldName as any);
			});
		},
		'Invalid field name in search query'
	)
	.optional();

// Search filters for UI components
export const searchFiltersSchema = z.object({
	status: z.array(z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED'])).optional(),
	categories: z.array(z.string()).optional(),
	priceRange: z.object({
		min: z.number().min(0).optional(),
		max: z.number().min(0).optional(),
		currency: z.string().length(3).default('USD')
	}).optional(),
	dateRange: z.object({
		field: z.enum(['created', 'updated']),
		from: z.string().datetime().optional(),
		to: z.string().datetime().optional()
	}).optional(),
	tags: z.array(z.string()).optional(),
	textSearch: z.string().max(200).optional()
});

// Product sort options
export const productSortSchema = z.object({
	field: z.enum(['title', 'createdAt', 'updatedAt', 'status']),
	direction: z.enum(['ASC', 'DESC']).default('ASC')
});

// Pagination schema
export const paginationSchema = z.object({
	first: z.number().int().min(1).max(100).optional(),
	after: z.string().optional(),
	last: z.number().int().min(1).max(100).optional(),
	before: z.string().optional()
});

// Export types
export type SearchTerm = z.infer<typeof searchTermSchema>;
export type SearchQueryObject = z.infer<typeof searchQueryObjectSchema>;
export type RawSearchQuery = z.infer<typeof rawSearchQuerySchema>;
export type SearchFilters = z.infer<typeof searchFiltersSchema>;
export type ProductSort = z.infer<typeof productSortSchema>;
export type Pagination = z.infer<typeof paginationSchema>;

// Helper functions for search query building
export const buildSearchQuery = (filters: SearchFilters): string => {
	const parts: string[] = [];
	
	// Status filter
	if (filters.status && filters.status.length > 0) {
		if (filters.status.length === 1) {
			parts.push(`status:${filters.status[0].toLowerCase()}`);
		} else {
			const statusQuery = filters.status.map(s => `status:${s.toLowerCase()}`).join(' OR ');
			parts.push(`(${statusQuery})`);
		}
	}
	
	// Price range filter
	if (filters.priceRange) {
		const { min, max, currency } = filters.priceRange;
		if (min !== undefined && max !== undefined) {
			parts.push(`price:${min}..${max}`);
		} else if (min !== undefined) {
			parts.push(`price:>${min}`);
		} else if (max !== undefined) {
			parts.push(`price:<${max}`);
		}
	}
	
	// Date range filter
	if (filters.dateRange && (filters.dateRange.from || filters.dateRange.to)) {
		const { field, from, to } = filters.dateRange;
		if (from && to) {
			const fromDate = from.split('T')[0]; // Extract date part
			const toDate = to.split('T')[0];
			parts.push(`${field}:${fromDate}..${toDate}`);
		} else if (from) {
			const fromDate = from.split('T')[0];
			parts.push(`${field}:>${fromDate}`);
		} else if (to) {
			const toDate = to.split('T')[0];
			parts.push(`${field}:<${toDate}`);
		}
	}
	
	// Categories filter
	if (filters.categories && filters.categories.length > 0) {
		if (filters.categories.length === 1) {
			parts.push(`category:${filters.categories[0]}`);
		} else {
			const categoryQuery = filters.categories.map(c => `category:${c}`).join(' OR ');
			parts.push(`(${categoryQuery})`);
		}
	}
	
	// Tags filter
	if (filters.tags && filters.tags.length > 0) {
		filters.tags.forEach(tag => {
			parts.push(`tags:${tag}`);
		});
	}
	
	// Text search
	if (filters.textSearch) {
		parts.push(`"${filters.textSearch}"`);
	}
	
	return parts.join(' AND ');
};

// Query validation helper
export const validateSearchQuery = (query: string): { valid: boolean; errors: string[] } => {
	const errors: string[] = [];
	
	try {
		rawSearchQuerySchema.parse(query);
	} catch (error) {
		if (error instanceof z.ZodError) {
			errors.push(...error.errors.map(e => e.message));
		} else {
			errors.push('Invalid search query format');
		}
	}
	
	return {
		valid: errors.length === 0,
		errors
	};
};

// Search suggestions/hints
export const searchHints = [
	'status:published - Find published products',
	'price:100..200 - Price range between 100 and 200',
	'category:electronics - Products in electronics category',
	'created:2024-01-01..2024-12-31 - Created this year',
	'title:"wireless headphones" - Exact title match',
	'status:published AND category:electronics - Multiple conditions'
];

export const searchExamples = [
	{
		description: 'Published electronics products',
		query: 'status:published AND category:electronics'
	},
	{
		description: 'Products in price range $100-200',
		query: 'status:published AND price:100..200'
	},
	{
		description: 'Products created this month',
		query: 'created:2024-01-01..2024-01-31'
	},
	{
		description: 'Draft or published wireless products',
		query: '(status:draft OR status:published) AND title:wireless'
	}
];