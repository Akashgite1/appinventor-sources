// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class I18nTranslationAssetGeneratorTest {

  @Test
  public void generatesManifestAndPerLanguageFiles() throws Exception {
    Map<String, String> assets =
        I18nTranslationAssetGenerator.generateTranslationAssets(createTranslationsJson());

    assertEquals(4, assets.size());
    assertTrue(assets.containsKey("i18n/manifest.json"));
    assertTrue(assets.containsKey("i18n/languages/en.json"));
    assertTrue(assets.containsKey("i18n/languages/hi.json"));
    assertTrue(assets.containsKey("i18n/languages/es.json"));

    JSONObject manifest = new JSONObject(assets.get("i18n/manifest.json"));
    assertEquals(2, manifest.getInt("version"));
    assertEquals("en", manifest.getString("baseLanguage"));

    JSONObject languagePaths = manifest.getJSONObject("languages");
    assertEquals("languages/en.json", languagePaths.getString("en"));
    assertEquals("languages/hi.json", languagePaths.getString("hi"));
    assertEquals("languages/es.json", languagePaths.getString("es"));

    JSONObject manifestEntries = manifest.getJSONObject("entries");

    JSONObject staticEntry = manifestEntries.getJSONObject("submit_button");
    assertEquals("static", staticEntry.getString("kind"));
    assertEquals("Submit",
        staticEntry.getJSONObject("source").getString("baseText"));
    assertFalse(staticEntry.has("translations"));

    JSONObject dynamicEntry = manifestEntries.getJSONObject("welcome_message");
    assertEquals("dynamic", dynamicEntry.getString("kind"));
    assertEquals("Hello {name}", dynamicEntry.getString("baseText"));
    assertEquals("name", dynamicEntry.getJSONArray("placeholders").getString(0));
    assertFalse(dynamicEntry.has("translations"));
  }

  @Test
  public void createsBaseLanguageValuesFromBaseText() throws Exception {
    Map<String, String> assets =
        I18nTranslationAssetGenerator.generateTranslationAssets(createTranslationsJson());

    JSONObject english =
        new JSONObject(assets.get("i18n/languages/en.json"));
    JSONObject entries = english.getJSONObject("entries");

    assertEquals("Submit", entries.getString("submit_button"));
    assertEquals("Hello {name}", entries.getString("welcome_message"));
  }

  @Test
  public void omitsMissingValuesFromNonBaseLanguage() throws Exception {
    Map<String, String> assets =
        I18nTranslationAssetGenerator.generateTranslationAssets(createTranslationsJson());

    JSONObject spanish =
        new JSONObject(assets.get("i18n/languages/es.json"));
    JSONObject entries = spanish.getJSONObject("entries");

    assertEquals("Enviar", entries.getString("submit_button"));
    assertFalse(entries.has("welcome_message"));
  }

  @Test
  public void usesSourceBaseTextForDynamicFallback() throws Exception {
    JSONObject source = new JSONObject();
    source.put("baseText", "Welcome {name}");

    JSONObject dynamicEntry = new JSONObject();
    dynamicEntry.put("kind", "dynamic");
    dynamicEntry.put("source", source);
    dynamicEntry.put("placeholders", new JSONArray().put("name"));

    JSONObject entries = new JSONObject();
    entries.put("welcome_message", dynamicEntry);

    JSONObject root = new JSONObject();
    root.put("baseLanguage", "en");
    root.put("entries", entries);

    Map<String, String> assets =
        I18nTranslationAssetGenerator.generateTranslationAssets(root.toString());

    JSONObject manifest =
        new JSONObject(assets.get("i18n/manifest.json"));
    assertEquals("Welcome {name}",
        manifest.getJSONObject("entries")
            .getJSONObject("welcome_message")
            .getString("baseText"));

    JSONObject english =
        new JSONObject(assets.get("i18n/languages/en.json"));
    assertEquals("Welcome {name}",
        english.getJSONObject("entries")
            .getString("welcome_message"));
  }

  @Test
  public void returnsNoAssetsForEmptyInput() throws Exception {
    assertTrue(I18nTranslationAssetGenerator.generateTranslationAssets("").isEmpty());
    assertTrue(I18nTranslationAssetGenerator.generateTranslationAssets(null).isEmpty());
  }

  @Test
  public void supportsCanonicalLanguageCodeForms() throws Exception {
    String[] languages = {
        "mr", "pt-BR", "zh-Hans", "zh-Hant-TW", "es-419"
    };

    JSONArray configuredLanguages = new JSONArray();
    for (String language : languages) {
      configuredLanguages.put(language);
    }

    JSONObject root = new JSONObject();
    root.put("baseLanguage", "en");
    root.put("languages", configuredLanguages);
    root.put("entries", new JSONObject());

    Map<String, String> assets =
        I18nTranslationAssetGenerator.generateTranslationAssets(root.toString());

    JSONObject manifest = new JSONObject(assets.get("i18n/manifest.json"));
    JSONObject languagePaths = manifest.getJSONObject("languages");

    for (String language : languages) {
      String assetPath = "i18n/languages/" + language + ".json";
      assertTrue(assets.containsKey(assetPath));
      assertEquals("languages/" + language + ".json",
        languagePaths.getString(language));
    }
  }

  @Test(expected = JSONException.class)
  public void rejectsUnsafeLanguageCodes() throws Exception {
    JSONObject root = new JSONObject();
    root.put("baseLanguage", "en");
    root.put("languages", new JSONArray().put("../hi"));
    root.put("entries", new JSONObject());

    I18nTranslationAssetGenerator.generateTranslationAssets(root.toString());
  }

  @Test
  public void rejectsMalformedLanguageCodes() throws Exception {
    String[] invalidLanguages = {
        "zh-hans",
        "en-us",
        "zh-Hant-tw",
        "es-41",
        "en-US-extra",
        "english",
        "en/US"
    };

    for (String language : invalidLanguages) {
      JSONObject root = new JSONObject();
      root.put("baseLanguage", "en");
      root.put("languages", new JSONArray().put(language));
      root.put("entries", new JSONObject());

      try {
        I18nTranslationAssetGenerator.generateTranslationAssets(root.toString());
      } catch (JSONException expected) {
        continue;
      }

      throw new AssertionError(
          "Expected invalid language code to be rejected: " + language);
    }
  }

  private static String createTranslationsJson() throws JSONException {
    JSONObject root = new JSONObject();
    root.put("baseLanguage", "en");
    root.put("languages", new JSONArray().put("hi").put("es"));

    JSONObject entries = new JSONObject();

    JSONObject staticSource = new JSONObject();
    staticSource.put("screen", "Screen1");
    staticSource.put("component", "Button1");
    staticSource.put("type", "Button");
    staticSource.put("property", "Text");
    staticSource.put("baseText", "Submit");

    JSONObject staticTranslations = new JSONObject();
    staticTranslations.put("hi", "Submit Hindi");
    staticTranslations.put("es", "Enviar");

    JSONObject staticEntry = new JSONObject();
    staticEntry.put("kind", "static");
    staticEntry.put("source", staticSource);
    staticEntry.put("translations", staticTranslations);
    entries.put("submit_button", staticEntry);

    JSONObject dynamicTranslations = new JSONObject();
    dynamicTranslations.put("hi", "Hello Hindi {name}");

    JSONObject dynamicEntry = new JSONObject();
    dynamicEntry.put("kind", "dynamic");
    dynamicEntry.put("baseText", "Hello {name}");
    dynamicEntry.put("placeholders", new JSONArray().put("name"));
    dynamicEntry.put("translations", dynamicTranslations);
    entries.put("welcome_message", dynamicEntry);

    root.put("entries", entries);
    return root.toString();
  }
}
