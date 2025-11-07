package com.simplecommerce.file;

import static com.simplecommerce.shared.types.Types.NODE_DIGITAL_CONTENT;
import static com.simplecommerce.shared.types.Types.NODE_MEDIA_FILE;

import com.simplecommerce.file.digitalcontent.DigitalContent;
import com.simplecommerce.file.media.MediaFile;
import com.simplecommerce.shared.types.Product;
import com.simplecommerce.product.variant.ProductVariant;
import com.simplecommerce.product.variant.ProductVariantManagement;
import com.simplecommerce.product.variant.ProductVariantService;
import com.simplecommerce.shared.GlobalId;
import java.util.List;
import java.util.Optional;
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
  private final ObjectProvider<ProductVariantService> variantService;
  // Defer creation of the FileService to avoid early initialization of aspectj proxy
  private final Supplier<FileService> fileServiceSupplier = SingletonSupplier.of(FileManagement::new);
  private final Supplier<ProductVariantService> variantServiceSupplier = SingletonSupplier.of(ProductVariantManagement::new);

  FileController(ObjectProvider<FileService> fileService, ObjectProvider<ProductVariantService> variantService) {
    this.fileService = fileService;
    this.variantService = variantService;
  }

  @SchemaMapping(typeName = "MediaFile")
  String id(MediaFile source) {
    return new GlobalId(NODE_MEDIA_FILE, source.id()).encode();
  }

  @SchemaMapping
  List<MediaFile> media(Product product) {
    return fileService.getIfAvailable(fileServiceSupplier).productMedia(product.id());
  }

  @SchemaMapping(typeName = "ProductVariant")
  Optional<DigitalContent> digitalContent(ProductVariant source) {
    var digitalContent = fileService.getIfAvailable(fileServiceSupplier).findDigitalContentByVariant(source.id());
    return Optional.ofNullable(digitalContent);
  }

  @SchemaMapping(typeName = "DigitalContent")
  String id(DigitalContent source) {
    return new GlobalId(NODE_DIGITAL_CONTENT, source.id()).encode();
  }

  @SchemaMapping(typeName = "DigitalContent")
  ProductVariant variant(DigitalContent source) {
    return variantService.getIfAvailable(variantServiceSupplier).findVariant(source.variantId());
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
  DigitalContent addVariantDigitalContent(@Argument String variantId, @Argument FileInput file) {
    return fileService.getIfAvailable(fileServiceSupplier).addDigitalContentToVariant(variantId, file);
  }
}
