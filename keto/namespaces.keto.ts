import { Namespace, Context } from "@ory/keto-namespace-types"

// Base Actor namespace
class Actor implements Namespace {
  related: {
    roles: Actor[]
  }
}

// Staff namespace for admin users
class Staff implements Namespace {
  related: {
    manager: Staff[]
    department: Group[]
    subordinates: Staff[]
  }
}

// Customer namespace for storefront users
class Customer implements Namespace {
  related: {
    group: CustomerGroup[]  // B2B, B2C, VIP, WHOLESALE
    orders: Order[]
  }
}

// Bot namespace for automation and external apps
class Bot implements Namespace {
  related: {
    app: Application[]
    scopes: Permission[]
  }
}

// Customer groups for tiered access
class CustomerGroup implements Namespace {
  related: {
    members: Customer[]
  }
}

// Group for organizational structure
class Group implements Namespace {
  related: {
    members: (Staff | Customer)[]
    parent: Group[]
  }
}

// Product permissions
class Product implements Namespace {
  related: {
    editors: Staff[]
    viewers: (Staff | Customer)[]
    publishers: Staff[]
  }
}

// Order permissions
class Order implements Namespace {
  related: {
    owner: Customer[]
    processors: Staff[]
    viewers: (Staff | Customer)[]
  }
}

// Cart permissions (customer only)
class Cart implements Namespace {
  related: {
    owner: Customer[]
  }
}

// Media/File permissions
class Media implements Namespace {
  related: {
    uploaders: (Staff | Bot)[]
    viewers: (Staff | Customer)[]
  }
}

// Application permissions for bots
class Application implements Namespace {
  related: {
    bots: Bot[]
    permissions: Permission[]
  }
}

// Permission scopes
class Permission implements Namespace {
  related: {
    holders: (Staff | Bot)[]
  }
}

// Categories permissions
class Category implements Namespace {
  related: {
    editors: Staff[]
    viewers: (Staff | Customer)[]
  }
}