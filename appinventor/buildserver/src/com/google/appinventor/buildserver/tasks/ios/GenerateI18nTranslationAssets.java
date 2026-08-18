// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.I18nTranslationAssetGenerator;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.json.JSONException;

/**
 * Generates App Inventor translation assets for packaged iOS applications.
 */
@BuildType(ipa = true, asc = true)
public class GenerateI18nTranslationAssets implements IosTask {

  @Override
  public TaskResult execute(IosCompilerContext context) {
    String translationsJson = context.getProject().getI18nTranslations();

    try {
      writeTranslationAssets(translationsJson, context.getPaths().getAssetsDir());
      return TaskResult.generateSuccess();
    } catch (IOException | JSONException e) {
      context.getReporter().error(
        "Unable to generate i18n translation assets for iOS", true);
      return TaskResult.generateError(e);
    }
  }

  static void writeTranslationAssets(String translationsJson, File assetsDirectory)
      throws IOException, JSONException {
    if (translationsJson == null || translationsJson.trim().length() == 0) {
      return;
    }

    Map<String, String> generatedAssets =
        I18nTranslationAssetGenerator.generateTranslationAssets(translationsJson);

    for (Map.Entry<String, String> asset : generatedAssets.entrySet()) {
      File outputFile = new File(assetsDirectory, asset.getKey());

      Files.createParentDirs(outputFile);
      Files.write(asset.getValue().getBytes(StandardCharsets.UTF_8), outputFile);
    }
  }
}
