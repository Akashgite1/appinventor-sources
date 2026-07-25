// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.google.appinventor.components.runtime.Form;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Loads and applies App Inventor i18n translation data bundled with compiled apps.
 */
public final class I18nTranslationManager {
  private static final String LOG_TAG = "I18nTranslationManager";
  private static final int MANIFEST_VERSION = 2;
  private static final int LANGUAGE_FILE_VERSION = 1;
  private static final String MANIFEST_ASSET = "i18n/manifest.json";
  private static final String LEGACY_TRANSLATIONS_ASSET = "i18n/translations.json";
  private static final String I18N_ASSET_PREFIX = "i18n/";

  // Combined format used by Companion preview and older compiled apps.
  private JSONObject translationsRoot;

  // Split format used by newly compiled apps.
  private JSONObject manifestRoot;
  private JSONObject activeLanguageEntries;
  private String activeLanguage = "";

  private String previewLanguageOverride = "";

  /**
   * Loads translations for a compiled application.
   *
   * <p>The split format is preferred. The legacy combined asset is used only
   * when the split manifest is unavailable or invalid.</p>
   */
  public void load(TranslationProvider provider) {
    if (provider == null) {
      return;
    }

    clearLoadedTranslations();

    if (loadSplitAssets(provider)) {
      return;
    }

    loadLegacyAsset(provider);
  }

  /**
   * Loads the combined JSON used by Companion preview.
   */
  public void loadFromJson(String json, TranslationProvider provider) {
    if (json == null || json.trim().length() == 0) {
      clear();
      return;
    }

    try {
      JSONObject parsedRoot = new JSONObject(json);

      clearLoadedTranslations();
      translationsRoot = parsedRoot;

      applyLoadedTranslations(provider);
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Invalid i18n translations JSON.", e);
    }
  }

  /**
   * Reapplies translations already loaded by either input format.
   */
  public void applyLoadedTranslations(TranslationProvider provider) {
    if (provider == null) {
      return;
    }

    try {
      if (manifestRoot != null) {
        JSONObject entries = manifestRoot.optJSONObject("entries");
        int appliedCount =
            applySplitTranslations(provider, entries, activeLanguageEntries);

        int entryCount = entries == null ? 0 : entries.length();

        Log.d(LOG_TAG, "Applied split i18n translations using language "
            + activeLanguage + " with " + entryCount
            + " entries and " + appliedCount + " applied values.");
        return;
      }

      if (translationsRoot != null) {
        String language = selectLegacyLanguage();
        JSONObject entries = translationsRoot.optJSONObject("entries");
        int appliedCount =
            applyLegacyTranslations(provider, entries, language);

        int entryCount = entries == null ? 0 : entries.length();

        Log.d(LOG_TAG, "Applied legacy i18n translations using language "
            + language + " with " + entryCount
            + " entries and " + appliedCount + " applied values.");
      }
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Unable to apply loaded i18n translations.", e);
    }
  }

  public String lookupDynamic(String key, Map<String, String> values) {
    if (key == null || key.length() == 0) {
      return "";
    }

    if (manifestRoot != null) {
      return lookupSplitDynamic(key, values);
    }

    return lookupLegacyDynamic(key, values);
  }

  public void setPreviewLanguageOverride(String language) {
    previewLanguageOverride =
        language == null ? "" : language.trim();
  }

  public void clearPreviewLanguageOverride() {
    previewLanguageOverride = "";
  }

  public void clear() {
    clearLoadedTranslations();
    previewLanguageOverride = "";
  }

  @VisibleForTesting
  void putTranslationsForTesting(JSONObject root) {
    clearLoadedTranslations();
    translationsRoot = root;
  }

