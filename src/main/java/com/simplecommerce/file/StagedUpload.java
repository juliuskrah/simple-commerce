package com.simplecommerce.file;

import java.net.URL;

/**
 * Represents a staged upload.
 * @param presignedUrl
 * @param resourceUrl
 * @author julius.krah
 */
record StagedUpload(
    URL presignedUrl,
    URL resourceUrl,
    String contentType
) {}
