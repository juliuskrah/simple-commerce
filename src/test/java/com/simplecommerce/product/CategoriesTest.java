package com.simplecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.simplecommerce.DataPostgresTest;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.support.WindowIterator;

/**
 * The test classes in this file follow the category taxonomy below:
 * <pre>
 * Mature/
 * ├─ Weapons & Weapon Accessories/
 * │  ├─ Weapon Care & Accessories/
 * │  │  ├─ Ammunition
 * │  │  ├─ Ammunition Cases & Holders
 * │  │  ├─ Reloading Supplies & Equipment/
 * │  │  │  ├─ Ammunition Reloading Presses
 * │  │  ├─ Weapon Cases & Range Bags
 * │  │  ├─ Weapon Cleaning/
 * │  │  │  ├─ Cleaning Cloths & Swabs
 * │  │  │  ├─ Cleaning Patches
 * │  │  │  ├─ Cleaning Solvents
 * │  │  ├─ Weapon Grips
 * │  │  ├─ Weapon Holsters
 * │  │  ├─ Weapon Lights
 * │  │  ├─ Weapon Rails
 * │  │  ├─ Weapon Slings
 * │  ├─ Weapons/
 * │  │  ├─ Brass Knuckles
 * │  │  ├─ Clubs & Batons
 * │  │  ├─ Combat Knives
 * │  │  ├─ Guns
 * │  │  ├─ Mace & Pepper Spray
 * │  │  ├─ Nunchucks
 * │  │  ├─ Spears
 * │  │  ├─ Staff & Stick Weapons
 * │  │  ├─ Stun Guns & Tasers
 * │  │  ├─ Swords
 * │  │  ├─ Throwing Stars
 * │  │  ├─ Whips
 * ├─ Erotic/
 * │  ├─ Erotic Books
 * │  ├─ Erotic Clothing
 * │  ├─ Erotic Food & Edibles
 * │  ├─ Erotic Magazines
 * │  ├─ Erotic Videos
 * │  ├─ Sex Toys & Erotic Games
 * Hardware/
 * Bundles/
 * </pre>
 * @author julius.krah
 */
@DataPostgresTest
class CategoriesTest {
  @Autowired Categories categoryRepository;

  @Test
  void shouldFindCategoryById() {
    var found = categoryRepository.findById(UUID.fromString("a0dab2f2-fb3d-40ed-bacf-f31dedd10f45"));
    assertThat(found).isPresent()
        .get().hasFieldOrPropertyWithValue("title", "Bundles")
        .hasFieldOrPropertyWithValue("slug", "bundles")
        .hasFieldOrPropertyWithValue("path", "Bundles");
  }

  @Test
  void shouldFindTreeLevelForCategory() {
    var level = categoryRepository.findTreeLevel(UUID.fromString("ae3d3d1f-703b-4878-9050-8034523f85ce")); // Hardware
    assertThat(level).isPresent()
        .get().isEqualTo(1);
  }

  @Test
  void shouldNotFindTreeLevelForMissingCategory() {
    var level = categoryRepository.findTreeLevel(UUID.randomUUID());
    assertThat(level).isNotPresent();
  }

  @Test
  void shouldFindTreeLevelForMultipleCategories() {
    var categoryIds = Set.of(
        UUID.fromString("4086eb75-f11a-49e6-8dcc-1825bdd40bde"), // level 1 (Mature)
        UUID.fromString("cb625b5a-0d43-4628-80e3-0c08a9b89e02"), // level 2 (Erotic)
        UUID.randomUUID(),                                             // missing
        UUID.fromString("4e3b1143-bd62-419e-9513-ede27411e3f5"), // level 3 (Erotic Magazines)
        UUID.fromString("13f3caf6-6665-4a4c-b5f7-1a1f3d204b1c"), // level 2 (Weapons & Weapon Accessories)
        UUID.fromString("b970ca6d-4c07-40b4-8513-edee51d12c4e"), // level 3 (Weapon Care & Accessories)
        UUID.fromString("7edaf30b-e700-42ce-bb0b-c2b9e0da63ca"), // level 4 (Ammunition)
        UUID.fromString("e38bcfe9-c418-4b46-af04-c2faba3164d8"), // level 5 (Ammunition Reloading Presses)
        UUID.fromString("ad5d8835-cbbe-4d7f-8be2-efc8b80c4b88"), // level 4 (Weapon Cleaning)
        UUID.fromString("56bec194-e68a-4f44-bace-76dfcf5b0b0f"), // level 5 (Cleaning Cloths & Swabs)
        UUID.fromString("c0bdb6d4-a372-49cb-b6fc-af686484cdfc"), // level 3 (Weapons)
        UUID.fromString("7293c511-0eb4-4eca-9c26-3d187128e59b")  // level 4 (Brass Knuckles)
    );
    try(var levels = categoryRepository.findTreeLevel(categoryIds)) {
      assertThat(levels).containsExactly(1, 2, 3, 2, 3, 4, 5, 4, 5, 3, 4);
    }
  }

