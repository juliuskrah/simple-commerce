// Keto namespace definitions for Actor system
// This defines the authorization model for Customer, Staff, and Bot actors

import { Namespace, Context } from '@ory/keto-namespace-types'

class User implements Namespace {
  related: {
    manager: User[]
  } = {}
}

class Permission implements Namespace {}

class Role implements Namespace {}

class Organization implements Namespace {
  related: {
    admins: User[]
    members: User[]
  } = {}
}

class Customer implements Namespace {
  related: {
    owner: User[]
    viewers: User[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.viewers.includes(ctx.subject) || 
                                    this.related.owner.includes(ctx.subject),
    write: (ctx: Context): boolean => this.related.owner.includes(ctx.subject),
    delete: (ctx: Context): boolean => this.related.owner.includes(ctx.subject)
  }
}

class Staff implements Namespace {
  related: {
    owner: User[]
    viewers: User[]
    organization: Organization[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.viewers.includes(ctx.subject) || 
                                    this.related.owner.includes(ctx.subject) ||
                                    this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    write: (ctx: Context): boolean => this.related.owner.includes(ctx.subject) ||
                                     this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    delete: (ctx: Context): boolean => this.related.owner.includes(ctx.subject) ||
                                      this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    manage: (ctx: Context): boolean => this.related.organization.some(org => org.related.admins.includes(ctx.subject))
  }
}

class Bot implements Namespace {
  related: {
    owner: User[]
    viewers: User[]
    organization: Organization[]
    app: App[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.viewers.includes(ctx.subject) || 
                                    this.related.owner.includes(ctx.subject) ||
                                    this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    write: (ctx: Context): boolean => this.related.owner.includes(ctx.subject) ||
                                     this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    delete: (ctx: Context): boolean => this.related.owner.includes(ctx.subject) ||
                                      this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    execute: (ctx: Context): boolean => this.related.app.some(app => app.related.executors.includes(ctx.subject)) ||
                                       this.related.owner.includes(ctx.subject)
  }
}

class App implements Namespace {
  related: {
    owner: User[]
    executors: User[]
    organization: Organization[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.executors.includes(ctx.subject) || 
                                    this.related.owner.includes(ctx.subject) ||
                                    this.related.organization.some(org => org.related.members.includes(ctx.subject)),
    write: (ctx: Context): boolean => this.related.owner.includes(ctx.subject) ||
                                     this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    execute: (ctx: Context): boolean => this.related.executors.includes(ctx.subject) ||
                                       this.related.owner.includes(ctx.subject)
  }
}

// Product and Category permissions for Actor interactions
class Product implements Namespace {
  related: {
    viewers: (User | Customer)[]
    editors: (User | Staff)[]
    organization: Organization[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.viewers.includes(ctx.subject) ||
                                    this.related.editors.includes(ctx.subject) ||
                                    this.related.organization.some(org => org.related.members.includes(ctx.subject)),
    write: (ctx: Context): boolean => this.related.editors.includes(ctx.subject) ||
                                     this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    delete: (ctx: Context): boolean => this.related.organization.some(org => org.related.admins.includes(ctx.subject))
  }
}

class Category implements Namespace {
  related: {
    viewers: (User | Customer)[]
    editors: (User | Staff)[]
    organization: Organization[]
  } = {}

  permits = {
    read: (ctx: Context): boolean => this.related.viewers.includes(ctx.subject) ||
                                    this.related.editors.includes(ctx.subject) ||
                                    this.related.organization.some(org => org.related.members.includes(ctx.subject)),
    write: (ctx: Context): boolean => this.related.editors.includes(ctx.subject) ||
                                     this.related.organization.some(org => org.related.admins.includes(ctx.subject)),
    delete: (ctx: Context): boolean => this.related.organization.some(org => org.related.admins.includes(ctx.subject))
  }
}