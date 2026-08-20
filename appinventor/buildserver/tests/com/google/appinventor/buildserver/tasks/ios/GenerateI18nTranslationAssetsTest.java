// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GenerateI18nTranslationAssetsTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void writesTranslationAssetsIntoProjectAssets()
      throws Exception {
    File assetsDirectory = temporaryFolder.newFolder("assets");

    JSONObject root = new JSONObject();
    root.put("baseLanguage", "en");
    root.put("languages", new JSONArray().put("hi"));
    root.put("entries", new JSONObject());

    GenerateI18nTranslationAssets.writeTranslationAssets(
        root.toString(), assetsDirectory);

    File manifestFile = new File(assetsDirectory, "i18n/manifest.json");
    File englishFile = new File(assetsDirectory, "i18n/languages/en.json");
    File hindiFile = new File(assetsDirectory, "i18n/languages/hi.json");

    assertTrue(manifestFile.isFile());
    assertTrue(englishFile.isFile());
    assertTrue(hindiFile.isFile());

    JSONObject manifest = new JSONObject(
        Files.toString(manifestFile, StandardCharsets.UTF_8));

    assertEquals(2, manifest.getInt("version"));
    assertEquals("en", manifest.getString("baseLanguage"));
    assertEquals("languages/hi.json",
      manifest.getJSONObject("languages").getString("hi"));
  }

  @Test
  public void skipsEmptyTranslationConfiguration()
      throws Exception {
    File assetsDirectory = new File(temporaryFolder.getRoot(), "assets");

    GenerateI18nTranslationAssets.writeTranslationAssets("", assetsDirectory);
    assertFalse(assetsDirectory.exists());
  }
}
