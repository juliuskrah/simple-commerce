import { browser } from '$app/environment';

let user = $state(null);
let isAuthenticated = $state(false);
let isLoading = $state(true);
let initialized = $state(false);

async function checkAuthenticationStatus() {
	if (!browser || initialized) return;
	
	isLoading = true;
	try {
		const response = await fetch('/api/auth/me');
		if (response.ok) {
			const userData = await response.json();
			user = userData;
			isAuthenticated = true;
		} else {
			user = null;
			isAuthenticated = false;
		}
	} catch (error) {
		console.log('Authentication check failed:', error);
		user = null;
		isAuthenticated = false;
	} finally {
		isLoading = false;
		initialized = true;
	}
}

export function useAuth() {
	// Initialize auth on first use
	if (!initialized) {
		checkAuthenticationStatus();
	}
	
	async function login(credentials) {
		try {
			const response = await fetch('/api/auth/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(credentials)
			});
			
			if (response.ok) {
				const userData = await response.json();
				user = userData;
				isAuthenticated = true;
				return { success: true, user: userData };
			} else {
				const error = await response.json();
				return { success: false, error: error.message || 'Login failed' };
			}
		} catch (error) {
			console.error('Login error:', error);
			return { success: false, error: 'Network error occurred' };
		}
	}
	
	async function logout() {
		try {
			await fetch('/api/auth/logout', {
				method: 'POST'
			});
		} catch (error) {
			console.error('Logout error:', error);
		} finally {
			user = null;
			isAuthenticated = false;
		}
	}
	
	async function register(userData) {
		try {
			const response = await fetch('/api/auth/register', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(userData)
			});
			
			if (response.ok) {
				const newUser = await response.json();
				user = newUser;
				isAuthenticated = true;
				return { success: true, user: newUser };
			} else {
				const error = await response.json();
				return { success: false, error: error.message || 'Registration failed' };
			}
		} catch (error) {
			console.error('Registration error:', error);
			return { success: false, error: 'Network error occurred' };
		}
	}
	
	async function updateProfile(profileData) {
		try {
			const response = await fetch('/api/customers/profile', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(profileData)
			});
			
			if (response.ok) {
				const updatedUser = await response.json();
				user = updatedUser;
				return { success: true, user: updatedUser };
			} else {
				const error = await response.json();
				return { success: false, error: error.message || 'Profile update failed' };
			}
		} catch (error) {
			console.error('Profile update error:', error);
			return { success: false, error: 'Network error occurred' };
		}
	}
	
	function refresh() {
		initialized = false;
		checkAuthenticationStatus();
	}
	
	return {
		login,
		logout,
		register,
		updateProfile,
		refresh,
		get user() { return user; },
		get isAuthenticated() { return isAuthenticated; },
		get isLoading() { return isLoading; },
		get initialized() { return initialized; }
	};
}