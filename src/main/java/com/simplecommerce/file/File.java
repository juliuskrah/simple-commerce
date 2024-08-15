package com.simplecommerce.file;

import com.simplecommerce.node.Node;
import java.time.OffsetDateTime;

/**
 * @author julius.krah
 */
interface File extends Node {

  /**
   * File attribute: Created timestamp.
   * @return time of creation
   */
  OffsetDateTime createdAt();

  /**
   * File attribute: Updated timestamp.
   * @return time of last update
   */
  OffsetDateTime updatedAt();
}
