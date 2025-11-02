package com.simplecommerce.group;

import com.simplecommerce.node.Node;

/// @author julius.krah
public record Group(String id, String name, @org.jspecify.annotations.Nullable String description) implements Node {

}
