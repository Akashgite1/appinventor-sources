// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Sidebar that arranges the existing language and export controls.
 */
public final class TranslationLanguageSidebar extends Composite {
  public TranslationLanguageSidebar(ListBox languageListBox,
      TextBox languageTextBox, Button addLanguageButton,
      Button deleteLanguageButton, Button exportButton) {
    FlowPanel sidebar = new FlowPanel();
    sidebar.setStylePrimaryName("ode-i18n-language-sidebar");

    FlowPanel languageCard = new FlowPanel();
    languageCard.setStylePrimaryName("ode-i18n-sidebar-card");

    Label languageTitle = new Label("Languages");
    languageTitle.setStylePrimaryName("ode-i18n-sidebar-title");

    Label languageDescription = new Label("Choose the language you are currently translating.");
    languageDescription.setStylePrimaryName("ode-i18n-sidebar-description");

    FlowPanel currentLanguageSection = new FlowPanel();
    currentLanguageSection.setStylePrimaryName("ode-i18n-sidebar-section");

    Label currentLanguageLabel = new Label("Current language");
    currentLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    languageListBox.setStylePrimaryName("ode-i18n-language-selector");
    deleteLanguageButton.addStyleName("ode-i18n-delete-language-button");

    currentLanguageSection.add(currentLanguageLabel);
    currentLanguageSection.add(languageListBox);
    currentLanguageSection.add(deleteLanguageButton);

    FlowPanel addLanguageSection = new FlowPanel();
    addLanguageSection.setStylePrimaryName("ode-i18n-sidebar-section");

    Label addLanguageLabel = new Label("Add language code");
    addLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    languageTextBox.setStylePrimaryName("ode-i18n-add-language-input");
    addLanguageButton.addStyleName("ode-i18n-add-language-button");

    addLanguageSection.add(addLanguageLabel);
    addLanguageSection.add(languageTextBox);
    addLanguageSection.add(addLanguageButton);

    languageCard.add(languageTitle);
    languageCard.add(languageDescription);
    languageCard.add(currentLanguageSection);
    languageCard.add(addLanguageSection);

    FlowPanel exportCard = new FlowPanel();
    exportCard.setStylePrimaryName("ode-i18n-sidebar-card");

    Label exportTitle = new Label("Export");
    exportTitle.setStylePrimaryName("ode-i18n-sidebar-title");

    Label exportDescription = new Label("Export all translations as JSON.");
    exportDescription.setStylePrimaryName("ode-i18n-sidebar-description");

    exportButton.addStyleName("ode-i18n-export-button");

    exportCard.add(exportTitle);
    exportCard.add(exportDescription);
    exportCard.add(exportButton);

    sidebar.add(languageCard);
    sidebar.add(exportCard);

    initWidget(sidebar);
  }
}
