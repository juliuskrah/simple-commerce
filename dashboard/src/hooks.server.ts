import { getSession } from '$lib/server/session';
import { setSession } from '$houdini';
import type { Handle, HandleFetch } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
	// Get the session token from the cookies
	const sessionToken = event.cookies.get('session_token');

	if (sessionToken) {
		// Validate the session
		const session = await getSession(sessionToken);

		if (session) {
			// Add the session to the event locals
			setSession(event, { session });
			event.locals.session = session;
			event.locals.user = session.userInfo;
		}
	}

	return resolve(event);
};

export const handleFetch: HandleFetch = async ({ event, request, fetch }) => {
	// Only modify GraphQL requests
	if (request.url.includes('/graphql')) {
		// Clone the request and strip out problematic headers
		const headers = new Headers(request.headers);

		const newRequest = new Request(request, {
			headers
		});
		return fetch(newRequest);
	}

	return fetch(request);
};
