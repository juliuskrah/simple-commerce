// routes/auth/login/+server.ts
import { createAuthorizationUrl } from '$lib/server/oidc';
import { redirect } from '@sveltejs/kit';
import type { RequestEvent } from '@sveltejs/kit';

export async function GET(event: RequestEvent): Promise<Response> {
	const { url, state, redirectUri } = await createAuthorizationUrl(event.request);

	// Store the state in a cookie for verification later
	event.cookies.set('oidc_state', state, {
		path: '/',
		httpOnly: true,
		maxAge: 60 * 10, // 10 minutes
		sameSite: 'lax'
	});

	// Store the redirectUri in a cookie to use during callback
	event.cookies.set('oidc_redirect_uri', redirectUri, {
		path: '/',
		httpOnly: true,
		maxAge: 60 * 10, // 10 minutes
		sameSite: 'lax'
	});

	// Redirect to the OIDC authorization endpoint
	return redirect(302, url);
}
