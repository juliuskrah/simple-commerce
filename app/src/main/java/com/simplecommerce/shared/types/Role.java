package com.simplecommerce.shared.types;

import java.util.List;

public record Role(String name, List<Permission> permissions) {

  public record Permission(String namespace, String permission) {

  }
}
