package com.simplecommerce.file;

import java.net.URL;

/**
 * Represents a staged upload.
 * @param presignedUrl a presigned URL for uploading a file. The authorization token is embedded in the URL
 * @param resourceUrl the URL of the file to be uploaded
 * @author julius.krah
 */
record StagedUpload(
    URL presignedUrl,
    URL resourceUrl,
    String contentType
) {}
