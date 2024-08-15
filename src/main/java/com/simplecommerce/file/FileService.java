package com.simplecommerce.file;

/**
 * @author julius.krah
 */
interface FileService {

  /**
   * Stages a file upload.
   * @param input file parameters
   * @return a staged upload that contains the presigned URL and the resource URL
   */
  StagedUpload stageUpload(StagedUploadInput input);
}
