package com.simplecommerce.file;

import com.simplecommerce.node.Node;
import java.time.OffsetDateTime;

/**
 * Super type for all files. This interfaces models the create and update metadata of a file.
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