  /**
   * Loads the split manifest and the best language file for the device.
   */
  private boolean loadSplitAssets(TranslationProvider provider) {
    final JSONObject manifest;

    try {
      manifest = new JSONObject(readAsset(provider, MANIFEST_ASSET));
    } catch (IOException e) {
      Log.d(LOG_TAG, "No split i18n manifest found.");
      return false;
    } catch (JSONException e) {
      Log.w(LOG_TAG,
          "Invalid split i18n manifest. Trying legacy translations.", e);
      return false;
    }

    int manifestVersion = manifest.optInt("version", -1);
    if (manifestVersion != MANIFEST_VERSION) {
      Log.w(LOG_TAG, "Unsupported split i18n manifest version "
          + manifestVersion + ". Trying legacy translations.");
      return false;
    }

    JSONObject languagePaths = manifest.optJSONObject("languages");
    JSONObject entries = manifest.optJSONObject("entries");

    if (languagePaths == null || entries == null) {
      Log.w(LOG_TAG,
          "Split i18n manifest is missing languages or entries. "
              + "Trying legacy translations.");
      return false;
    }

    String language = selectSplitLanguage(languagePaths);
    JSONObject languageEntries = null;

    if (language.length() > 0) {
      languageEntries =
          loadSplitLanguageEntries(provider, languagePaths, language);

      /*
       * If an exact regional file was listed but could not be loaded,
       * try its language-only equivalent.
       */
      String languageOnly = getLanguageOnly(language);

      if (languageEntries == null
          && languageOnly.length() > 0
          && !languageOnly.equals(language)
          && languagePaths.has(languageOnly)) {
        JSONObject fallbackEntries =
            loadSplitLanguageEntries(provider, languagePaths, languageOnly);

        if (fallbackEntries != null) {
          language = languageOnly;
          languageEntries = fallbackEntries;
        }
      }
    }

    /*
     * No supported device-language file is not an error. The runtime will
     * use base text from the manifest.
     */
    if (languageEntries == null) {
      languageEntries = new JSONObject();
      language = "";
    }

    translationsRoot = null;
    manifestRoot = manifest;
    activeLanguageEntries = languageEntries;
    activeLanguage = language;

    try {
      int appliedCount =
          applySplitTranslations(provider, entries, activeLanguageEntries);

      Log.d(LOG_TAG, "Loaded split i18n translations for locale "
          + getDeviceLanguageCode() + " using language "
          + (activeLanguage.length() == 0
              ? "base text" : activeLanguage)
          + " with " + entries.length()
          + " entries and " + appliedCount + " applied values.");
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Unable to apply split i18n translations.", e);
    }

    return true;
  }

  private JSONObject loadSplitLanguageEntries(TranslationProvider provider,
      JSONObject languagePaths, String language) {
    String relativePath =
        languagePaths.optString(language, "").trim();

    if (!isSafeLanguageAssetPath(relativePath)) {
      Log.w(LOG_TAG, "Invalid split i18n asset path for language "
          + language + ": " + relativePath);
      return null;
    }

    String assetPath = I18N_ASSET_PREFIX + relativePath;

    try {
      JSONObject languageRoot =
          new JSONObject(readAsset(provider, assetPath));

      int languageFileVersion =
          languageRoot.optInt("version", -1);

      if (languageFileVersion != LANGUAGE_FILE_VERSION) {
        Log.w(LOG_TAG, "Unsupported split i18n language file version "
            + languageFileVersion + " in " + assetPath + ".");
        return null;
      }

      String declaredLanguage =
          languageRoot.optString("language", "").trim();

      if (!language.equals(declaredLanguage)) {
        Log.w(LOG_TAG, "Split i18n file " + assetPath
            + " declares language " + declaredLanguage
            + " instead of " + language + ".");
        return null;
      }

      JSONObject entries =
          languageRoot.optJSONObject("entries");

      if (entries == null) {
        Log.w(LOG_TAG, "Split i18n file " + assetPath
            + " is missing entries.");
        return null;
      }

      return entries;
    } catch (IOException e) {
      Log.w(LOG_TAG,
          "Unable to open split i18n language asset "
              + assetPath + ".", e);
    } catch (JSONException e) {
      Log.w(LOG_TAG,
          "Invalid split i18n language JSON in "
              + assetPath + ".", e);
    }

    return null;
  }

  /**
   * Loads the old combined asset for compatibility.
   */
  private void loadLegacyAsset(TranslationProvider provider) {
    try {
      JSONObject root =
          new JSONObject(readAsset(provider, LEGACY_TRANSLATIONS_ASSET));

      clearLoadedTranslations();
      translationsRoot = root;

      String language = selectLegacyLanguage();
      JSONObject entries = root.optJSONObject("entries");
      int appliedCount =
          applyLegacyTranslations(provider, entries, language);

      int entryCount = entries == null ? 0 : entries.length();

      Log.d(LOG_TAG, "Loaded legacy i18n translations for locale "
          + getDeviceLanguageCode() + " using language "
          + language + " with " + entryCount
          + " entries and " + appliedCount + " applied values.");
    } catch (IOException e) {
      Log.d(LOG_TAG, "No legacy i18n translations asset found.");
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Invalid legacy i18n translations JSON.", e);
    }
  }

