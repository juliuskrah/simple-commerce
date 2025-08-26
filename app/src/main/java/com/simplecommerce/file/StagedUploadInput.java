package com.simplecommerce.file;

/**
 * Represents a media file.
 * @param filename The name of the file
 * @param contentType The content type of the file
 * @author julius.krah
 */
record StagedUploadInput(
    String filename,
    String contentType
) {

}
