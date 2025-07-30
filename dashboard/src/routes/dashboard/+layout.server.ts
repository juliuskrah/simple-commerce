import { redirect } from '@sveltejs/kit';

export const load = (({ locals }) => {
    // If the user is not logged in, redirect to the login page
    if (!locals.user) {
        throw redirect(302, '/auth');
    }

    return {
        user: locals.user
    };
});
