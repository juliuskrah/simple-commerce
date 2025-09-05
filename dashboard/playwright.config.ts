import { defineConfig } from '@playwright/test';

export default defineConfig({
		webServer: {
			command: 'bash -c "cp .env.test .env && npm run build && npm run preview"',
		port: 4173,
		reuseExistingServer: true,
		timeout: 120_000,
		env: {
			E2E_BYPASS_AUTH: '1',
			E2E_GRAPHQL_MOCK: '1'
		}
	},
	use: {
		baseURL: 'http://localhost:4173'
	},
	testDir: 'e2e',
	timeout: 60_000
});
