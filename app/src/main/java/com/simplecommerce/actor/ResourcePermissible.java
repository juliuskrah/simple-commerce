package com.simplecommerce.actor;

/// Marker interface for resources that can have permissions assigned to them.
/// @author julius.krah
public sealed interface ResourcePermissible permits User, Bot, Group, Role {

}
