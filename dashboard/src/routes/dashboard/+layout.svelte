<script lang="ts">
	import type { LayoutProps } from './$types';

	let { data, children }: LayoutProps = $props();
	const user = $derived(data.user);
</script>

{@render children()}

<style lang="postcss">
	:global(body) {
		background-color: #f8fafc;
	}
</style>
