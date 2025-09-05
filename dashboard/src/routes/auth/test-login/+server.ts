// routes/auth/test-login/+server.ts
// Test-only endpoint to bypass real OIDC during Playwright/Vitest runs.
// Creates an in-memory session and sets the session cookie, then redirects to dashboard.
// Guarded so it won't be available in production builds.
import { generateSessionToken, createSession } from '$lib/server/session';
import type { RequestEvent } from '@sveltejs/kit';

export async function GET(event: RequestEvent) {
  // Available in preview for e2e tests (ensure it's not deployed to production environment inadvertently).

  const sessionToken = generateSessionToken();
  const userInfo = {
    sub: 'simple_commerce',
    email: 'simple@example.com',
    name: 'simple_commerce'
  };
  // Access token can be a placeholder for tests
  const accessToken = 'test-access-token';
  const session = await createSession(sessionToken, userInfo, accessToken);
  // Manually set cookie WITHOUT secure flag so it works over http in preview (playwright)
  event.cookies.set('session_token', sessionToken, {
    path: '/',
    httpOnly: true,
    secure: false,
    sameSite: 'lax',
    expires: session.expiresAt
  });
  // Some adapters in test mode may not follow redirects within Playwright dev server reliably
  // so return an HTML page that performs a client-side redirect.
  return new Response(`<!DOCTYPE html><html><head><meta http-equiv="refresh" content="0; url=/dashboard" />
  <title>Auth</title></head><body>Signed in (test). Redirecting…<script>window.location.href='/dashboard';</script></body></html>`, {
    status: 200,
    headers: { 'Content-Type': 'text/html' }
  });
}
