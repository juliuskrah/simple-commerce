package com.simplecommerce.group;

import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.jspecify.annotations.Nullable;

/**
 * Service abstraction for group management (phase 0-1: persistence + basic membership & product permission assignment bookkeeping).
 */
public interface GroupService {
  Optional<Group> findGroup(String id);
  List<Group> findGroups(int limit);
  Window<Group> findGroups(int limit, Sort sort, ScrollPosition scroll);
  Group addGroup(String name, @Nullable String description);
  Group addMembers(String groupId, @Nullable List<String> actorUsernames, @Nullable List<String> nestedGroupIds);
  Group removeMembers(String groupId, @Nullable List<String> actorUsernames, @Nullable List<String> nestedGroupIds);
  Group assignGroupProductPermissions(String groupId, List<String> productIds, BasePermissions permission);
  Group revokeGroupProductPermissions(String groupId, List<String> productIds, BasePermissions permission);
}
