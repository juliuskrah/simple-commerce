# Simple Commerce

Simple Commerce is a ecommerce platform for the sale of digital products.

## License

This code is made available under a Apache-2.0 license. See the LICENSE file.

## Table of Contents

## Overview

## Instructions

You can start up Simple Commerce locally using `gradle` and `java`:

```bash
SPRING_PROFILES_ACTIVE=oidc-authn,keto-authz ./gradlew bootRun
```

## Docker Compose

Starting the docker containers is as simple as running:

```bash
docker compose --profile keto-authz --profile oidc-authn up -d
```

Stopping the docker containers is as simple as running:

```bash
docker compose --profile oidc-authn --profile keto-authz down --remove-orphans 
```