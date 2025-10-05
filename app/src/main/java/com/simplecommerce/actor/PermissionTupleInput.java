package com.simplecommerce.actor;

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

  record SubjectInput(@Nullable String subjectId, @Nullable SubjectSetInput subjectSet) {

  }

  record SubjectSetInput(String namespace, String object, String relation) {

  }
}
