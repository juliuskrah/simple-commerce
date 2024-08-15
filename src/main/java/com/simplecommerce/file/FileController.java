package com.simplecommerce.file;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * @author julius.krah
 */
@Controller
class FileController {
  @Autowired(required = false)
  private FileService fileService;

  private final Supplier<FileService> fileServiceSupplier = () -> {
    if (fileService != null) {
      return fileService;
    }
    fileService = new FileManagement();
    return fileService;
  };

  @MutationMapping
  StagedUpload stagedUpload(@Argument StagedUploadInput input) {
    return fileServiceSupplier.get().stageUpload(input);
  }
}
