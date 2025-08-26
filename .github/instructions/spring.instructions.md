---
applyTo: '**/*.java'
description: Provide project context and coding guidelines that AI should follow when generating Spring code.
---
# Spring Boot Instructions

1. **Configuration Classes**: All configuration classes should have the attribute `proxyBeanMethods` set to `false`.

## Spring Modulith Instructions

- Each module should be self-contained and encapsulate its domain logic.
- Use domain events to communicate between modules.
- Keep module dependencies to a minimum and use interfaces to decouple modules.
  - `product` module should not depend on `file` module.
