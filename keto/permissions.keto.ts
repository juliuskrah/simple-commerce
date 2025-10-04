import {Context, Namespace, SubjectSet} from "@ory/keto-namespace-types"

// Customers, Staff and Bots
class Actor implements Namespace {
}

// Group for organizational structure
class Group implements Namespace {
  related: {
    members: (Actor | Group)[]
  }
}

class Product implements Namespace {
  related: {
    parents: Category[]
    editors: (Actor | SubjectSet<Group, "members">)[]
    viewers: (Actor | SubjectSet<Group, "members">)[]
    owners: (Actor | SubjectSet<Group, "members">)[]
  }
  permits = {
    edit: (ctx: Context): boolean => this.permits.delete(ctx) ||
        this.related.editors.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.edit(ctx)),
    view: (ctx: Context): boolean => this.permits.edit(ctx) ||
        this.related.viewers.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.view(ctx)),
    delete: (ctx: Context): boolean => this.related.owners.includes(ctx.subject)
  }
}

class ProductVariant implements Namespace {
  related: {
    parents: Product[]
    editors: (Actor | SubjectSet<Group, "members">)[]
    viewers: (Actor | SubjectSet<Group, "members">)[]
  }
  permits = {
    edit: (ctx: Context): boolean => this.related.editors.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.edit(ctx)),
    view: (ctx: Context): boolean => this.permits.edit(ctx) ||
        this.related.viewers.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.view(ctx)),
  }
}

class Order implements Namespace {
  related: {
    owners: (Actor | SubjectSet<Group, "members">)[]
    contains: ProductVariant[]
  }
}

// Categories are hierarchical
class Category implements Namespace {
  related: {
    parents: Category[]
    editors: (Actor | SubjectSet<Group, "members">)[]
    viewers: (Actor | SubjectSet<Group, "members">)[]
  }
  permits = {
    edit: (ctx: Context): boolean => this.related.editors.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.edit(ctx)),
    view: (ctx: Context): boolean => this.permits.edit(ctx) ||
        this.related.viewers.includes(ctx.subject) ||
        this.related.parents.traverse(p => p.permits.view(ctx)),
  }
}
