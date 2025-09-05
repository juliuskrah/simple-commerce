// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces

/// <reference path="./houdini.env.d.ts" />

declare global {
	namespace App {
		// interface Error {}
		interface Locals {
			session: import('$lib/server/session').SessionData | null;
			user: any | null;
		}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

export {};
