/// <references types="houdini-svelte">

/** @type {import('houdini').ConfigFile} */
const config = {
	watchSchema: {
		url: 'env:GRAPHQL_ENDPOINT',
		headers: {
			Authorization(env) {
				return `Bearer ${env.GRAPHQL_ACCESS_TOKEN}`;
			}
		}
	},
	runtimeDir: '.houdini',
	plugins: {
		'houdini-svelte': {
			forceRunesMode: true
		}
	},
	scalars: {
		// the name of the scalar we are configuring
		DateTime: {
			// the corresponding typescript type
			type: 'Date',
			// turn the api's response into that type
			unmarshal(val) {
				return val ? new Date(val) : null;
			},
			// turn the value into something the API can use
			marshal(date) {
				return date && date.getTime();
			}
		},
		JsonString: {
			type: 'string',
			unmarshal(val) {
				return val;
			},
			marshal(val) {
				return val;
			}
		},
		Decimal: {
			type: 'number',
			unmarshal(val) {
				return val ? parseFloat(val) : null;
			},
			marshal(val) {
				return val ? val.toString() : null;
			}
		},
		Currency: {
			type: 'string',
			unmarshal(val) {
				return typeof val === 'string' ? val : null;
			},
			marshal(val) {
				return val;
			}
		},
		URL: {
			type: 'string',
			unmarshal(val) {
				return typeof val === 'string' ? val : null;
			},
			marshal(val) {
				return val;
			}
		}
	}
};

export default config;
