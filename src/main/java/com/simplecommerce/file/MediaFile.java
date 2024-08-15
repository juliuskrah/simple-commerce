package com.simplecommerce.file;

import com.simplecommerce.node.Node;
import java.net.URL;
import java.time.OffsetDateTime;

/**
 * Represents a media file.
 * @param id
 * @param createdAt
 * @param url
 * @param updatedAt
 *
 * @author julius.krah
 */
record MediaFile(
    String id,
    OffsetDateTime createdAt,
    URL url,
    OffsetDateTime updatedAt
) implements File { }
