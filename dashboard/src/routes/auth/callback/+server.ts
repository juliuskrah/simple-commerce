// routes/auth/callback/+server.ts
import { validateAuthorizationCode, getUserInfo } from '$lib/server/oidc';
import { generateSessionToken, createSession, setSessionTokenCookie } from '$lib/server/session';
import { redirect } from '@sveltejs/kit';
import type { RequestEvent } from '@sveltejs/kit';

export async function GET(event: RequestEvent): Promise<Response> {
	// Get the authorization code and state from the URL
	const code = event.url.searchParams.get('code');
	const state = event.url.searchParams.get('state');

	// Get the stored state from the cookie
	const storedState = event.cookies.get('oidc_state') ?? null;

	// Validate the code and state
	if (code === null || state === null || storedState === null) {
		return new Response('Invalid request', { status: 400 });
	}

	if (state !== storedState) {
		return new Response('State mismatch', { status: 400 });
	}

	// Exchange the authorization code for tokens
	const tokens = await validateAuthorizationCode(code, event.request);
	const accessToken = tokens.accessToken();

	// Get the user info using the access token
	const userInfo = await getUserInfo(accessToken);

	// Create a new session
	const sessionToken = generateSessionToken();
	const session = await createSession(sessionToken, userInfo);

	// Set the session cookie
	setSessionTokenCookie(event, sessionToken, session.expiresAt);

	// Clear the state and redirect_uri cookies
	event.cookies.delete('oidc_state', { path: '/' });
	event.cookies.delete('oidc_redirect_uri', { path: '/' });

	// Redirect to the dashboard
	return redirect(302, '/');
}
