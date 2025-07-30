<script lang="ts">
	let { children } = $props();

	import '../app.css';
</script>

{@render children()}

<style lang="postcss">
    @reference "tailwindcss";
    :global(html, body) {
        height: 100%;
        margin: 0;
        font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
            Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
    }
    
    :global(body) {
        background-color: #F8FAFC;
        color: #1E293B;
    }
</style>
