package com.simplecommerce.shared;

import java.net.URI;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Objects;

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

  /**
   * Decodes the global identifier from Base64.
   * @param base64String The encoded global identifier.
   * @return The decoded global identifier.
   * @throws IllegalArgumentException If the global identifier matches the following cases <br/>
   * <ul>
   *   <li> The base64String is not encoded in base64. </li>
   *   <li> The global identifier is not a valid URI. </li>
   *   <li> The global identifier is not absolute. </li>
   *   <li> The global identifier does not start with {@literal gid}. </li>
   * </ul>
   */
  public static GlobalId decode(String base64String) {
    byte[] bytes = Base64.getDecoder().decode(base64String);
    var uri = URI.create(new String(bytes)); // gid://SimpleCommerce/{node}/{id}
    var gid = switch (uri) {
      case URI url when !url.isAbsolute() -> throw new IllegalArgumentException("Invalid global identifier: " + base64String);
      case URI url when Objects.equals(url.getScheme(), "gid") -> {
        var path = uri.getRawPath().split("/"); // path[0] = , path[1] = {node}, path[2] = {id}
        yield new GlobalId(path[1], path[2]);
      }
      default -> throw new IllegalArgumentException("Unexpected global identifier: " + uri);
    };
    return gid;
  }
}