  private String lookupSplitDynamic(String key,
      Map<String, String> values) {
    JSONObject entries = manifestRoot.optJSONObject("entries");
    if (entries == null) {
      return "";
    }

    JSONObject entry = entries.optJSONObject(key);
    if (entry == null) {
      return "";
    }

    String template = "";

    if (activeLanguageEntries != null) {
      template = activeLanguageEntries.optString(key, "");
    }

    if (template.length() == 0) {
      template = getEntryBaseText(entry);
    }

    return I18nFormatter.format(template, values);
  }

  private String lookupLegacyDynamic(String key,
      Map<String, String> values) {
    if (translationsRoot == null) {
      return "";
    }

    String language = selectLegacyLanguage();
    JSONObject entries =
        translationsRoot.optJSONObject("entries");

    if (entries == null) {
      return "";
    }

    JSONObject entry = entries.optJSONObject(key);
    if (entry == null) {
      return "";
    }

    JSONObject translations =
        entry.optJSONObject("translations");

    String template = "";

    if (translations != null) {
      template = translations.optString(language, "");
    }

    if (template.length() == 0) {
      template = getEntryBaseText(entry);
    }

    return I18nFormatter.format(template, values);
  }

  private int applySplitTranslations(TranslationProvider provider,
      JSONObject entries, JSONObject languageEntries)
      throws JSONException {
    if (entries == null) {
      return 0;
    }

    int appliedCount = 0;
    Iterator<String> keys = entries.keys();

    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject entry = entries.optJSONObject(key);

      if (entry == null) {
        continue;
      }

      JSONObject source = entry.optJSONObject("source");

      // Dynamic entries do not have component source metadata.
      if (source == null) {
        continue;
      }

      String screenName = source.optString("screen", "");

      if (screenName.length() > 0
          && !screenName.equals(provider.getFormName())) {
        continue;
      }

      String componentName =
          source.optString("component", "");
      String propertyName =
          source.optString("property", "");

      String translatedValue = "";

      if (languageEntries != null) {
        translatedValue =
            languageEntries.optString(key, "");
      }

      /*
       * Missing translations use the original Designer text stored in the
       * manifest.
       */
      if (translatedValue.length() == 0) {
        translatedValue = getEntryBaseText(entry);
      }

      if (translatedValue.length() == 0) {
        continue;
      }

      Object component =
          provider.lookupComponent(componentName);

      if (component == null) {
        Log.d(LOG_TAG, "No component found for i18n entry "
            + key + " component " + componentName);
        continue;
      }

      if (applyStringProperty(
          component, propertyName, translatedValue)) {
        appliedCount++;
      }
    }

