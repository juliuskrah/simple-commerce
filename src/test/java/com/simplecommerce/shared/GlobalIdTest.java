package com.simplecommerce.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

/**
 * @author julius.krah
 */
class GlobalIdTest {

  @Test
  void shouldEncodeToBase64() {
    var gid = new GlobalId("Product", "368bb677-ce99-4fb8-bb2b-2d681203068d");
    var encodedGid = gid.encode();
    assertThat(encodedGid).isNotNull().isBase64()
        .isEqualTo("Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8zNjhiYjY3Ny1jZTk5LTRmYjgtYmIyYi0yZDY4MTIwMzA2OGQ=");
  }

  @Test
  void shouldDecodeFromBase64() {
    var encodedGid = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8zNjhiYjY3Ny1jZTk5LTRmYjgtYmIyYi0yZDY4MTIwMzA2OGQ=";
    var gid = GlobalId.decode(encodedGid);
    assertThat(gid).isNotNull().extracting(GlobalId::node, GlobalId::id)
        .containsExactly("Product", "368bb677-ce99-4fb8-bb2b-2d681203068d");
  }

  @Test
  void shouldFailDecodeWhenNotBase64() {
    var invalidBase64 = "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC8zNjhi=====";
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> GlobalId.decode(invalidBase64))
        .withMessage("Input byte array has wrong 4-byte ending unit");
  }

  @Test
  void shouldFailDecodeWhenInvalidGid() {
    // id://SimpleCommerce/Product/1a381d01-8f00-4041-8d84-9212159064f9
    var invalidGid = "aWQ6Ly9TaW1wbGVDb21tZXJjZS9Qcm9kdWN0LzFhMzgxZDAxLThmMDAtNDA0MS04ZDg0LTkyMTIxNTkwNjRmOQ==";
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> GlobalId.decode(invalidGid))
        .withMessageContaining("Unexpected global identifier:");
  }

  @Test
  void shouldFailDecodeWhenNotAbsolute() {
    // //SimpleCommerce/Product/2df22cbb-e78e-41cb-b708-5d8ad95474ed
    var invalidGid = "Ly9TaW1wbGVDb21tZXJjZS9Qcm9kdWN0LzJkZjIyY2JiLWU3OGUtNDFjYi1iNzA4LTVkOGFkOTU0NzRlZA==";
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> GlobalId.decode(invalidGid))
        .withMessage("Invalid global identifier");
  }
}