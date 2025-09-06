import { z } from 'zod';

// Price Context Types enum matching backend
export const priceContextTypes = [
	'GEOGRAPHIC',
	'CURRENCY', 
	'CUSTOMER_GROUP',
	'DEFAULT'
] as const;

export const PriceContextType = z.enum(priceContextTypes);

// Money input validation
export const moneyInputSchema = z.object({
	amount: z.coerce
		.number()
		.min(0, 'Amount must be non-negative')
		.max(999999.99, 'Amount too large')
		.refine((val) => Number.isFinite(val), 'Amount must be a valid number')
		.refine(
			(val) => Number((val * 100).toFixed(0)) / 100 === val,
			'Amount can have at most 2 decimal places'
		),
	currency: z
		.string()
		.length(3, 'Currency must be 3 characters (ISO 4217)')
		.regex(/^[A-Z]{3}$/, 'Currency must be uppercase letters only')
		.default('USD')
});

// Price Context input validation
export const priceContextInputSchema = z.object({
	customerGroup: z.string().optional(),
	region: z
		.string()
		.regex(/^[A-Z]{2,3}$/, 'Region must be 2-3 uppercase letters (ISO country code)')
		.optional(),
	currency: z
		.string()
		.length(3, 'Currency must be 3 characters')
		.regex(/^[A-Z]{3}$/, 'Currency must be uppercase')
		.default('USD'),
	quantity: z.coerce.number().int().min(1, 'Quantity must be at least 1').default(1)
});

// Price Rule validation
export const priceRuleInputSchema = z
	.object({
		contextType: PriceContextType,
		contextValue: z.string().optional(),
		price: moneyInputSchema,
		minQuantity: z.coerce.number().int().min(1, 'Minimum quantity must be at least 1').optional(),
		maxQuantity: z.coerce.number().int().min(1, 'Maximum quantity must be at least 1').optional(),
		validFrom: z.string().datetime().optional(),
		validUntil: z.string().datetime().optional(),
		active: z.boolean().default(true)
	})
	.superRefine((data, ctx) => {
		// Validate context value requirements
		if (data.contextType !== 'DEFAULT' && !data.contextValue) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['contextValue'],
				message: `Context value is required for ${data.contextType} context type`
			});
		}

		if (data.contextType === 'DEFAULT' && data.contextValue) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['contextValue'],
				message: 'Context value should not be set for DEFAULT context type'
			});
		}

		// Validate quantity range
		if (data.minQuantity && data.maxQuantity && data.minQuantity > data.maxQuantity) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['maxQuantity'],
				message: 'Maximum quantity must be greater than or equal to minimum quantity'
			});
		}

		// Validate date range
		if (data.validFrom && data.validUntil) {
			const fromDate = new Date(data.validFrom);
			const untilDate = new Date(data.validUntil);
			if (fromDate >= untilDate) {
				ctx.addIssue({
					code: z.ZodIssueCode.custom,
					path: ['validUntil'],
					message: 'Valid until date must be after valid from date'
				});
			}
		}

		// Validate context-specific rules
		if (data.contextType === 'CURRENCY' && data.contextValue) {
			if (!/^[A-Z]{3}$/.test(data.contextValue)) {
				ctx.addIssue({
					code: z.ZodIssueCode.custom,
					path: ['contextValue'],
					message: 'Currency context value must be a 3-letter ISO code (e.g., USD, EUR)'
				});
			}
		}

		if (data.contextType === 'GEOGRAPHIC' && data.contextValue) {
			if (!/^[A-Z]{2,3}$/.test(data.contextValue)) {
				ctx.addIssue({
					code: z.ZodIssueCode.custom,
					path: ['contextValue'],
					message: 'Geographic context value must be a 2-3 letter country/region code (e.g., US, EU)'
				});
			}
		}

		if (data.contextType === 'CUSTOMER_GROUP' && data.contextValue) {
			const validGroups = ['B2B', 'B2C', 'VIP', 'WHOLESALE', 'RETAIL'];
			if (!validGroups.includes(data.contextValue.toUpperCase())) {
				ctx.addIssue({
					code: z.ZodIssueCode.custom,
					path: ['contextValue'],
					message: `Customer group must be one of: ${validGroups.join(', ')}`
				});
			}
		}
	});

// Price Set validation
export const priceSetInputSchema = z
	.object({
		name: z
			.string()
			.min(2, 'Price set name must be at least 2 characters')
			.max(100, 'Price set name too long'),
		priority: z.coerce.number().int().min(0, 'Priority must be non-negative').default(0),
		active: z.boolean().default(true),
		rules: z
			.array(priceRuleInputSchema)
			.min(1, 'At least one pricing rule is required')
			.max(50, 'Too many pricing rules (maximum 50)')
	})
	.superRefine((data, ctx) => {
		// Ensure there's at least one DEFAULT rule
		const hasDefaultRule = data.rules.some((rule) => rule.contextType === 'DEFAULT');
		if (!hasDefaultRule) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['rules'],
				message: 'At least one DEFAULT pricing rule is required as fallback'
			});
		}

		// Check for duplicate context combinations
		const contextKeys = new Set<string>();
		data.rules.forEach((rule, index) => {
			const key = `${rule.contextType}-${rule.contextValue || 'null'}-${rule.minQuantity || 'null'}-${rule.maxQuantity || 'null'}`;
			if (contextKeys.has(key)) {
				ctx.addIssue({
					code: z.ZodIssueCode.custom,
					path: ['rules', index],
					message: 'Duplicate pricing rule for the same context and quantity range'
				});
			}
			contextKeys.add(key);
		});
	});

// Search query validation
export const searchQuerySchema = z
	.string()
	.max(500, 'Search query too long')
	.refine(
		(query) => {
			// Basic validation for balanced quotes and parentheses
			const quoteCount = (query.match(/"/g) || []).length;
			const openParens = (query.match(/\(/g) || []).length;
			const closeParens = (query.match(/\)/g) || []).length;
			
			return quoteCount % 2 === 0 && openParens === closeParens;
		},
		'Invalid query syntax: unmatched quotes or parentheses'
	)
	.optional();

// Product lifecycle validation
export const productStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']);

// Export types
export type MoneyInput = z.infer<typeof moneyInputSchema>;
export type PriceContextInput = z.infer<typeof priceContextInputSchema>;
export type PriceRuleInput = z.infer<typeof priceRuleInputSchema>;
export type PriceSetInput = z.infer<typeof priceSetInputSchema>;
export type SearchQuery = z.infer<typeof searchQuerySchema>;
export type ProductStatus = z.infer<typeof productStatusSchema>;

// Helper functions for validation
export const validateCurrency = (currency: string): boolean => {
	return /^[A-Z]{3}$/.test(currency);
};

export const validateCountryCode = (code: string): boolean => {
	return /^[A-Z]{2,3}$/.test(code);
};

export const formatMoney = (amount: number, currency: string): string => {
	return new Intl.NumberFormat('en-US', {
		style: 'currency',
		currency: currency,
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	}).format(amount);
};

export const parseMoney = (moneyString: string): { amount: number; currency: string } | null => {
	const match = moneyString.match(/^([A-Z]{3})\s*([0-9,]+\.?[0-9]*)$/);
	if (!match) return null;
	
	const [, currency, amountStr] = match;
	const amount = parseFloat(amountStr.replace(/,/g, ''));
	
	if (isNaN(amount)) return null;
	
	return { amount, currency };
};