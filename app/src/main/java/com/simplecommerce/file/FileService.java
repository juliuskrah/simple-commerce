package com.simplecommerce.file;

import java.util.List;

/**
 * Basic operations for working with files.
 * @author julius.krah
 */
interface FileService {

  /**
   * Stages a file upload.
   * @param input file parameters
   * @return a staged upload that contains the presigned URL and the resource URL
   */
  StagedUpload stageUpload(StagedUploadInput input);

  /**
   * Add media to a product.
   * @param productId the product id
   * @param file the url of file to be added
   * @return media object containing the file details
   */
  MediaFile addMediaToProduct(String productId, FileInput file);

  /**
   * Get media files associated with a product.
   * @param productId the product id
   * @return list of media files associated with the product
   */
  List<MediaFile> productMedia(String productId);

  /**
   * Add digital content to a variant.
   * @param variantId the variant id
   * @param file the url of file to be added
   * @return digital content object containing the file details
   */
  DigitalContent addDigitalContentToVariant(String variantId, FileInput file);

  /**
   * Get digital content for a variant.
   * @param variantId the variant id
   * @return digital content associated with the variant, if any
   */
  DigitalContent findDigitalContentByVariant(String variantId);
}
