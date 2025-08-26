package com.simplecommerce.file;

import java.net.URL;
import java.time.OffsetDateTime;

/**
 * Represents digital content for a product variant.
 * @author julius.krah
 */
public record DigitalContent(
    String id,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String variantId,
    URL url,
    String contentType
) implements File {

}
