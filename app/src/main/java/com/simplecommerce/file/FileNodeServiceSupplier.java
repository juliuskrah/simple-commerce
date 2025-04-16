package com.simplecommerce.file;

import com.simplecommerce.node.NodeService;
import com.simplecommerce.node.NodeServiceSupplier;
import com.simplecommerce.shared.Types;
import org.springframework.util.function.SingletonSupplier;

/**
 * Supplies a {@link NodeService} for media files. By registering this as a Service Provider, we avoid a
 * cyclic dependency between the {@code node} module and the {@code file} module.
 * @author julius.krah
 */
public class FileNodeServiceSupplier implements NodeServiceSupplier {
  private final SingletonSupplier<NodeService> fileNodeService = SingletonSupplier.of(FileManagement::new);

  @Override
  public boolean supports(String node) {
    return Types.NODE_MEDIA_FILE.equals(node);
  }

  @Override
  public NodeService getNodeService() {
    return fileNodeService.obtain();
  }
}
