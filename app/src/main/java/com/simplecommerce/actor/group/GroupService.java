package com.simplecommerce.actor.group;

import com.simplecommerce.actor.Group;
import com.simplecommerce.actor.GroupMember;
import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

/**
 * Service abstraction for group management (phase 0-1: persistence + basic membership & product permission assignment bookkeeping).
 * @author julius.krah
 */
public interface GroupService {
  Optional<Group> findGroup(String id);

  List<Group> findGroups(int limit);

  Window<Group> findGroups(int limit, Sort sort, ScrollPosition scroll);

  Group addGroup(String name, @Nullable String description);

  List<? extends GroupMember> addMembers(String groupId, List<String> actorUsernames, List<String> nestedGroupIds);

  List<? extends GroupMember> removeMembers(String groupId, List<String> actorUsernames, List<String> nestedGroupIds);

  Group assignGroupProductsPermission(String groupId, List<String> productIds, BasePermissions permission);

  Group revokeGroupProductsPermission(String groupId, List<String> productIds, BasePermissions permission);
}
