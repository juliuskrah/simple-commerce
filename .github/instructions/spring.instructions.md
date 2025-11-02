---
applyTo: '**/*.java'
description: Provide project context and coding guidelines that AI should follow when generating Spring code.
---
# Spring Boot Instructions

- **Configuration Classes**: All configuration classes should have the attribute `proxyBeanMethods` set to `false`.

## Spring for GraphQL Instructions

- Use Spring for GraphQL
- `*Connection` types do not need to be explicitly defined in the schema. This is automatically added by Spring for GraphQL when a `@QueryMapping` return value is `Window`.

## Spring Modulith Instructions

- Each module should be self-contained and encapsulate its domain logic.
- Use domain events to communicate between modules.
- Keep module dependencies to a minimum and use interfaces to decouple modules.
  - e.g. `product` module should not depend on `file` module.
- After each iteration run `app/src/test/java/../SimpleCommerceApplicationTest.java` to ensure the module is working correctly.


## Testing Guidelines

- GraphQL tests should not embed documents in the test code. 
  - Instead, use separate files for GraphQL queries and mutations located in the `src/test/resources/graphql-test` directory.
- Integration tests should have the suffix `IT` and be located in the `src/test/java` directory.
- Integration tests must be annotated with `@SpringBootTest` and should start the entire Spring context.

## Starting the GraphQL Server

- Start the server by running `./gradlew bootRun --args='serve --spring.profiles.active=oidc-authn,keto-authz'`.
- In case of failure resulting from Hibernate validation, run the schema migration using `./gradlew bootRun --args='migrate'`.
