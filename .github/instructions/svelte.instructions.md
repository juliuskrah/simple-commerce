---
applyTo: '**/*.ts,**/*.svelte,**/*.js,**/*.css,**/*.mdx'
description: This file contains instructions for AI to follow when generating or reviewing TypeScript code in a Svelte project. It includes guidelines on project structure, coding standards, component design, state management, styling, testing, and documentation. The AI should ensure that the generated code adheres to these guidelines and is consistent with the project's architecture.
---
# SvelteKit Instructions

1. **Project Structure**: A typical SvelteKit project looks like this:

```
my-project/
├ src/
│ ├ lib/
│ │ ├ server/
│ │ │ └ [your server-only lib files]
│ │ └ [your lib files]
│ ├ params/
│ │ └ [your param matchers]
│ ├ routes/
│ │ └ [your routes]
│ ├ app.html
│ ├ error.html
│ ├ hooks.client.js
│ ├ hooks.server.js
│ └ service-worker.js
├ static/
│ └ [your static assets]
├ tests/
│ └ [your tests]
├ package.json
├ svelte.config.js
├ tsconfig.json
└ vite.config.js
```
2. **Coding Standards**: Follow established coding standards and best practices for TypeScript and Svelte.
3. **Component Design**: Adhere to the principles of component-based design, promoting reusability and maintainability.
4. **State Management**: Utilize appropriate state management techniques for Svelte applications.
5. **Styling**: Follow the project's styling guidelines, including the use of CSS frameworks or methodologies.
6. **Testing**: Write unit tests for new features and ensure existing tests pass.
