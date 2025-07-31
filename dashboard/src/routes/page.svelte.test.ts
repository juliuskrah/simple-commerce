import { page } from '@vitest/browser/context';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { render } from 'vitest-browser-svelte';
import Page from './+page.svelte';

// Mock data for testing
const mockUser = {
	name: 'Test User',
	email: 'test@example.com',
	sub: 'user-123'
};

describe('/+page.svelte', () => {
	beforeEach(() => {
		// Set up mock for $props function
		// @ts-expect-error - Mocking the Svelte $props function
		window.$props = vi.fn().mockReturnValue({
			data: { user: mockUser }
		});
	});

	afterEach(() => {
		// Clean up mocks
		vi.restoreAllMocks();
		// @ts-expect-error - Clean up mock
		delete window.$props;
	});

	it('should render h1', async () => {
		// Render the actual Page component
		render(Page);

		// Check if the heading is in the document
		const heading = page.getByRole('heading', { level: 1 });
		await expect.element(heading).toBeInTheDocument();

		// Check the text content
		const welcomeText = page.getByText('Welcome to Simple Commerce Dashboard');
		await expect.element(welcomeText).toBeInTheDocument();
	});
});
