package com.simplecommerce.shared.authorization;

import java.util.List;

/**
 * Bridge abstraction for writing authorization relationship tuples to Keto.
 */
public interface AuthorizationBridge {
  void addActorsToGroup(String groupId, List<String> actorUsernames);
  void addGroupsToGroup(String parentGroupId, List<String> nestedGroupIds);
  void assignGroupPermissionOnProducts(String groupId, List<String> productIds, String relation);
  void purgeGroupRelations(String groupId);
}
