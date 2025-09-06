import { fail, redirect } from '@sveltejs/kit';
import { UpdateProductStore } from '$houdini';
import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ parent }) => {
	const data = await parent();
	return {
		user: data.user
	};
};

export const actions: Actions = {
	update: async ({ request, params }) => {
		const data = await request.formData();
		const title = data.get('title') as string;
		const description = data.get('description') as string;
		const price = data.get('price') as string;
		const tags = data.get('tags') as string;
		const status = data.get('status') as string;

		// Basic validation
		const errors: Record<string, string> = {};
		
		if (!title?.trim()) {
			errors.title = 'Product title is required';
		}
		
		if (price && (isNaN(Number(price)) || Number(price) < 0)) {
			errors.price = 'Price must be a valid positive number';
		}

		if (status && !['DRAFT', 'PUBLISHED', 'ARCHIVED'].includes(status)) {
			errors.status = 'Invalid product status';
		}

		if (Object.keys(errors).length > 0) {
			return fail(400, { errors });
		}

		// Prepare input
		const input: any = {
			title: title.trim(),
			status: status || 'DRAFT'
		};

		if (description?.trim()) {
			input.description = description.trim();
		}

		if (price && Number(price) > 0) {
			input.price = Number(price);
		}

		if (tags?.trim()) {
			input.tags = tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0);
		}

		try {
			// Update the product using GraphQL mutation
			const updateProductStore = new UpdateProductStore();
			const result = await updateProductStore.mutate({ 
				id: params.id!,
				input 
			});

			if (result.errors) {
				console.error('GraphQL errors:', result.errors);
				return fail(500, { 
					errors: { 
						_form: 'Failed to update product: ' + result.errors.map(e => e.message).join(', ') 
					} 
				});
			}

			if (result.data?.updateProduct) {
				// Redirect back to the product detail page
				throw redirect(303, `/dashboard/products/${params.id}?updated=true`);
			}

			return fail(500, { 
				errors: { 
					_form: 'Failed to update product: Unknown error' 
				} 
			});

		} catch (error) {
			if (error instanceof Response) {
				throw error; // Re-throw redirect responses
			}
			
			console.error('Error updating product:', error);
			return fail(500, { 
				errors: { 
					_form: 'Failed to update product: ' + (error instanceof Error ? error.message : 'Unknown error')
				} 
			});
		}
	}
};