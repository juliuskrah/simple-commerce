import type { LayoutServerLoad } from './$types';

export const load = (({ locals }) => {
	return {
		user: locals.user,
		session: locals.session
	};
}) satisfies LayoutServerLoad;
