import { HoudiniClient } from '$houdini';
import { PUBLIC_GRAPHQL_ENDPOINT } from '$env/static/public';

// Guard against direct process.env access in the browser (process is undefined client-side)
const isMock = typeof process !== 'undefined' && process.env?.E2E_GRAPHQL_MOCK === '1';
const resolvedUrl = isMock ? '/graphql' : PUBLIC_GRAPHQL_ENDPOINT;

export default new HoudiniClient({
	url: resolvedUrl,

	// Configure the network call with appropriate headers for CORS
	fetchParams({ session }: any) {
		const token = session?.accessToken || session?.user?.accessToken || '';
		return {
			headers: {
				Authorization: token ? `Bearer ${token}` : ''
			},
			credentials: 'include'
		};
	}
});
