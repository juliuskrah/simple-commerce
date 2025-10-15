package com.simplecommerce.shared.types;

import org.jspecify.annotations.Nullable;

///
/// Input type for permission tuples. This is the input argument for the following operations:
///
/// ```graphql
/// type mutation {
///     assignPermissionToActor(actorId: ID!, permission: [PermissionTupleInput!]!): Actor
///     removePermissionFromActor(actorId: ID!, permission: [PermissionTupleInput!]!): Actor
/// }
/// ```
///
/// @author julius.krah
///
public record PermissionTupleInput(
    String namespace,
    String object,
    String relation,
    SubjectInput subject
) {

  public record SubjectInput(@Nullable String subjectId, @Nullable SubjectSetInput subjectSet) {

  }

  public record SubjectSetInput(String namespace, String object, String relation) {

  }
}
