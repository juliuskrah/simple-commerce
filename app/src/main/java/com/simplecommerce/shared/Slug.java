package com.simplecommerce.shared;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A slug is a short label representing an object or page's content. The main
 * reason for using a slug is the ability to incorporate it into URLs.
 * @author julius.krah
 */
public final class Slug {

  private static final Pattern STRIP_ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
  private Slug() {}

  /**
   * A slug is a short label representing an object or page's content. The main
   * reason for using a slug is the ability to incorporate it into URLs. Translated
   * names can contain all UTF-8 signs, allowing for URLs to be written by people with
   * different keyboard layouts. <br/>
   * Slug can contain:
   * <ul>
   *   <li>letters</li>
   *   <li>numbers</li>
   *   <li>hyphens</li>
   * </ul>
   *
   * For example, we would like to create a product called "Żółta Ćma" ("yellow moth").
   * The expected slug for such a product would be {@code zolta-cma}.
   * @param title The title to generate a slug from.
   * @return The generated slug.
   */
  public static String generate(String title) {
    String regex = "[\\s\\W&&[\\P{IsLatin}]]+";
    return Pattern.compile(regex)
        .splitAsStream(title)
        .filter(s -> !s.isBlank())
        .map(Slug::stripAccents)
        .map(String::toLowerCase)
        .collect(Collectors.joining("-"));
  }

  // Replace instances of Ł and ł with L and l respectively
  private static void convertRemainingAccentCharacters(StringBuilder decomposed) {
    for(int i = 0; i < decomposed.length(); ++i) {
      if (decomposed.charAt(i) == 321) {
        decomposed.setCharAt(i, 'L');
      } else if (decomposed.charAt(i) == 322) {
        decomposed.setCharAt(i, 'l');
      }
    }
  }

  // Copied from org.apache.commons.lang3.StringUtils.stripAccents
  private static String stripAccents(String input) {
    if (input == null) {
      return null;
    } else {
      StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Form.NFD));
      convertRemainingAccentCharacters(decomposed);
      return STRIP_ACCENTS_PATTERN.matcher(decomposed).replaceAll("");
    }
  }
}
