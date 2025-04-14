# Simple Commerce

Simple Commerce is a ecommerce platform for the sale of digital products.

## License

This code is made available under `Apache-2.0` license. See the [LICENSE](./LICENSE) file.

## Table of Contents

## Overview

Simple Commerce is a headless commerce platform that performs a minimal set of commerce primitives.

## Running Locally

You can start up Simple Commerce locally using `gradle` and `java`:

```bash
SPRING_PROFILES_ACTIVE=oidc-authn,keto-authz ./gradlew bootRun
```

### Docker Compose

Starting the docker containers is as simple as running:

```bash
docker compose --profile keto-authz --profile oidc-authn up -d
```

Stopping the docker containers is as simple as running:

```bash
docker compose --profile keto-authz --profile oidc-authn down --remove-orphans 
```

## Contributing

We welcome contributions to Simple Commerce!
If you have an idea for a new feature, bug fix, or improvement, please open an issue or submit a pull request.
Please make sure to follow the [contribution guidelines](./.github/CONTRIBUTING.md) when submitting your changes.

Before commiting your changes, please make sure to run the tests and ensure that everything is working as expected.

```bash
./gradlew test
```
