package com.simplecommerce.file;

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
    String contentType,
    URL url,
    OffsetDateTime updatedAt
) implements File { }
