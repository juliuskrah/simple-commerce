import type { Actions, ServerLoad } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms/server';
import { zod } from 'sveltekit-superforms/adapters';
import { variantSchema } from '$lib/validation/variant';
import { UpdateProductVariantStore } from '$houdini';
import { fail, redirect } from '@sveltejs/kit';

export const load: ServerLoad = async (event) => {
  const parent = await event.parent();
  // Preload variant details via parent query already fetched in +page.gql store
  const form = await superValidate(zod(variantSchema));
  return { ...parent, form };
};

export const actions: Actions = {
  update: async (event) => {
    const form = await superValidate(event, zod(variantSchema));
    if (!form.valid) {
      return fail(400, { form });
    }
    try {
      const updateStore = new UpdateProductVariantStore();
      const input: any = { title: form.data.title, sku: form.data.sku };
      if (form.data.amount && form.data.amount > 0) {
        input.price = { amount: form.data.amount, currency: form.data.currency || 'USD' };
      }
      const variantId = event.params.variantId as string;
      const result = await updateStore.mutate({ id: variantId, input }, { event });
      if (result.errors) {
        return fail(500, { form, message: result.errors.map((e: any) => e.message).join(', ') });
      }
    } catch (e) {
      console.error('Error updating variant', e);
      return fail(500, { form, message: 'Failed to update variant' });
    }
    throw redirect(302, `/dashboard/products/${event.params.id}?updated=1`);
  }
};
