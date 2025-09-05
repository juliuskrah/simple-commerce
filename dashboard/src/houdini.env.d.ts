import type { SessionData } from '$lib/server/session';

declare global {
	namespace Houdini {
		interface Session extends SessionData {}
	}
}

export {};
