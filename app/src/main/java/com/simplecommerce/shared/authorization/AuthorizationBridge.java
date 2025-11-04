package com.simplecommerce.shared.authorization;

import java.util.List;

/**
 * Bridge abstraction for writing authorization relationship tuples to Keto.
 */
public interface AuthorizationBridge {
  void addActorsToGroup(String groupId, List<String> actorUsernames);
  void addGroupsToGroup(String parentGroupId, List<String> nestedGroupIds);
  void assignGroupPermissionOnProducts(String groupId, List<String> productIds, String relation);
  void assignActorPermissionOnProducts(String username, List<String> productIds, String relation);
  void assignRolePermissionOnProducts(String role, List<String> productIds, String relation);
  void assignRolesToGroup(String groupId, List<String> roles);
  void assignRolesToActor(String actorUsername, List<String> roles);
  void removeActorsFromGroup(String groupId, List<String> actorUsernames);
  void removeGroupsFromGroup(String parentGroupId, List<String> nestedGroupIds);
  void revokeGroupPermissionOnProducts(String groupId, List<String> productIds, String relation);
  void purgeGroupRelations(String groupId);
  void purgeProductRelations(String productId);
}
