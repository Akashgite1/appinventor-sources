// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;

/**
 * Sidebar that arranges the existing language and export controls.
 */
public final class TranslationLanguageSidebar extends Composite {
  private static final String CUSTOM_LANGUAGE_VALUE = "__custom__";

  private final Label baseLanguageValueLabel;
  private final ListBox currentLanguageListBox;
  private final ListBox availableLanguageListBox;
  private final FlowPanel customLanguageSection;
  private final TextBox customLanguageCodeTextBox;
  private final Label validationMessageLabel;
  private final Button addLanguageButton;
  private final Button deleteLanguageButton;
  private final Button exportButton;

  public TranslationLanguageSidebar() {
    FlowPanel sidebar = new FlowPanel();
    sidebar.setStylePrimaryName("ode-i18n-language-sidebar");

    FlowPanel languageCard = new FlowPanel();
    languageCard.setStylePrimaryName("ode-i18n-sidebar-card");

    Label languageTitle = new Label("Languages");
    languageTitle.setStylePrimaryName("ode-i18n-sidebar-title");

    Label languageDescription = new Label(
        "Manage the languages used by your app and its translations.");
    languageDescription.setStylePrimaryName("ode-i18n-sidebar-description");

    FlowPanel baseLanguageSection = new FlowPanel();
    baseLanguageSection.setStylePrimaryName("ode-i18n-sidebar-section");

    Label baseLanguageLabel = new Label("Base language");
    baseLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    baseLanguageValueLabel = new Label("Not selected");
    baseLanguageValueLabel.setStylePrimaryName(
        "ode-i18n-base-language-value");

    baseLanguageSection.add(baseLanguageLabel);
    baseLanguageSection.add(baseLanguageValueLabel);

    FlowPanel currentLanguageSection = new FlowPanel();
    currentLanguageSection.setStylePrimaryName("ode-i18n-sidebar-section");

    Label currentLanguageLabel = new Label("Current translation language");
    currentLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    currentLanguageListBox = new ListBox();
    currentLanguageListBox.setVisibleItemCount(1);
    currentLanguageListBox.setStylePrimaryName("ode-i18n-language-selector");

    deleteLanguageButton = new Button("Delete Language");
deleteLanguageButton.addStyleName("ode-i18n-delete-language-button");
    deleteLanguageButton.setEnabled(false);

    currentLanguageSection.add(currentLanguageLabel);
    currentLanguageSection.add(currentLanguageListBox);
    currentLanguageSection.add(deleteLanguageButton);

    FlowPanel addLanguageSection = new FlowPanel();
    addLanguageSection.setStylePrimaryName("ode-i18n-sidebar-section");

    Label addLanguageLabel = new Label("Add translation language");
    addLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    availableLanguageListBox = new ListBox();
    availableLanguageListBox.setVisibleItemCount(1);
    availableLanguageListBox.setStylePrimaryName(
        "ode-i18n-available-language-selector");

    customLanguageSection = new FlowPanel();
    customLanguageSection.setStylePrimaryName("ode-i18n-custom-language-section");

    Label customLanguageLabel = new Label("Custom language code");
    customLanguageLabel.setStylePrimaryName("ode-i18n-field-label");

    customLanguageCodeTextBox = new TextBox();
    customLanguageCodeTextBox.setStylePrimaryName(
        "ode-i18n-custom-language-input");
    customLanguageCodeTextBox.getElement().setPropertyString(
        "placeholder", "e.g., mr, pt-BR, zh-Hans, or es-419");

    Label customLanguageGuidance = new Label(
        "Use a BCP 47 language tag and verify platform support before "
            + "building your app.");
    customLanguageGuidance.setStylePrimaryName("ode-i18n-custom-language-guidance");

    FlowPanel platformGuides = new FlowPanel();
    platformGuides.setStylePrimaryName("ode-i18n-custom-language-guides");

    platformGuides.add(createExternalGuideLink(
        "Android guide",
        "https://developer.android.com/guide/topics/resources/"
            + "providing-resources"));
    platformGuides.add(createExternalGuideLink(
        "iOS guide",
        "https://developer.apple.com/library/archive/documentation/"
            + "MacOSX/Conceptual/BPInternational/LanguageandLocaleIDs/"
            + "LanguageandLocaleIDs.html"));

    customLanguageSection.add(customLanguageLabel);
    customLanguageSection.add(customLanguageCodeTextBox);
    customLanguageSection.add(customLanguageGuidance);
    customLanguageSection.add(platformGuides);
    customLanguageSection.setVisible(false);

    addLanguageButton = new Button("Add Language");
    addLanguageButton.addStyleName("ode-i18n-add-language-button");
    addLanguageButton.setEnabled(false);

    validationMessageLabel = new Label();
    validationMessageLabel.setStylePrimaryName(
        "ode-i18n-language-validation-message");
    validationMessageLabel.setVisible(false);

    addLanguageSection.add(addLanguageLabel);
    addLanguageSection.add(availableLanguageListBox);
    addLanguageSection.add(customLanguageSection);
    addLanguageSection.add(validationMessageLabel);
    addLanguageSection.add(addLanguageButton);

    languageCard.add(languageTitle);
    languageCard.add(languageDescription);
    languageCard.add(baseLanguageSection);
    languageCard.add(currentLanguageSection);
    languageCard.add(addLanguageSection);

    FlowPanel exportCard = new FlowPanel();
    exportCard.setStylePrimaryName("ode-i18n-sidebar-card");

    Label exportTitle = new Label("Export");
    exportTitle.setStylePrimaryName("ode-i18n-sidebar-title");

    Label exportDescription = new Label("Export all translations as JSON.");
    exportDescription.setStylePrimaryName("ode-i18n-sidebar-description");

    exportButton = new Button("Export JSON");
    exportButton.addStyleName("ode-i18n-export-button");

    exportCard.add(exportTitle);
    exportCard.add(exportDescription);
    exportCard.add(exportButton);

    sidebar.add(languageCard);
    sidebar.add(exportCard);

    availableLanguageListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateAvailableLanguageSelection();
      }
    });

    customLanguageCodeTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        clearValidationMessage();
        updateAddLanguageButtonState();
      }
    });

    initWidget(sidebar);
  }

  public void addCurrentLanguageChangeHandler(ChangeHandler changeHandler) {
    currentLanguageListBox.addChangeHandler(changeHandler);
  }

  public void addLanguageClickHandler(ClickHandler clickHandler) {
    addLanguageButton.addClickHandler(clickHandler);
  }

  public void addDeleteLanguageClickHandler(ClickHandler clickHandler) {
    deleteLanguageButton.addClickHandler(clickHandler);
  }

  public void addExportClickHandler(ClickHandler clickHandler) {
    exportButton.addClickHandler(clickHandler);
  }

  public String getSelectedCurrentLanguage() {
    int selectedIndex = currentLanguageListBox.getSelectedIndex();
    if (selectedIndex < 0 || !currentLanguageListBox.isEnabled()) {
      return "";
    }

    return currentLanguageListBox.getValue(selectedIndex);
  }

  public String getLanguageToAdd() {
    int selectedIndex = availableLanguageListBox.getSelectedIndex();
    if (selectedIndex < 0) {
      return "";
    }

    String selectedValue = availableLanguageListBox.getValue(selectedIndex);
    if (CUSTOM_LANGUAGE_VALUE.equals(selectedValue)) {
      selectedValue = customLanguageCodeTextBox.getValue();
    }

    return TranslationLanguageCatalog.normalizeLanguageCode(selectedValue);
  }

  public boolean isCustomLanguageSelected() {
    int selectedIndex = availableLanguageListBox.getSelectedIndex();
    return selectedIndex >= 0
        && CUSTOM_LANGUAGE_VALUE.equals(
            availableLanguageListBox.getValue(selectedIndex));
  }

  public void updateLanguages(String baseLanguage,
      List<String> translationLanguages, String selectedLanguage) {
    String baseLanguageLabel =
        TranslationLanguageCatalog.getDisplayLabel(baseLanguage);
    baseLanguageValueLabel.setText(
        baseLanguageLabel.length() == 0
            ? "Not selected"
            : baseLanguageLabel);

    refreshCurrentLanguageListBox(translationLanguages, selectedLanguage);
    refreshAvailableLanguageListBox(baseLanguage, translationLanguages);

    customLanguageCodeTextBox.setValue("");
    customLanguageSection.setVisible(false);
    clearValidationMessage();
    updateAddLanguageButtonState();
  }

  public void showValidationMessage(String message) {
    validationMessageLabel.setText(message == null ? "" : message);
    validationMessageLabel.setVisible(
        message != null && message.length() > 0);
  }

  private void refreshCurrentLanguageListBox(
      List<String> translationLanguages, String selectedLanguage) {
    currentLanguageListBox.clear();

    if (translationLanguages == null || translationLanguages.isEmpty()) {
      currentLanguageListBox.addItem(
          "No translation language selected", "");
      currentLanguageListBox.setEnabled(false);
      deleteLanguageButton.setEnabled(false);
      return;
    }

    int selectedIndex = 0;
    for (int i = 0; i < translationLanguages.size(); i++) {
      String language = translationLanguages.get(i);
      currentLanguageListBox.addItem(
          TranslationLanguageCatalog.getDisplayLabel(language),
          language);

      if (language.equals(selectedLanguage)) {
        selectedIndex = i;
      }
    }

    currentLanguageListBox.setSelectedIndex(selectedIndex);
    currentLanguageListBox.setEnabled(true);
    deleteLanguageButton.setEnabled(true);
  }

  private void refreshAvailableLanguageListBox(
      String baseLanguage, List<String> translationLanguages) {
    availableLanguageListBox.clear();
    availableLanguageListBox.addItem("Select a language...", "");

    for (TranslationLanguageCatalog.LanguageOption language :
        TranslationLanguageCatalog.getSuggestedLanguages()) {
      String languageCode = language.getCode();

      if (languageCode.equals(baseLanguage)
          || translationLanguages != null
            && translationLanguages.contains(languageCode)) {
        continue;
      }

      availableLanguageListBox.addItem(language.getDisplayLabel(), languageCode);
    }

    availableLanguageListBox.addItem("Other...", CUSTOM_LANGUAGE_VALUE);
    availableLanguageListBox.setSelectedIndex(0);
  }

  private void updateAvailableLanguageSelection() {
    clearValidationMessage();
    customLanguageSection.setVisible(isCustomLanguageSelected());
    updateAddLanguageButtonState();
  }

  private void updateAddLanguageButtonState() {
    int selectedIndex = availableLanguageListBox.getSelectedIndex();
    if (selectedIndex < 0) {
      addLanguageButton.setEnabled(false);
      return;
    }

    String selectedValue =
        availableLanguageListBox.getValue(selectedIndex);

    if (CUSTOM_LANGUAGE_VALUE.equals(selectedValue)) {
      String customLanguageCode = customLanguageCodeTextBox.getValue();
      addLanguageButton.setEnabled(
          customLanguageCode != null
              && customLanguageCode.trim().length() > 0);
    } else {
      addLanguageButton.setEnabled(selectedValue.length() > 0);
    }
  }

  private void clearValidationMessage() {
    validationMessageLabel.setText("");
    validationMessageLabel.setVisible(false);
  }

  private Anchor createExternalGuideLink(String text, String url) {
    Anchor guideLink = new Anchor(text, url);
    guideLink.setTarget("_blank");
    guideLink.getElement().setAttribute(
        "rel", "noopener noreferrer");
    guideLink.setStylePrimaryName(
        "ode-i18n-custom-language-guide-link");
    return guideLink;
  }
}
