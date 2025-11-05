package com.simplecommerce.shared.types;

import java.util.List;
import org.jspecify.annotations.Nullable;

/// @param actors the list of actor usernames
/// @param groups the list of group IDs
/// @author julius.krah
public record SubjectGroupInput(@Nullable List<String> actors, @Nullable List<String> groups) {

}
