package com.simplecommerce.shared;

import com.simplecommerce.shared.types.GlobalId;
import org.springframework.stereotype.Service;

/**
 * Service for handling global identifiers.
 * 
 * @author julius.krah
 */
@Service
public class IdService {

  /**
   * Creates a new GlobalId.
   */
  public GlobalId createId(String node, String id) {
    return new GlobalId(node, id);
  }

  /**
   * Decodes a base64 encoded global identifier.
   */
  public GlobalId decodeId(String base64String) {
    return GlobalId.decode(base64String);
  }
}