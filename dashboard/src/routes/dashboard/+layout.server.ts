import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = ({ locals }) => {
	// If the user is not logged in, redirect to the login page
	if (!locals.user) {
		throw redirect(302, '/auth');
	}

	return {
		user: locals.user
	};
};
