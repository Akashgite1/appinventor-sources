// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.util.ArrayList;
import java.util.Collections;
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
  public static Map<String, String> generate(String translationsJson) throws JSONException {
    Map<String, String> assets = new LinkedHashMap<String, String>();

    if (translationsJson == null || translationsJson.trim().length() == 0) {
      return assets;
    }

    JSONObject root = new JSONObject(translationsJson);
    String baseLanguage = root.optString("baseLanguage", DEFAULT_BASE_LANGUAGE).trim();
    validateLanguageCode(baseLanguage);

    JSONObject entries = root.optJSONObject("entries");
    if (entries == null) {
      entries = new JSONObject();
    }

    List<String> entryKeys = getSortedKeys(entries);
    Set<String> languageSet = collectLanguages(root, entries, entryKeys, baseLanguage);
    List<String> languages = new ArrayList<String>(languageSet);

    JSONObject manifest = createManifest(entries, entryKeys, languages, baseLanguage);
    assets.put(MANIFEST_ASSET_PATH, manifest.toString());

    for (String language : languages) {
      JSONObject languageFile =
          createLanguageFile(entries, entryKeys, language, baseLanguage);
      assets.put(getLanguageAssetPath(language), languageFile.toString());
    }

    return assets;
  }

  private static Set<String> collectLanguages(JSONObject root, JSONObject entries,
      List<String> entryKeys, String baseLanguage) throws JSONException {
    Set<String> languages = new TreeSet<String>();
    languages.add(baseLanguage);

    JSONArray declaredLanguages = root.optJSONArray("languages");
    if (declaredLanguages != null) {
      for (int i = 0; i < declaredLanguages.length(); i++) {
        String language = declaredLanguages.optString(i, "").trim();
        if (language.length() > 0) {
          validateLanguageCode(language);
          languages.add(language);
        }
      }
    }

    for (String key : entryKeys) {
      JSONObject entry = entries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject translations = entry.optJSONObject("translations");
      if (translations == null) {
        continue;
      }

      for (String language : getSortedKeys(translations)) {
        validateLanguageCode(language);
        languages.add(language);
      }
    }

    return languages;
  }

  private static JSONObject createManifest(JSONObject entries, List<String> entryKeys,
      List<String> languages, String baseLanguage) throws JSONException {
    JSONObject manifest = new JSONObject();
    manifest.put("version", MANIFEST_VERSION);
    manifest.put("baseLanguage", baseLanguage);

    JSONObject languagePaths = new JSONObject();
    for (String language : languages) {
      languagePaths.put(language, "languages/" + language + ".json");
    }
    manifest.put("languages", languagePaths);

    JSONObject manifestEntries = new JSONObject();
    for (String key : entryKeys) {
      JSONObject entry = entries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject metadata = new JSONObject();
      String kind = entry.optString("kind", "");
      if (kind.length() > 0) {
        metadata.put("kind", kind);
      }

      if ("dynamic".equals(kind)) {
        metadata.put("baseText", getBaseText(entry));

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

  private static JSONObject createLanguageFile(JSONObject entries, List<String> entryKeys,
      String language, String baseLanguage) throws JSONException {
    JSONObject languageEntries = new JSONObject();

    for (String key : entryKeys) {
      JSONObject entry = entries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject translations = entry.optJSONObject("translations");
      String value = translations == null ? "" : translations.optString(language, "");

      if (value.length() == 0 && language.equals(baseLanguage)) {
        value = getBaseText(entry);
      }

      if (value.length() > 0) {
        languageEntries.put(key, value);
      }
    }

    JSONObject languageFile = new JSONObject();
    languageFile.put("version", LANGUAGE_FILE_VERSION);
    languageFile.put("language", language);
    languageFile.put("entries", languageEntries);
    return languageFile;
  }

  private static String getBaseText(JSONObject entry) {
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

  private static List<String> getSortedKeys(JSONObject object) {
    List<String> keys = new ArrayList<String>();

    if (object == null) {
      return keys;
    }

    java.util.Iterator<String> iterator = object.keys();
    while (iterator.hasNext()) {
      keys.add(iterator.next());
    }

    Collections.sort(keys);
    return keys;
  }
}
