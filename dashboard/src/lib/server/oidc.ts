import { OAuth2Client, generateState } from 'arctic';
import { OIDC_CLIENT_ID, OIDC_ISSUER } from '$env/static/private';
import { building } from '$app/environment';

/**
 * Dynamically construct the redirect URI based on the request
 */
export function getRedirectUri(request: Request): string {
    if (building) {
        // During build time, use a placeholder URL
        return 'http://localhost:5173/auth/callback';
    }

    const url = new URL(request.url);
    return `${url.protocol}//${url.host}/auth/callback`;
}

// Cache the OIDC configuration
let oidcConfig: any = null;

/**
 * Fetch OIDC configuration from the issuer's well-known endpoint
 */
export async function getOidcConfiguration() {
	if (oidcConfig) return oidcConfig;
	
	try {
		const response = await fetch(`${OIDC_ISSUER}/.well-known/openid-configuration`);
		
		if (!response.ok) {
			throw new Error(`Failed to fetch OIDC configuration: ${response.statusText}`);
		}
		
		oidcConfig = await response.json();
		return oidcConfig;
	} catch (error) {
		console.error('Error fetching OIDC configuration:', error);
		throw error;
	}
}

/**
 * Create an authorization URL for the OIDC flow
 */
export async function createAuthorizationUrl(request: Request) {
	const config = await getOidcConfiguration();
	const state = generateState();
	const redirectUri = getRedirectUri(request);
	
	// Create OIDC client for this request
	const client = new OAuth2Client(
		OIDC_CLIENT_ID,
		null,
		redirectUri
	);
	
	// Create authorization URL with the required scopes
	const url = client.createAuthorizationURL(
		config.authorization_endpoint,
		state,
		['openid', 'profile', 'email']
	);
	
	return { url, state, redirectUri };
}

/**
 * Validate the authorization code and exchange it for tokens
 */
export async function validateAuthorizationCode(code: string, request: Request) {
	const config = await getOidcConfiguration();
	const redirectUri = getRedirectUri(request);
	
	// Create OIDC client for this request
	const client = new OAuth2Client(
		OIDC_CLIENT_ID,
		null,
		redirectUri
	);
	
	try {
		const tokens = await client.validateAuthorizationCode(
			config.token_endpoint,
			code,
			null
		);
		
		return tokens;
	} catch (error) {
		console.error('Error validating authorization code:', error);
		throw error;
	}
}

/**
 * Get user info from the OIDC provider
 */
export async function getUserInfo(accessToken: string) {
	const config = await getOidcConfiguration();
	
	try {
		const response = await fetch(config.userinfo_endpoint, {
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		});
		
		if (!response.ok) {
			throw new Error(`Failed to fetch user info: ${response.statusText}`);
		}
		
		return await response.json();
	} catch (error) {
		console.error('Error fetching user info:', error);
		throw error;
	}
}
