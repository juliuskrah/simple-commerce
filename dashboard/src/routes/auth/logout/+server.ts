// routes/auth/logout/+server.ts
import { invalidateSession, deleteSessionTokenCookie } from '$lib/server/session';
import { redirect } from '@sveltejs/kit';
import type { RequestEvent } from '@sveltejs/kit';

export async function GET(event: RequestEvent): Promise<Response> {
	// Get the session token from the cookie
	const sessionToken = event.cookies.get('session_token');

	if (sessionToken) {
		// Invalidate the session
		await invalidateSession(sessionToken);

		// Delete the session cookie
		deleteSessionTokenCookie(event);
	}

	// Redirect to the login page
	return redirect(302, '/auth');
}
