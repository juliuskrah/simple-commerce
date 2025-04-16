package com.simplecommerce.node;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.file.FileNodeServiceSupplier;
import com.simplecommerce.product.ProductNodeServiceSupplier;
import com.simplecommerce.shared.Types;
import org.junit.jupiter.api.Test;

class NodeServiceSupplierTest {

  @Test
  void shouldLoadInstanceOfProductSupplier() {
    var supplier = NodeServiceSupplier.findFirst(s -> s.supports(Types.NODE_PRODUCT));
    assertThat(supplier).isPresent()
        .containsInstanceOf(ProductNodeServiceSupplier.class);
  }

  @Test
  void shouldLoadInstanceOfFileSupplier() {
    var supplier = NodeServiceSupplier.findFirst(s -> s.supports(Types.NODE_MEDIA_FILE));
    assertThat(supplier).isPresent()
        .containsInstanceOf(FileNodeServiceSupplier.class);
  }
}