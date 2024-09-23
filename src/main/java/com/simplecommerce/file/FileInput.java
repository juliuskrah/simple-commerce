package com.simplecommerce.file;

import java.net.URL;

/**
 * Input type for file uploads. This is the input argument for the following operations:
 * <pre>
 *   mutation addDigitalContent(id: ID!, file: FileInput!)
 *   mutation addProductMedia(id: ID!, file: FileInput!)
 * </pre>
 * @param resourceUrl The URL of the file to be uploaded
 * @param contentType The content type of the file
 * @author julius.krah
 */
public record FileInput(URL resourceUrl, String contentType) { }
