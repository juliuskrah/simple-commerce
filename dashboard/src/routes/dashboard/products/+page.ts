import type { PageLoad } from './$types';

export const load: PageLoad = ({ params }) => {
	return {
		items: [
			{
				id: 1,
				name: 'Classy men wristwatch',
				price: 'GHS 500',
				inventory: 25,
				category: 'Accessories',
				status: 'Active'
			},
			{
				id: 2,
				name: "Women's handbag",
				price: 'GHS 300',
				inventory: 15,
				category: 'Accessories',
				status: 'Active'
			},
			{
				id: 3,
				name: 'Smartphone case',
				price: 'GHS 100',
				inventory: 50,
				category: 'Electronics',
				status: 'Active'
			},
			{
				id: 4,
				name: 'Wireless headphones',
				price: 'GHS 250',
				inventory: 8,
				category: 'Electronics',
				status: 'Low stock'
			},
			{
				id: 5,
				name: 'Summer dress',
				price: 'GHS 180',
				inventory: 0,
				category: 'Clothing',
				status: 'Out of stock'
			}
		]
	};
};
