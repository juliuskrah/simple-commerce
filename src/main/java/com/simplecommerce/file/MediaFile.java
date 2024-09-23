package com.simplecommerce.file;

import java.net.URL;
import java.time.OffsetDateTime;

/**
 * Representation of a media file.
 * @param id The unique identifier of the media file
 * @param createdAt The date and time the media file was created
 * @param url The URL of the media file
 * @param updatedAt The date and time the media file was last updated
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
