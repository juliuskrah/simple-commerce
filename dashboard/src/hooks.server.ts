import { getSession } from '$lib/server/session';
import { setSession } from '$houdini';
import type { Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	// Get the session token from the cookies
	const sessionToken = event.cookies.get('session_token');

	if (sessionToken) {
		// Validate the session
		const session = await getSession(sessionToken);

		if (session) {
			// Add the session to the event locals
			event.locals.session = session;
			event.locals.user = session.userInfo;
			setSession(event, { session });
		}
	}

	return resolve(event);
};
