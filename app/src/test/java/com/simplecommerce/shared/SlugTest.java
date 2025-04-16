package com.simplecommerce.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author julius.krah
 */
class SlugTest {

  @Test
  void shouldGenerateBasic() {
    String slug = Slug.generate("DataDynamo");
    assertThat(slug).isNotNull().isEqualTo("datadynamo");
  }

  @Test
  void shouldGenerateWith1Spaces() {
    String slug = Slug.generate("Pixel Pro");
    assertThat(slug).isNotNull().isEqualTo("pixel-pro");
  }

  @Test
  void shouldGenerateAndNormalize() {
    String slug = Slug.generate("Żółta Ćma");
    assertThat(slug).isNotNull().isEqualTo("zolta-cma");
  }

  @Test
  void shouldGenerateWith1SpacesBefore() {
    String slug = Slug.generate(" Pixel Pro");
    assertThat(slug).isNotNull().isEqualTo("pixel-pro");
  }

  @Test
  void shouldGenerateWithSpacesBefore() {
    String slug = Slug.generate("   Pixel Pro");
    assertThat(slug).isNotNull().isEqualTo("pixel-pro");
  }

  @Test
  void shouldGenerateWithSpaces() {
    String slug = Slug.generate("Pixel   Pro");
    assertThat(slug).isNotNull().isEqualTo("pixel-pro");
  }

  @Test
  void shouldGenerateWithSpecialCharactersAndSpaces() {
    String slug = Slug.generate("Life's Good");
    assertThat(slug).isNotNull().isEqualTo("life-s-good");
  }

  @Test
  void shouldGenerateWithSpecialCharacters() {
    String slug = Slug.generate("Yah!!");
    assertThat(slug).isNotNull().isEqualTo("yah");
  }

  @Test
  void shouldGenerateWithNumbers() {
    String slug = Slug.generate("1Password");
    assertThat(slug).isNotNull().isEqualTo("1password");
  }

  @Test
  void shouldGenerateWithSpecialCharactersAndSpacesAndNumbers() {
    String slug = Slug.generate("Life's Good 2");
    assertThat(slug).isNotNull().isEqualTo("life-s-good-2");
  }
}