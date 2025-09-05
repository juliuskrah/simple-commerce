import type { PageServerLoad, Actions } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { AddProductVariantStore } from '$houdini';
import { variantSchema } from '$lib/validation/variant';
import { superValidate } from 'sveltekit-superforms/server';
import { zod } from 'sveltekit-superforms/adapters';

export const load: PageServerLoad = async (event) => {
	const parent = await event.parent();
	const form = await superValidate(zod(variantSchema));
	return { ...parent, form };
};

export const actions: Actions = {
	default: async (event) => {
		const form = await superValidate(event, zod(variantSchema));
		if (!form.valid) {
			return fail(400, { form });
		}
		try {
			const addVariantMutation = new AddProductVariantStore();
			const input: any = { title: form.data.title, sku: form.data.sku };
			if (form.data.amount && form.data.amount > 0) {
				input.price = { amount: form.data.amount, currency: form.data.currency || 'USD' };
			}
			const result = await addVariantMutation.mutate({ productId: event.params.id, input }, { event });
			if (result.errors) {
				return fail(500, { form, message: result.errors.map((e: any) => e.message).join(', ') });
			}
		} catch (error) {
			console.error('Error creating variant:', error);
			return fail(500, { form, message: 'Failed to create variant' });
		}
		throw redirect(302, `/dashboard/products/${event.params.id}?variantCreated=1`);
	}
};
