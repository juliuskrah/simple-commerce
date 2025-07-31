import { dev } from '$app/environment';
import { randomBytes } from 'crypto';

/**
 * Generate a secure random session token
 */
export function generateSessionToken(): string {
	return randomBytes(32).toString('hex');
}

/**
 * In-memory session store for development purposes
 * In production, this should be replaced with a database store
 */
const sessionStore = new Map<string, SessionData>();

/**
 * Session data structure
 */
export interface SessionData {
	id: string;
	userId: string;
	expiresAt: Date;
	userInfo: any;
	accessToken: string; // Store JWT access token
}

/**
 * Create a new session
 */
export async function createSession(
	sessionToken: string,
	userInfo: any,
	accessToken: string
): Promise<SessionData> {
	// Create expiration date (24 hours from now)
	const expiresAt = new Date();
	expiresAt.setHours(expiresAt.getHours() + 24);

	const session: SessionData = {
		id: sessionToken,
		userId: userInfo.sub,
		expiresAt,
		userInfo,
		accessToken // Store the JWT access token
	};

	// Store the session
	sessionStore.set(sessionToken, session);

	return session;
}

/**
 * Get a session by token
 */
export async function getSession(sessionToken: string): Promise<SessionData | null> {
	const session = sessionStore.get(sessionToken);

	if (!session) {
		return null;
	}

	// Check if session has expired
	if (new Date() > session.expiresAt) {
		await invalidateSession(sessionToken);
		return null;
	}

	return session;
}

/**
 * Invalidate a session
 */
export async function invalidateSession(sessionToken: string): Promise<void> {
	sessionStore.delete(sessionToken);
}

/**
 * Set the session cookie
 */
export function setSessionTokenCookie(event: any, sessionToken: string, expiresAt: Date): void {
	event.cookies.set('session_token', sessionToken, {
		path: '/',
		httpOnly: true,
		secure: !dev,
		sameSite: 'lax',
		expires: expiresAt
	});
}

/**
 * Delete the session cookie
 */
export function deleteSessionTokenCookie(event: any): void {
	event.cookies.delete('session_token', {
		path: '/',
		httpOnly: true,
		secure: !dev,
		sameSite: 'lax'
	});
}
