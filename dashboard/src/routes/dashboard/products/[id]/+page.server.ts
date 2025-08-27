import type { PageServerLoad } from './$houdini';

export const load: PageServerLoad = async (event) => {
	return {
		...(await event.parent())
	};
};
