package com.simplecommerce.file;

/**
 * Represents a media file.
 * @param filename
 * @param contentType
 */
record StagedUploadInput(
    String filename,
    String contentType
) { }
