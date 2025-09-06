import { z } from 'zod';
import { moneyInputSchema, priceSetInputSchema } from './pricing';

export const variantSchema = z
	.object({
		title: z.string().min(2, 'Title must be at least 2 characters').max(120, 'Title too long'),
		sku: z
			.string()
			.min(2, 'SKU must be at least 2 characters')
			.max(64, 'SKU too long')
			.regex(/^[A-Z0-9._-]+$/, 'SKU may contain A-Z, 0-9, dash, underscore and dot'),

		// Legacy price fields (for backward compatibility)
		amount: z.coerce.number().nonnegative().optional(),
		currency: z.string().optional().default('USD'),

		// New contextual pricing
		price: moneyInputSchema.optional(),
		priceSets: z.array(priceSetInputSchema).optional()
	})
	.superRefine((data, ctx) => {
		// Legacy validation for backward compatibility
		if (data.amount && data.amount > 0 && !data.currency) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['currency'],
				message: 'Currency required when amount is set'
			});
		}

		// Ensure either legacy price or new pricing is provided
		const hasLegacyPrice = data.amount && data.amount > 0;
		const hasNewPrice = data.price || (data.priceSets && data.priceSets.length > 0);

		if (!hasLegacyPrice && !hasNewPrice) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['price'],
				message: 'Either legacy price or contextual pricing must be provided'
			});
		}

		// Validate price sets don't exceed limit
		if (data.priceSets && data.priceSets.length > 10) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				path: ['priceSets'],
				message: 'Maximum 10 price sets allowed per variant'
			});
		}
	});

// Enhanced variant input for the new system
export const enhancedVariantSchema = z.object({
	title: z.string().min(2, 'Title must be at least 2 characters').max(120, 'Title too long'),
	sku: z
		.string()
		.min(2, 'SKU must be at least 2 characters')
		.max(64, 'SKU too long')
		.regex(/^[A-Z0-9._-]+$/, 'SKU may contain A-Z, 0-9, dash, underscore and dot'),
	
	// Default price (required)
	defaultPrice: moneyInputSchema,
	
	// Additional price sets for contextual pricing
	additionalPriceSets: z.array(priceSetInputSchema).max(10, 'Maximum 10 price sets allowed').optional()
});

export type VariantSchema = typeof variantSchema;
export type VariantInput = z.infer<VariantSchema>;
export type EnhancedVariantInput = z.infer<typeof enhancedVariantSchema>;
