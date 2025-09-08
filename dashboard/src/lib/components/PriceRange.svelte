<script lang="ts">
	import { formatPriceRange } from '../validation/pricing';

	interface Props {
		priceRange: {
			start?: { amount: number; currency: string } | null;
			stop?: { amount: number; currency: string } | null;
		} | null;
		size?: 'sm' | 'md' | 'lg';
		variant?: 'default' | 'subtle' | 'bold';
	}

	let { priceRange, size = 'md', variant = 'default' }: Props = $props();

	const formattedPrice = $derived(formatPriceRange(priceRange));
	const isEmpty = $derived(formattedPrice === '-');
	const isRange = $derived(formattedPrice.includes(' - '));

	const sizeClasses = {
		sm: 'text-sm',
		md: 'text-base',
		lg: 'text-lg'
	};

	const variantClasses = {
		default: 'text-gray-900',
		subtle: 'text-gray-600', 
		bold: 'font-semibold text-gray-900'
	};
</script>

<span 
	class="price-range {sizeClasses[size]} {variantClasses[variant]}"
	class:text-gray-400={isEmpty}
	class:font-medium={isRange && variant === 'default'}
	title={isRange ? 'Price range from lowest to highest variant' : undefined}
>
	{formattedPrice}
</span>

<style>
	.price-range {
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}
</style>