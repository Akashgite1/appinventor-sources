// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts the combined project translation JSON into compiled per-language assets.
 *
 * <p>The input contains a base language, configured translation languages, and
 * translation entries keyed by their stable translation key. Static entries store
 * Designer source metadata, while dynamic entries store base text and placeholders.
 */
public final class I18nTranslationAssetGenerator {
  static final String MANIFEST_ASSET_PATH = "i18n/manifest.json";
  static final String LANGUAGE_ASSET_DIRECTORY = "i18n/languages/";
  private static final int MANIFEST_VERSION = 2;
  private static final int LANGUAGE_FILE_VERSION = 1;
  private static final String DEFAULT_BASE_LANGUAGE = "en";
  private static final String LANGUAGE_CODE_PATTERN = "[a-z]{2,3}(-[A-Z]{2})?";

  private I18nTranslationAssetGenerator() {
  }

  /**
   * Generates the manifest and per-language assets from the combined translation JSON.
   *
   * @param translationsJson combined project translation JSON
   * @return asset paths mapped to their JSON contents
   * @throws JSONException if the input JSON is malformed or contains an invalid language code
   */
  public static Map<String, String> generateTranslationAssets(
    String translationsJson) throws JSONException {
    Map<String, String> translationAssets = new LinkedHashMap<String, String>();

    if (translationsJson == null || translationsJson.trim().length() == 0) {
      return translationAssets;
    }

    JSONObject translationsRoot = new JSONObject(translationsJson);
    String baseLanguage = translationsRoot.optString("baseLanguage", DEFAULT_BASE_LANGUAGE).trim();
    validateLanguageCode(baseLanguage);

    JSONObject translationEntries = translationsRoot.optJSONObject("entries");
    if (translationEntries == null) {
      translationEntries = new JSONObject();
    }

    List<String> translationKeys = getTranslationEntryKeys(translationEntries);
    Set<String> translationLanguages =
        collectTranslationLanguages(
            translationsRoot, translationEntries, translationKeys, baseLanguage);

    JSONObject manifest = createTranslationManifest(
        translationEntries, translationKeys, translationLanguages, baseLanguage);

    translationAssets.put(MANIFEST_ASSET_PATH, manifest.toString());

    for (String language : translationLanguages) {
      JSONObject languageAsset = createTranslationLanguageAsset(
          translationEntries, translationKeys, language, baseLanguage);

      translationAssets.put(
          getLanguageAssetPath(language), languageAsset.toString());
    }

    return translationAssets;
  }

  private static Set<String> collectTranslationLanguages(
    JSONObject translationsRoot, JSONObject translationEntries,
    List<String> translationKeys, String baseLanguage) throws JSONException {
    Set<String> languages = new TreeSet<String>();
    languages.add(baseLanguage);

    JSONArray declaredLanguages = translationsRoot.optJSONArray("languages");
    if (declaredLanguages != null) {
      for (int i = 0; i < declaredLanguages.length(); i++) {
        String language = declaredLanguages.optString(i, "").trim();
        if (language.length() > 0) {
          validateLanguageCode(language);
          languages.add(language);
        }
      }
    }

    for (String key : translationKeys) {
      JSONObject entry = translationEntries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject translations = entry.optJSONObject("translations");
      if (translations == null) {
        continue;
      }

      Iterator<String> languageIterator = translations.keys();
      while (languageIterator.hasNext()) {
        String language = languageIterator.next();
        validateLanguageCode(language);
        languages.add(language);
      }
    }

    return languages;
  }

  private static JSONObject createTranslationManifest(
    JSONObject translationEntries, List<String> translationKeys,
    Set<String> languages, String baseLanguage) throws JSONException {
    JSONObject manifest = new JSONObject();
    manifest.put("version", MANIFEST_VERSION);
    manifest.put("baseLanguage", baseLanguage);

    JSONObject languagePaths = new JSONObject();
    for (String language : languages) {
      languagePaths.put(language, "languages/" + language + ".json");
    }
    manifest.put("languages", languagePaths);

    JSONObject manifestEntries = new JSONObject();
    for (String key : translationKeys) {
      JSONObject entry = translationEntries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject metadata = new JSONObject();
      String kind = entry.optString("kind", "");
      if (kind.length() > 0) {
        metadata.put("kind", kind);
      }

      if ("dynamic".equals(kind)) {
        metadata.put("baseText", getTranslationBaseText(entry));

        JSONArray placeholders = entry.optJSONArray("placeholders");
        metadata.put("placeholders",
            placeholders == null ? new JSONArray() : new JSONArray(placeholders.toString()));
      } else {
        JSONObject source = entry.optJSONObject("source");
        if (source != null) {
          metadata.put("source", new JSONObject(source.toString()));
        }
      }

      manifestEntries.put(key, metadata);
    }

    manifest.put("entries", manifestEntries);

    return manifest;
  }

  private static JSONObject createTranslationLanguageAsset(
    JSONObject translationEntries, List<String> translationKeys, String language,
    String baseLanguage) throws JSONException {

    JSONObject languageEntries = new JSONObject();
    for (String key : translationKeys) {
      JSONObject entry = translationEntries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject translations = entry.optJSONObject("translations");
      String value = translations == null ? "" : translations.optString(language, "");

      if (value.length() == 0 && language.equals(baseLanguage)) {
        value = getTranslationBaseText(entry);
      }

      if (value.length() > 0) {
        languageEntries.put(key, value);
      }
    }

    JSONObject languageAsset = new JSONObject();
    languageAsset.put("version", LANGUAGE_FILE_VERSION);
    languageAsset.put("language", language);
    languageAsset.put("entries", languageEntries);
    return languageAsset;
  }

  private static String getTranslationBaseText(JSONObject entry) {
    String baseText = entry.optString("baseText", "");
    if (baseText.length() > 0) {
      return baseText;
    }

    JSONObject source = entry.optJSONObject("source");
    return source == null ? "" : source.optString("baseText", "");
  }

  private static String getLanguageAssetPath(String language) {
    return LANGUAGE_ASSET_DIRECTORY + language + ".json";
  }

  private static void validateLanguageCode(String language) throws JSONException {
    if (language == null || !language.matches(LANGUAGE_CODE_PATTERN)) {
      throw new JSONException("Invalid i18n language code: " + language);
    }
  }

  private static List<String> getTranslationEntryKeys(JSONObject translationEntries) {
    List<String> translationKeys = new ArrayList<String>();
    Iterator<String> iterator = translationEntries.keys();

    while (iterator.hasNext()) {
      translationKeys.add(iterator.next());
    }

    return translationKeys;
  }
}