  @Test
  void shouldFindAllAncestorsById() {
    var categoryId = UUID.fromString("e38bcfe9-c418-4b46-af04-c2faba3164d8"); // level 5 (Ammunition Reloading Presses)
    try (var ancestors = categoryRepository.findAncestorsById(categoryId)) {
      assertThat(ancestors).extracting("title")
          .containsExactly(
              "Mature",
              "Weapons & Weapon Accessories",
              "Weapon Care & Accessories",
              "Reloading Supplies & Equipment",
              "Ammunition Reloading Presses");
    }
  }

  @Test
  void shouldFindWindowedAncestorsById() {
    var categoryId = UUID.fromString("3b41a7e4-0bfd-47ed-b58e-2d39822fe1f8"); // level 5 (Cleaning Patches)
    var ancestors = categoryRepository.findAncestorsById(categoryId, Limit.unlimited(), Sort.unsorted(), ScrollPosition.offset());
    assertThat(ancestors).hasSize(5)
        .extracting("title")
        .containsExactly(
            "Mature",
            "Weapons & Weapon Accessories",
            "Weapon Care & Accessories",
            "Weapon Cleaning",
            "Cleaning Patches");
  }

  @Test
  void shouldFindScrolledAncestorsById() {
    var categoryId = UUID.fromString("f0427d1b-6c81-4570-b173-a8bf32b124d4"); // level 5 (Cleaning Solvents)
    var ancestors = WindowIterator.of(position ->
            categoryRepository.findAncestorsById(categoryId, Limit.of(3), Sort.unsorted(), position))
        .startingAt(ScrollPosition.offset());
    assertThat(ancestors).hasNext()
        .toIterable().hasSize(5)
        .extracting("title")
        .containsExactly(
            "Mature",
            "Weapons & Weapon Accessories",
            "Weapon Care & Accessories",
            "Weapon Cleaning",
            "Cleaning Solvents");
  }

  @Test
  void shouldFindSortedAncestorsById() {
    var categoryId = UUID.fromString("3b41a7e4-0bfd-47ed-b58e-2d39822fe1f8"); // level 5 (Cleaning Patches)
    var ancestors = categoryRepository.findAncestorsById(categoryId, Limit.unlimited(), Sort.by(Order.desc("title")), ScrollPosition.keyset());
    assertThat(ancestors).hasSize(5)
        .extracting("title")
        .containsExactly(
            "Weapons & Weapon Accessories",
            "Weapon Cleaning",
            "Weapon Care & Accessories",
            "Mature",
            "Cleaning Patches");
  }

  @Test
  void shouldFindAllDescendantsById() {
    var categoryId = UUID.fromString("b970ca6d-4c07-40b4-8513-edee51d12c4e"); // level 3 (Weapon Care & Accessories)
    try (var descendants = categoryRepository.findDescendantsById(categoryId)) {
      assertThat(descendants).hasSize(15)
          .extracting("title")
          .containsExactly(
              "Weapon Care & Accessories",
              "Ammunition",
              "Ammunition Cases & Holders",
              "Reloading Supplies & Equipment",
              "Ammunition Reloading Presses",
              "Weapon Cases & Range Bags",
              "Weapon Cleaning",
              "Cleaning Cloths & Swabs",
              "Cleaning Patches",
              "Cleaning Solvents",
              "Weapon Grips",
              "Weapon Holsters",
              "Weapon Lights",
              "Weapon Rails",
              "Weapon Slings");
    }
  }

