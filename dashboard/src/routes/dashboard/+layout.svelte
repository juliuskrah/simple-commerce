<script lang="ts">
	let { data, children } = $props();
	const user = $derived(data.user);
</script>

{@render children()}

<style lang="postcss">
	:global(body) {
		background-color: #f8fafc;
	}
</style>
