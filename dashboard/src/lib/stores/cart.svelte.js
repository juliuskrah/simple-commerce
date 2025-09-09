import { browser } from '$app/environment';

let cartItems = $state([]);
let initialized = $state(false);

function initializeCart() {
	if (!browser || initialized) return;
	
	try {
		const savedCart = localStorage.getItem('shopping-cart');
		if (savedCart) {
			cartItems = JSON.parse(savedCart);
		}
	} catch (error) {
		console.error('Error loading cart:', error);
		cartItems = [];
	}
	initialized = true;
}

function saveCart() {
	if (!browser) return;
	
	try {
		localStorage.setItem('shopping-cart', JSON.stringify(cartItems));
	} catch (error) {
		console.error('Error saving cart:', error);
	}
}

export function useCart() {
	// Initialize cart on first use
	if (!initialized) {
		initializeCart();
	}
	
	function addToCart(product, variant, quantity = 1) {
		const cartItem = {
			id: `${product.id}-${variant.id}`,
			productId: product.id,
			variantId: variant.id,
			name: product.name,
			variantName: variant.name !== product.name ? variant.name : null,
			price: variant.priceSet?.amount || 0,
			currency: variant.priceSet?.currency || 'USD',
			quantity: quantity,
			maxStock: variant.stock,
			sku: variant.sku,
			weight: variant.weight,
			image: product.media?.edges?.[0]?.node?.url || null
		};
		
		// Check if item already exists in cart
		const existingItemIndex = cartItems.findIndex(item => item.id === cartItem.id);
		
		if (existingItemIndex >= 0) {
			// Update quantity of existing item
			const existingItem = cartItems[existingItemIndex];
			const newQuantity = Math.min(existingItem.quantity + quantity, cartItem.maxStock);
			cartItems[existingItemIndex] = { ...existingItem, quantity: newQuantity };
		} else {
			// Add new item to cart
			cartItems = [...cartItems, cartItem];
		}
		
		saveCart();
		return true;
	}
	
	function removeFromCart(itemId) {
		cartItems = cartItems.filter(item => item.id !== itemId);
		saveCart();
	}
	
	function updateQuantity(itemId, newQuantity) {
		if (newQuantity <= 0) {
			removeFromCart(itemId);
			return;
		}
		
		const itemIndex = cartItems.findIndex(item => item.id === itemId);
		if (itemIndex >= 0) {
			const item = cartItems[itemIndex];
			if (newQuantity <= item.maxStock) {
				cartItems[itemIndex] = { ...item, quantity: newQuantity };
				cartItems = [...cartItems]; // Trigger reactivity
				saveCart();
			}
		}
	}
	
	function clearCart() {
		cartItems = [];
		saveCart();
	}
	
	function getCartItems() {
		return cartItems;
	}
	
	function getCartCount() {
		return cartItems.reduce((sum, item) => sum + item.quantity, 0);
	}
	
	function getCartTotal() {
		return cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
	}
	
	return {
		addToCart,
		removeFromCart,
		updateQuantity,
		clearCart,
		getCartItems,
		getCartCount,
		getCartTotal,
		get items() { return cartItems; },
		get count() { return getCartCount(); },
		get total() { return getCartTotal(); }
	};
}