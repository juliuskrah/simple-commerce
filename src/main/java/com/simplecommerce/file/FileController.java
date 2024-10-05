package com.simplecommerce.file;

import static com.simplecommerce.shared.Types.NODE_MEDIA_FILE;

import com.simplecommerce.product.Product;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.function.SingletonSupplier;

/**
 * @author julius.krah
 */
@Controller
class FileController {
  private final ObjectProvider<FileService> fileService;
  // Defer creation of the FileService to avoid early initialization of aspectj proxy
  private final Supplier<FileService> fileServiceSupplier = SingletonSupplier.of(FileManagement::new);

  FileController(ObjectProvider<FileService> fileService) {
    this.fileService = fileService;
  }

  @SchemaMapping(typeName = "MediaFile")
  String id(MediaFile source) {
    return new GlobalId(NODE_MEDIA_FILE, source.id()).encode();
  }

  @SchemaMapping
  List<MediaFile> media(Product product) {
    return fileService.getIfAvailable(fileServiceSupplier).productMedia(product.id());
  }

  @SchemaMapping
  DigitalContent digitalContent(Product product) {
    return new DigitalContent();
  }

  @MutationMapping
  StagedUpload stagedUpload(@Argument StagedUploadInput input) {
    return fileService.getIfAvailable(fileServiceSupplier).stageUpload(input);
  }

  @MutationMapping
  MediaFile addProductMedia(@Argument String productId, @Argument FileInput file) {
    return fileService.getIfAvailable(fileServiceSupplier).addMediaToProduct(productId, file);
  }

  @MutationMapping
  DigitalContent addDigitalContent(@Argument String productId, @Argument FileInput file) {
    return new DigitalContent();
  }
}
