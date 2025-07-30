import { OAuth2Client, generateState } from 'arctic';
import { OIDC_CLIENT_ID, OIDC_ISSUER, OIDC_REDIRECT_URI } from '$env/static/private';

// Create the OIDC client
export const oidcClient = new OAuth2Client(
	OIDC_CLIENT_ID,
	null,
	OIDC_REDIRECT_URI
);

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
export async function createAuthorizationUrl() {
	const config = await getOidcConfiguration();
	const state = generateState();
	
	// Create authorization URL with the required scopes
	const url = oidcClient.createAuthorizationURL(
		config.authorization_endpoint,
		state,
		['openid', 'profile', 'email']
	);
	
	return { url, state };
}

/**
 * Validate the authorization code and exchange it for tokens
 */
export async function validateAuthorizationCode(code: string) {
	const config = await getOidcConfiguration();
	
	try {
		const tokens = await oidcClient.validateAuthorizationCode(
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
