import { describe, it, expect } from 'vitest';
import { variantSchema } from '../src/lib/validation/variant';

// Helper to parse with safe fallback
function validate(data: any) {
  const parsed = variantSchema.safeParse(data);
  return parsed;
}

describe('variantSchema', () => {
  it('accepts minimal valid data', () => {
    const result = validate({ title: 'Standard Edition', sku: 'STD-001' });
    expect(result.success).toBe(true);
  });

  it('rejects short title', () => {
    const result = validate({ title: 'A', sku: 'STD-001' });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.flatten().fieldErrors.title?.[0]).toMatch(/at least 2/i);
    }
  });

  it('rejects invalid sku characters', () => {
    const result = validate({ title: 'Good Title', sku: 'bad sku' });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.flatten().fieldErrors.sku?.[0]).toMatch(/SKU may contain/i);
    }
  });

  it('allows price when amount > 0 with currency', () => {
    const result = validate({ title: 'Deluxe', sku: 'DLX-001', amount: '19.99', currency: 'USD' });
    expect(result.success).toBe(true);
  });

  it('defaults amount to 0 when blank string provided', () => {
    const result = validate({ title: 'Basic', sku: 'BSC-100', amount: '' });
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.amount).toBe(0);
    }
  });

  it('requires currency when amount > 0', () => {
    const result = validate({ title: 'Pro', sku: 'PRO-1', amount: '10.00', currency: '' });
    expect(result.success).toBe(false);
  });

  it('rejects negative amount', () => {
    const result = validate({ title: 'Bad', sku: 'BAD-1', amount: '-5', currency: 'USD' });
    expect(result.success).toBe(false);
  });
});
