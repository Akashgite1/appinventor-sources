// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Prompts the user to add a target language after base-language setup.
 */
public final class TranslationWorkspaceEmptyState extends Composite {
  private final Label descriptionLabel;

  public TranslationWorkspaceEmptyState() {
    FlowPanel emptyState = new FlowPanel();
    emptyState.setStylePrimaryName("ode-i18n-workspace-empty-state");

    Label titleLabel = new Label("Add a translation language");
    titleLabel.setStylePrimaryName("ode-i18n-workspace-empty-state-title");

    descriptionLabel = new Label();
    descriptionLabel.setStylePrimaryName("ode-i18n-workspace-empty-state-description");

    Label instructionLabel = new Label(
        "Choose a language from the sidebar to begin translating.");
    instructionLabel.setStylePrimaryName("ode-i18n-workspace-empty-state-instruction");

    emptyState.add(titleLabel);
    emptyState.add(descriptionLabel);
    emptyState.add(instructionLabel);

    initWidget(emptyState);
  }

  public void setBaseLanguage(String baseLanguage) {
    String baseLanguageLabel =
        TranslationLanguageCatalog.getDisplayLabel(baseLanguage);

    if (baseLanguageLabel.length() == 0) {
      descriptionLabel.setText(
          "Your app does not have a translation language yet.");
    } else {
      descriptionLabel.setText(
          "Your app's base language is " + baseLanguageLabel + ".");
    }
  }
}