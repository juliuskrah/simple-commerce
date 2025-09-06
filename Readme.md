# Simple Commerce

Simple Commerce is a ecommerce platform for the sale of digital products.

## License

This code is made available under `Apache-2.0` license. See the [LICENSE](./LICENSE) file.

## Table of Contents

## Overview

Simple Commerce is a headless commerce platform that performs a minimal set of commerce primitives.

## Running Locally

Simple Commerce now supports CLI commands for different operations:

### CLI Commands

- **serve**: Start the GraphQL web server (default if no command specified)
- **migrate**: Run database migrations only
- **migrate --seed**: Run database migrations and seed the database

#### Using Gradle

```bash
# Start the server (default behavior)
SPRING_PROFILES_ACTIVE=oidc-authn,keto-authz ./gradlew bootRun

# Start the server explicitly
SPRING_PROFILES_ACTIVE=oidc-authn,keto-authz ./gradlew bootRun --args="serve"
# Or
./gradlew bootRun --args="serve --spring.profiles.active=oidc-authn,keto-authz"

# Run migrations only
./gradlew bootRun --args="migrate"

# Clean database and run migrations
./gradlew bootRun --args="migrate --clean"

# Run migrations and seed the database
./gradlew bootRun --args="migrate --seed"
```

#### Using JAR directly

```bash
# Build the application
./gradlew build

# Start the server
java -jar app/build/libs/app-*.jar serve

# Run migrations only
java -jar app/build/libs/app-*.jar migrate

# Run migrations and seed the database
java -jar app/build/libs/app-*.jar migrate --seed

# Clean database and run migrations
java -jar app/build/libs/app-*.jar migrate --clean
```

### Starting the Frontend

```bash
cd dashboard && npm run dev
```

### Docker Compose

Build docker image for use in docker compose:

```bash
docker build -t juliuskrah/simple-commerce .
```

#### Starting the full stack

```bash
docker compose --profile keto-authz --profile oidc-authn --profile app up -d
```

This will:
1. Start dependencies (PostgreSQL, MinIO, OIDC, Keto)
2. Run database migrations with seeding (`simple-commerce-migrate`)
3. Start the web server (`simple-commerce`)

#### Stopping the docker containers

```bash
docker compose --profile keto-authz --profile oidc-authn --profile app down --remove-orphans 
```

## Contributing

We welcome contributions to Simple Commerce!
If you have an idea for a new feature, bug fix, or improvement, please open an issue or submit a pull request.
Please make sure to follow the [contribution guidelines](./.github/CONTRIBUTING.md) when submitting your changes.

Before commiting your changes, please make sure to run the tests and ensure that everything is working as expected.

```bash
./gradlew test
```
