package com.simplecommerce.actor.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link GroupManagement}. Focus on pure persistence logic & tuple emission contract.
 */
@ExtendWith(MockitoExtension.class)
class GroupManagementTests {

  @Mock
  Groups groups;
  @Mock
  GroupMembers groupMembers;
  @InjectMocks
  GroupManagement service;

  @Test
  void addGroupPersistsEntity() {
    var entity = new GroupEntity();
    entity.setId(UUID.randomUUID());
    entity.setName("Engineering");
    when(groups.saveAndFlush(any())).thenReturn(entity);
    var dto = service.addGroup("Engineering", null);
    assertThat(dto.name()).isEqualTo("Engineering");
  }

  @Test
  @Disabled
  void addMembersSkipsSelfCycle() {
    var id = UUID.fromString("32779848-78b5-4ba2-bdc3-fc97fc7f0c8a");
    var entity = new GroupEntity();
    entity.setId(id);
    entity.setName("Root");
    when(groups.findById(id)).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Group/32779848-78b5-4ba2-bdc3-fc97fc7f0c8a
    var gid = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvR3JvdXAvMzI3Nzk4NDgtNzhiNS00YmEyLWJkYzMtZmM5N2ZjN2YwYzhh";
    // nested group list contains itself; expect no nested group persisted
    var dto = service.addMembers(gid, List.of(), List.of(gid));
//    assertThat(dto.id()).isEqualTo(id.toString());
    verify(groupMembers, never()).saveAndFlush(argThat(m -> gid.equals(Optional.ofNullable(m.getMemberGroupId()).map(UUID::toString).orElse(""))));
  }

  @Test
  @Disabled
  void deepCyclePreventionSkipsIndirectCycle() {
    // parent -> A ; A -> parent should be prevented when attempting to add A to parent
    var parentId = UUID.fromString("8393296b-32f2-4429-bb8f-2b8e8687ee1f");
    var parent = new GroupEntity();
    parent.setId(parentId);
    parent.setName("Parent");
    var child = new GroupEntity();
    var childId = UUID.fromString("588c8c76-9815-45fe-8f03-dec1dd5b9254");
    child.setId(childId);
    child.setName("Child");
    when(groups.findById(parentId)).thenReturn(Optional.of(parent));
    // Simulate existing membership child -> parent causing potential cycle
    when(groupMembers.findByGroupId(childId)).thenReturn(List.of(GroupMemberEntity.forNestedGroup(childId, parentId)));
    // gid://SimpleCommerce/Group/8393296b-32f2-4429-bb8f-2b8e8687ee1f
    var pgid = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvR3JvdXAvODM5MzI5NmItMzJmMi00NDI5LWJiOGYtMmI4ZTg2ODdlZTFm";
    // gid://SimpleCommerce/Group/588c8c76-9815-45fe-8f03-dec1dd5b9254
    var cgid = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvR3JvdXAvNTg4YzhjNzYtOTgxNS00NWZlLThmMDMtZGVjMWRkNWI5MjU0";
    var dto = service.addMembers(pgid, List.of(), List.of(cgid));
//    assertThat(dto.id()).isEqualTo(parentId.toString());
    // Because cycle detected, no save for nested group should occur
    verify(groupMembers, never()).saveAndFlush(argThat(m -> childId.equals(m.getMemberGroupId())));
  }

  @Test
  void assignAndRevokeProductPermissionsNoBridge_NoExternalWrite() {
    var id = UUID.fromString("d5f701e7-afd0-43cd-aad9-646a9b4da1ef");
    var entity = new GroupEntity();
    entity.setId(id);
    entity.setName("Ops");
    when(groups.findById(id)).thenReturn(Optional.of(entity));
    // gid://SimpleCommerce/Group/d5f701e7-afd0-43cd-aad9-646a9b4da1ef
    var gid = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvR3JvdXAvZDVmNzAxZTctYWZkMC00M2NkLWFhZDktNjQ2YTliNGRhMWVm";
    var dto = service.assignGroupProductsPermission(gid, List.of("p1"), BasePermissions.VIEW_PRODUCTS);
    assertThat(dto.id()).isEqualTo(id.toString());
    dto = service.revokeGroupProductsPermission(gid, List.of("p1"), BasePermissions.VIEW_PRODUCTS);
    assertThat(dto.id()).isEqualTo(id.toString());
    // No authorization bridge means no external tuple writes
  }
}