  @Test
  void shouldFindWindowedDescendantsById() {
    var categoryId = UUID.fromString("ad5d8835-cbbe-4d7f-8be2-efc8b80c4b88"); // level 4 (Weapon Cleaning)
    var descendants = categoryRepository.findDescendantsById(categoryId, Limit.unlimited(), Sort.unsorted(), ScrollPosition.offset());
    assertThat(descendants).hasSize(4)
        .extracting("title")
        .containsExactly(
            "Weapon Cleaning",
            "Cleaning Cloths & Swabs",
            "Cleaning Patches",
            "Cleaning Solvents");
  }

  @Test
  void shouldFindScrolledDescendantsById() {
    var categoryId = UUID.fromString("cb625b5a-0d43-4628-80e3-0c08a9b89e02"); // level 2 (Erotic)
    var descendants = WindowIterator.of(position ->
            categoryRepository.findDescendantsById(categoryId, Limit.of(3), Sort.unsorted(), position))
        .startingAt(ScrollPosition.offset());
    assertThat(descendants).hasNext()
        .toIterable().hasSize(7)
        .extracting("title")
        .containsExactly(
            "Erotic",
            "Erotic Clothing",
            "Erotic Food & Edibles",
            "Erotic Magazines",
            "Sex Toys & Erotic Games",
            "Erotic Videos",
            "Erotic Books");
  }

  @Test
  void shouldFindSortedDescendantsById() {
    var categoryId = UUID.fromString("1f12c348-02e1-42ed-a4bc-058478506f25"); // level 4 (Reloading Supplies & Equipment)
    var descendants = categoryRepository.findDescendantsById(categoryId, Limit.unlimited(), Sort.by(Order.asc("title")), ScrollPosition.keyset());
    assertThat(descendants).hasSize(2)
        .extracting("title")
        .containsExactly(
            "Ammunition Reloading Presses",
            "Reloading Supplies & Equipment");
  }

  @Test
  void shouldFindParentWhenNotRoot() {
    var categoryId = UUID.fromString("1f12c348-02e1-42ed-a4bc-058478506f25"); // level 4 (Reloading Supplies & Equipment)
    var category = categoryRepository.findById(categoryId);
    assertThat(category).isPresent();
    var parent = categoryRepository.findParent(category.get().getId());
    assertThat(parent).isPresent()
        .get().hasFieldOrPropertyWithValue("title", "Weapon Care & Accessories")
        .hasFieldOrPropertyWithValue("slug", "weapon-care-accessories")
        .hasFieldOrPropertyWithValue("path", "Mature.Weapons_Weapon_Accessories.Weapon_Care_Accessories");
  }

  @Test
  void shouldNotFindParentWhenRoot() {
    var categoryId = UUID.fromString("ae3d3d1f-703b-4878-9050-8034523f85ce"); // level 1 (Hardware)
    var category = categoryRepository.findById(categoryId);
    assertThat(category).isPresent();
    var parent = categoryRepository.findParent(category.get().getId());
    assertThat(parent).isNotPresent();
  }

  @Test
  void shouldFindTrueForIsLeaf() {
    var isLeaf = categoryRepository.isLeaf(UUID.fromString("f0427d1b-6c81-4570-b173-a8bf32b124d4")); // level 4 (Cleaning Solvents)
    assertThat(isLeaf).isTrue();
  }

  @Test
  void shouldFindFalseForIsLeaf() {
    var isLeaf = categoryRepository.isLeaf(UUID.fromString("ad5d8835-cbbe-4d7f-8be2-efc8b80c4b88")); // level 3 (Weapon Cleaning)
    assertThat(isLeaf).isFalse();
  }

  @Test
  void shouldFindTrueForIsRoot() {
    var isRoot = categoryRepository.isRoot(UUID.fromString("4086eb75-f11a-49e6-8dcc-1825bdd40bde")); // level 1 (Mature)
    assertThat(isRoot).isTrue();
  }

  @Test
  void shouldFindFalseForIsRoot() {
    var isRoot = categoryRepository.isRoot(UUID.fromString("c0bdb6d4-a372-49cb-b6fc-af686484cdfc")); // level 3 (Weapons)
    assertThat(isRoot).isFalse();
  }
}