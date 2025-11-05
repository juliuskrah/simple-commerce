package com.simplecommerce.actor;

/// Marker interface for group members: users and groups.
/// @author julius.krah
public sealed interface GroupMember permits User, Group {

}
