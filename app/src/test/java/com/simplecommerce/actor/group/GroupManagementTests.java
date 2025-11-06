package com.simplecommerce.actor.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
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
}
