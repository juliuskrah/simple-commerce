package com.simplecommerce.shared;

import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * Global identifier of the form {@code gid://SimpleCommerce/{node}/{id}}.
 * @since 1.0
 * @author julius.krah
 */
public record GlobalId(String node, String id) {

  /**
   * Encodes the global identifier to Base64.
   * @return Encoded global identifier
   */
  public String encode() {
    var gid = "gid://SimpleCommerce/%s/%s".formatted(node, id);
    Encoder encoder = Base64.getEncoder();
    return encoder.encodeToString(gid.getBytes());
  }

  public static GlobalId decode(String base64String) {
    return null;
  }
}
