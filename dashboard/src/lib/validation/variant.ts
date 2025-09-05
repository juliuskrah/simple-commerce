import { z } from 'zod';

export const variantSchema = z.object({
  title: z.string().min(2, 'Title must be at least 2 characters').max(120, 'Title too long'),
  sku: z.string()
    .min(2, 'SKU must be at least 2 characters')
    .max(64, 'SKU too long')
    .regex(/^[A-Z0-9._-]+$/, 'SKU may contain A-Z, 0-9, dash, underscore and dot'),
  amount: z.coerce.number().nonnegative().optional().default(0),
  currency: z.string().optional().default('USD')
}).superRefine((data, ctx) => {
  if (data.amount && data.amount > 0 && !data.currency) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['currency'], message: 'Currency required when amount is set' });
  }
});

export type VariantSchema = typeof variantSchema;
export type VariantInput = z.infer<VariantSchema>;
