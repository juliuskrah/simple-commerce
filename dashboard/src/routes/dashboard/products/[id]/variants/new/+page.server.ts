// touch: force rebuild to ensure thrown redirect is reflected in compiled output
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

function redirectToProduct(id: string): never {
	// central helper so generated output must include thrown redirect
	throw redirect(302, `/dashboard/products/${id}?variantCreated=1`);
}

export const actions: Actions = {
	create: async (event) => {
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
			const result = await addVariantMutation.mutate(
				{ productId: event.params.id, input },
				{ event }
			);
			if (result.errors) {
				return fail(500, { form, message: result.errors.map((e: any) => e.message).join(', ') });
			}
			console.log('[variant:new] created variant, redirecting');
		} catch (error) {
			console.error('Error creating variant:', error);
			return fail(500, { form, message: 'Failed to create variant' });
		}
		redirectToProduct(event.params.id);
	}
};
