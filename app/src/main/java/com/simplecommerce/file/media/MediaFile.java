package com.simplecommerce.file.media;

import com.simplecommerce.file.File;
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
public record MediaFile(
    String id,
    OffsetDateTime createdAt,
    String contentType,
    URL url,
    OffsetDateTime updatedAt
) implements File { }
