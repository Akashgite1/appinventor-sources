// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides suggested translation languages and canonical language-code handling.
 */
public final class TranslationLanguageCatalog {
  private static final List<LanguageOption> SUGGESTED_LANGUAGES =
      createSuggestedLanguages();

  private TranslationLanguageCatalog() {
  }

  public static List<LanguageOption> getSuggestedLanguages() {
    return SUGGESTED_LANGUAGES;
  }

  public static String getDisplayLabel(String languageCode) {
    String normalizedLanguageCode = normalizeLanguageCode(languageCode);

    if (normalizedLanguageCode.length() == 0) {
      return languageCode == null ? "" : languageCode.trim();
    }

    for (LanguageOption language : SUGGESTED_LANGUAGES) {
      if (language.getCode().equals(normalizedLanguageCode)) {
        return language.getDisplayLabel();
      }
    }

    return "Custom language (" + normalizedLanguageCode + ")";
  }

  public static boolean isValidLanguageCode(String languageCode) {
    return normalizeLanguageCode(languageCode).length() > 0;
  }

  /**
   * Normalizes a language tag using language, optional script, and optional
   * region subtags.
   */
  public static String normalizeLanguageCode(String languageCode) {
    if (languageCode == null) {
      return "";
    }

    String normalizedLanguageCode = languageCode.trim().replace('_', '-');

    if (normalizedLanguageCode.length() == 0
        || normalizedLanguageCode.startsWith("-")
        || normalizedLanguageCode.endsWith("-")
        || normalizedLanguageCode.indexOf("--") >= 0) {
      return "";
    }

    String[] subtags = normalizedLanguageCode.split("-");
    if (subtags.length < 1 || subtags.length > 3
        || !isLanguageSubtag(subtags[0])) {
      return "";
    }

    StringBuilder result = new StringBuilder();
    result.append(subtags[0].toLowerCase());

    if (subtags.length == 2) {
      if (isScriptSubtag(subtags[1])) {
        result.append('-').append(normalizeScriptSubtag(subtags[1]));
      } else if (isRegionSubtag(subtags[1])) {
        result.append('-').append(normalizeRegionSubtag(subtags[1]));
      } else {
        return "";
      }
    }

    if (subtags.length == 3) {
      if (!isScriptSubtag(subtags[1])
          || !isRegionSubtag(subtags[2])) {
        return "";
      }

      result.append('-').append(normalizeScriptSubtag(subtags[1]));
      result.append('-').append(normalizeRegionSubtag(subtags[2]));
    }

    return result.toString();
  }

  private static boolean isLanguageSubtag(String subtag) {
    return subtag.matches("[A-Za-z]{2,3}");
  }

  private static boolean isScriptSubtag(String subtag) {
    return subtag.matches("[A-Za-z]{4}");
  }

  private static boolean isRegionSubtag(String subtag) {
    return subtag.matches("([A-Za-z]{2}|[0-9]{3})");
  }

  private static String normalizeScriptSubtag(String subtag) {
    return subtag.substring(0, 1).toUpperCase()
        + subtag.substring(1).toLowerCase();
  }

  private static String normalizeRegionSubtag(String subtag) {
    return subtag.matches("[0-9]{3}")
        ? subtag
        : subtag.toUpperCase();
  }

  private static List<LanguageOption> createSuggestedLanguages() {
    List<LanguageOption> languages = new ArrayList<LanguageOption>();

    languages.add(new LanguageOption("English", "en"));
    languages.add(new LanguageOption("Armenian", "hy"));
    languages.add(new LanguageOption("Catalan", "ca"));
    languages.add(new LanguageOption("Chinese, Simplified", "zh-CN"));
    languages.add(new LanguageOption("Chinese, Traditional", "zh-TW"));
    languages.add(new LanguageOption("Dutch", "nl"));
    languages.add(new LanguageOption("French, France", "fr-FR"));
    languages.add(new LanguageOption("German", "de"));
    languages.add(new LanguageOption("Hindi", "hi"));
    languages.add(new LanguageOption("Hungarian", "hu"));
    languages.add(new LanguageOption("Italian, Italy", "it-IT"));
    languages.add(new LanguageOption("Japanese", "ja"));
    languages.add(new LanguageOption("Korean, South Korea", "ko-KR"));
    languages.add(new LanguageOption("Lithuanian", "lt"));
    languages.add(new LanguageOption("Polish", "pl"));
    languages.add(new LanguageOption("Portuguese", "pt"));
    languages.add(new LanguageOption("Portuguese, Brazil", "pt-BR"));
    languages.add(new LanguageOption("Russian", "ru"));
    languages.add(new LanguageOption("Spanish, Spain", "es-ES"));
    languages.add(new LanguageOption("Swedish", "sv"));
    languages.add(new LanguageOption("Turkish", "tr"));
    languages.add(new LanguageOption("Ukrainian", "uk"));

    return Collections.unmodifiableList(languages);
  }

  /**
   * A language displayed in translation-language selectors.
   */
  public static final class LanguageOption {
    private final String name;
    private final String code;

    private LanguageOption(String name, String code) {
      this.name = name;
      this.code = code;
    }

    public String getName() {
      return name;
    }

    public String getCode() {
      return code;
    }

    public String getDisplayLabel() {
      return name + " (" + code + ")";
    }
  }
}
