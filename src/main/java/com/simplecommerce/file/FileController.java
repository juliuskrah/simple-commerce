package com.simplecommerce.file;

import static com.simplecommerce.shared.Types.NODE_MEDIA_FILE;

import com.simplecommerce.product.Product;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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

  @SchemaMapping(typeName = "MediaFile")
  String id(MediaFile source) {
    return new GlobalId(NODE_MEDIA_FILE, source.id()).encode();
  }

  @SchemaMapping
  List<MediaFile> media(Product product) {
    return fileServiceSupplier.get().productMedia(product.id());
  }

  @SchemaMapping
  DigitalContent digitalContent(Product product) {
    return new DigitalContent();
  }

  @MutationMapping
  StagedUpload stagedUpload(@Argument StagedUploadInput input) {
    return fileServiceSupplier.get().stageUpload(input);
  }

  @MutationMapping
  MediaFile addProductMedia(@Argument String productId, @Argument FileInput file) {
    return fileServiceSupplier.get().addMediaToProduct(productId, file);
  }

  @MutationMapping
  DigitalContent addDigitalContent(@Argument String productId, @Argument FileInput file) {
    return new DigitalContent();
  }
}
