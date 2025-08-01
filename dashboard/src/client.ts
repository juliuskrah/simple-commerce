import { HoudiniClient } from '$houdini';
import { PUBLIC_GRAPHQL_ENDPOINT } from '$env/static/public';

export default new HoudiniClient({
	url: PUBLIC_GRAPHQL_ENDPOINT,

	// Configure the network call with appropriate headers for CORS
	fetchParams({ session }) {
		return {
			headers: {
				Authorization: `Bearer ${session?.session.accessToken}`
			},
			credentials: 'include'
		};
	}
});
