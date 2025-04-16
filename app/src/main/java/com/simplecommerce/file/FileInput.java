package com.simplecommerce.file;

import java.net.URL;

/**
 * Input type for file uploads. This is the input argument for the following operations:
 * <ul>
 *   <li>mutation addDigitalContent(id: ID!, file: FileInput!)</li>
 *   <li>mutation addProductMedia(id: ID!, file: FileInput!)</li>
 * </ul>
 * @param resourceUrl The URL of the file to be uploaded
 * @param contentType The content type of the file
 * @author julius.krah
 */
public record FileInput(URL resourceUrl, String contentType) { }
