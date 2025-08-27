import type { PageServerLoad, Actions } from './$types';
import { fail, redirect } from '@sveltejs/kit';
import { AddProductVariantStore } from '$houdini';

export const load: PageServerLoad = async (event) => {
	return {
		...(await event.parent())
	};
};

export const actions: Actions = {
	default: async (event) => {
		const formData = await event.request.formData();
		
		const title = formData.get('title') as string;
		const sku = formData.get('sku') as string;
		const amount = parseFloat(formData.get('amount') as string);
		const currency = formData.get('currency') as string;
		
		if (!title || !sku) {
			return fail(400, { message: 'Title and SKU are required' });
		}
		
		try {
			// Create the mutation store and execute on the server side
			const addVariantMutation = new AddProductVariantStore();
			
			const input: any = {
				title,
				sku
			};
			
			// Add price if provided
			if (amount > 0) {
				input.price = {
					amount,
					currency: currency || 'USD'
				};
			}
			
			const result = await addVariantMutation.mutate({
				productId: event.params.id,
				input
			}, {
				event // Pass the event for server-side context and authentication
			});
			
			if (result.errors) {
				console.error('GraphQL errors:', result.errors);
				return fail(500, { message: result.errors.map((e: any) => e.message).join(', ') });
			}
			
		} catch (error) {
			console.error('Error creating variant:', error);
			return fail(500, { message: 'Failed to create variant' });
		}
		
		// Redirect on success
		redirect(302, `/dashboard/products/${event.params.id}`);
	}
};
