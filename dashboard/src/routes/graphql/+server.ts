// E2E mock GraphQL endpoint (enabled when E2E_GRAPHQL_MOCK=1)
import type { RequestHandler } from '@sveltejs/kit';

interface Variant { id: string; title: string; sku: string; price: any; createdAt: string; updatedAt: string }
interface ProductState { product: { id: string; title: string; description: string; slug: string; status: string; priceRange: any; category: any; createdAt: string; updatedAt: string; variants: Variant[] } }
const state: ProductState = {
  product: {
    id: 'prod_1',
    title: 'Test Product for Variants',
    description: 'E2E product',
    slug: 'e2e-product',
    status: 'ACTIVE',
    priceRange: { start: { amount: 0, currency: 'USD' }, stop: { amount: 0, currency: 'USD' } },
    category: null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    variants: []
  }
};

function baseVariantFields(v: Variant) {
  return {
    id: v.id,
    title: v.title,
    sku: v.sku,
    price: v.price,
    createdAt: v.createdAt,
    updatedAt: v.updatedAt,
    product: { id: state.product.id }
  };
}

export const POST: RequestHandler = async ({ request }) => {
  if (process.env.E2E_GRAPHQL_MOCK !== '1') {
    return new Response('GraphQL mock disabled', { status: 404 });
  }
  const body = await request.json();
  const op = body.operationName;

  if (op === 'ProductDetail') {
    return new Response(
      JSON.stringify({
        data: {
          product: {
            ...state.product,
            variants: { edges: state.product.variants.map((v) => ({ node: baseVariantFields(v) })) }
          }
        }
      }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    );
  }
  if (op === 'AddProductVariant') {
    const input = body.variables.input;
    const now = new Date().toISOString();
    const variant: Variant = {
      id: 'var_' + Date.now(),
      title: input.title,
      sku: input.sku,
      price: input.price ?? null,
      createdAt: now,
      updatedAt: now
    };
    state.product.variants.push(variant);
    return new Response(JSON.stringify({ data: { addProductVariant: baseVariantFields(variant) } }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  }
  if (op === 'UpdateProductVariant') {
    const { id, input } = body.variables;
    const existing = state.product.variants.find((v) => v.id === id);
    if (existing) {
      Object.assign(existing, input, { updatedAt: new Date().toISOString() });
    }
    return new Response(JSON.stringify({ data: { updateProductVariant: baseVariantFields(existing!) } }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  }
  if (op === 'DeleteProductVariant') {
    const { id } = body.variables;
    const idx = state.product.variants.findIndex((v) => v.id === id);
    if (idx !== -1) state.product.variants.splice(idx, 1);
    return new Response(JSON.stringify({ data: { deleteProductVariant: true } }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  }
  return new Response(JSON.stringify({ data: {} }), { status: 200, headers: { 'Content-Type': 'application/json' } });
};