    return appliedCount;
  }

  private int applyLegacyTranslations(TranslationProvider provider,
      JSONObject entries, String language)
      throws JSONException {
    if (entries == null
        || language == null
        || language.length() == 0) {
      return 0;
    }

    int appliedCount = 0;
    Iterator<String> keys = entries.keys();

    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject entry = entries.optJSONObject(key);

      if (entry == null) {
        continue;
      }

      JSONObject source = entry.optJSONObject("source");

      if (source == null) {
        continue;
      }

      String screenName = source.optString("screen", "");

      if (screenName.length() > 0
          && !screenName.equals(provider.getFormName())) {
        continue;
      }

      String componentName =
          source.optString("component", "");
      String propertyName =
          source.optString("property", "");

      JSONObject translations =
          entry.optJSONObject("translations");

      String translatedValue =
          getLegacyStaticValue(entry, translations, language);

      if (translatedValue.length() == 0) {
        continue;
      }

      Object component =
          provider.lookupComponent(componentName);

      if (component == null) {
        Log.d(LOG_TAG, "No component found for i18n entry "
            + key + " component " + componentName);
        continue;
      }

      if (applyStringProperty(
          component, propertyName, translatedValue)) {
        appliedCount++;
      }
    }

    return appliedCount;
  }

  public static void setPreviewLanguageForCompanion(
      String language, String translationsJson) {
    Form form = Form.getActiveForm();

    if (form == null) {
      return;
    }

    I18nTranslationManager manager =
        form.getI18nTranslationManager();

    manager.setPreviewLanguageOverride(language);

    if (translationsJson != null
        && translationsJson.trim().length() > 0) {
      manager.loadFromJson(translationsJson, form);
    } else {
      manager.applyLoadedTranslations(form);
    }
  }

  private String getLegacyStaticValue(JSONObject entry,
      JSONObject translations, String language) {
    if (translations != null) {
      String translatedValue =
          translations.optString(language, "");

      if (translatedValue.length() > 0) {
        return translatedValue;
      }
    }

    return getEntryBaseText(entry);
  }

  private static String getEntryBaseText(JSONObject entry) {
    String baseText = entry.optString("baseText", "");

    if (baseText.length() > 0) {
      return baseText;
    }

    JSONObject source = entry.optJSONObject("source");

    return source == null
        ? "" : source.optString("baseText", "");
  }

  private boolean applyStringProperty(Object component,
      String propertyName, String translatedValue) {
    if (propertyName == null
        || propertyName.length() == 0) {
      return false;
    }

    try {
      Method setter =
          component.getClass().getMethod(
              propertyName, String.class);

      setter.invoke(component, translatedValue);
      return true;
    } catch (Exception e) {
      Log.d(LOG_TAG,
          "Unable to apply translated property "
              + propertyName + " on "
              + component.getClass().getName(), e);
      return false;
    }
  }

  /**
   * Selects the best available split language based on device language.
   *
   * <p>The project base language is not forced as the active language.</p>
   */
  private String selectSplitLanguage(JSONObject languages) {
    if (previewLanguageOverride.length() > 0) {
      String previewLanguage =
          resolveAvailableLanguage(
              languages, previewLanguageOverride);

      if (previewLanguage.length() > 0) {
        return previewLanguage;
      }
    }

    return resolveAvailableLanguage(
        languages, getDeviceLanguageCode());
  }

  private String selectLegacyLanguage() {
    if (previewLanguageOverride.length() > 0) {
      return previewLanguageOverride;
    }

    String deviceLanguage = getDeviceLanguageCode();
    JSONObject entries =
        translationsRoot == null
            ? null
            : translationsRoot.optJSONObject("entries");

    if (legacyContainsLanguage(entries, deviceLanguage)) {
      return deviceLanguage;
    }

    String languageOnly =
        getLanguageOnly(deviceLanguage);

    if (legacyContainsLanguage(entries, languageOnly)) {
      return languageOnly;
    }

    /*
     * Return the device language even when unavailable. Individual lookups
     * will then fall back to their original base text.
     */
    return deviceLanguage;
  }

  private static boolean legacyContainsLanguage(
      JSONObject entries, String language) {
    if (entries == null
        || language == null
        || language.length() == 0) {
      return false;
    }

    Iterator<String> keys = entries.keys();

    while (keys.hasNext()) {
      JSONObject entry =
          entries.optJSONObject(keys.next());

      if (entry == null) {
        continue;
      }

      JSONObject translations =
          entry.optJSONObject("translations");

      if (translations != null
          && translations.has(language)) {
        return true;
      }
    }

    return false;
  }

  private static String resolveAvailableLanguage(
      JSONObject languages, String requestedLanguage) {
    if (languages == null || requestedLanguage == null) {
      return "";
    }

    String normalized =
        requestedLanguage.trim().replace('_', '-');

    if (normalized.length() == 0) {
      return "";
    }

    if (languages.has(normalized)) {
      return normalized;
    }

    String languageOnly =
        getLanguageOnly(normalized);

    if (languageOnly.length() > 0
        && languages.has(languageOnly)) {
      return languageOnly;
    }

    return "";
  }

  private static String getLanguageOnly(String language) {
    if (language == null) {
      return "";
    }

    int separator = language.indexOf('-');

    if (separator < 0) {
      separator = language.indexOf('_');
    }

    if (separator <= 0) {
      return language;
    }

    return language.substring(0, separator);
  }

  private static boolean isSafeLanguageAssetPath(
      String relativePath) {
    return relativePath != null
        && relativePath.length() > 0
        && !relativePath.startsWith("/")
        && !relativePath.contains("..")
        && !relativePath.contains("\\")
        && relativePath.endsWith(".json");
  }

  private void clearLoadedTranslations() {
    translationsRoot = null;
    manifestRoot = null;
    activeLanguageEntries = null;
    activeLanguage = "";
  }

  private static String getDeviceLanguageCode() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    String country = locale.getCountry();

    if (country == null || country.length() == 0) {
      return language;
    }

    return language + "-" + country;
  }

  private static String readAsset(TranslationProvider provider,
      String assetPath) throws IOException {
    InputStream inputStream = null;

    try {
      inputStream = provider.openAsset(assetPath);
      return readFully(inputStream);
    } finally {
      IOUtils.closeQuietly(LOG_TAG, inputStream);
    }
  }

  private static String readFully(InputStream inputStream)
      throws IOException {
    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream();

    byte[] buffer = new byte[4096];
    int count;
    while ((count = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, count);
    }

    return outputStream.toString("UTF-8");
  }
}
