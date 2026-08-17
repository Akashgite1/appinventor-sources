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

/**
 * First-time setup view for choosing the app's base language.
 */
public final class TranslationSetupPanel extends Composite {
  private static final String CUSTOM_LANGUAGE_VALUE = "__custom__";

  private final ListBox baseLanguageListBox;
  private final FlowPanel customLanguageSection;
  private final TextBox customLanguageCodeTextBox;
  private final Label validationMessageLabel;
  private final Button continueButton;

  public TranslationSetupPanel() {
    FlowPanel setupPanel = new FlowPanel();
    setupPanel.setStylePrimaryName("ode-i18n-setup-panel");

    FlowPanel setupCard = new FlowPanel();
    setupCard.setStylePrimaryName("ode-i18n-setup-card");

    FlowPanel setupForm = new FlowPanel();
    setupForm.setStylePrimaryName("ode-i18n-setup-form");

    Label setupTitle =
        new Label("Welcome to the App Inventor Translation Panel");
    setupTitle.setStylePrimaryName("ode-i18n-setup-title");

    Label setupDescription = new Label(
        "Choose the language used by your app's existing Designer text "
            + "and messages.");
    setupDescription.setStylePrimaryName("ode-i18n-setup-description");

    Label baseLanguageLabel = new Label("Base language");
    baseLanguageLabel.setStylePrimaryName("ode-i18n-setup-field-label");

    Label baseLanguageDescription = new Label(
        "This is the language currently used by your app's source text.");
    baseLanguageDescription.setStylePrimaryName("ode-i18n-setup-field-description");

    baseLanguageListBox = new ListBox();
    baseLanguageListBox.setStylePrimaryName("ode-i18n-setup-language-selector");
    populateBaseLanguageListBox();

    customLanguageSection = new FlowPanel();
    customLanguageSection.setStylePrimaryName("ode-i18n-setup-custom-language");

    Label customLanguageLabel = new Label("Custom language code");
    customLanguageLabel.setStylePrimaryName("ode-i18n-setup-field-label");

    customLanguageCodeTextBox = new TextBox();
    customLanguageCodeTextBox.setStylePrimaryName(
        "ode-i18n-setup-custom-language-input");
    customLanguageCodeTextBox.getElement().setPropertyString(
        "placeholder", "e.g., mr, pt-BR, zh-Hans, or es-419");

    Label customLanguageGuidance = new Label(
        "Use a BCP 47 language tag. Confirm that the language identifier "
            + "is supported by the platforms you intend to build for.");
    customLanguageGuidance.setStylePrimaryName(
        "ode-i18n-setup-custom-language-guidance");

    FlowPanel platformGuides = new FlowPanel();
    platformGuides.setStylePrimaryName("ode-i18n-setup-platform-guides");

    Anchor androidGuide = createExternalGuideLink(
        "Android language guide",
        "https://developer.android.com/guide/topics/resources/providing-resources");
    Anchor iosGuide = createExternalGuideLink(
        "iOS language guide",
        "https://developer.apple.com/library/archive/documentation/"
            + "MacOSX/Conceptual/BPInternational/LanguageandLocaleIDs/"
            + "LanguageandLocaleIDs.html");

    platformGuides.add(androidGuide);
    platformGuides.add(iosGuide);

    customLanguageSection.add(customLanguageLabel);
    customLanguageSection.add(customLanguageCodeTextBox);
    customLanguageSection.add(customLanguageGuidance);
    customLanguageSection.add(platformGuides);
    customLanguageSection.setVisible(false);

    validationMessageLabel = new Label();
    validationMessageLabel.setStylePrimaryName(
        "ode-i18n-setup-validation-message");
    validationMessageLabel.setVisible(false);

    continueButton = new Button("Continue");
    continueButton.addStyleName("ode-i18n-setup-continue-button");
    continueButton.setEnabled(false);

    setupForm.add(setupTitle);
    setupForm.add(setupDescription);
    setupForm.add(baseLanguageLabel);
    setupForm.add(baseLanguageDescription);
    setupForm.add(baseLanguageListBox);
    setupForm.add(customLanguageSection);
    setupForm.add(validationMessageLabel);
    setupForm.add(continueButton);

    FlowPanel setupGuide = new FlowPanel();
    setupGuide.setStylePrimaryName("ode-i18n-setup-guide");

    Label setupGuideTitle = new Label("How it works");
    setupGuideTitle.setStylePrimaryName("ode-i18n-setup-guide-title");

    setupGuide.add(setupGuideTitle);
    setupGuide.add(createSetupStep(
        "1",
        "Choose your base language",
        "Identify the language used by your app's existing text."
    ));
    setupGuide.add(createSetupStep(
        "2",
        "Add translation languages",
        "Choose the languages you want to translate into from the sidebar."
    ));
    setupGuide.add(createSetupStep(
        "3",
        "Translate and test",
        "Review your translated text on Android and iOS devices."
    ));

    setupCard.add(setupForm);
    setupCard.add(setupGuide);
    setupPanel.add(setupCard);

    baseLanguageListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateLanguageSelectionState();
      }
    });

    customLanguageCodeTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        clearValidationMessage();
        updateContinueButtonState();
      }
    });

    initWidget(setupPanel);
  }

  public void addContinueClickHandler(ClickHandler clickHandler) {
    continueButton.addClickHandler(clickHandler);
  }

  public String getSelectedBaseLanguageCode() {
    int selectedIndex = baseLanguageListBox.getSelectedIndex();
    if (selectedIndex < 0) {
      return "";
    }

    String selectedValue = baseLanguageListBox.getValue(selectedIndex);
    if (CUSTOM_LANGUAGE_VALUE.equals(selectedValue)) {
      selectedValue = customLanguageCodeTextBox.getValue();
    }

    return TranslationLanguageCatalog.normalizeLanguageCode(selectedValue);
  }

  public boolean isCustomLanguageSelected() {
    int selectedIndex = baseLanguageListBox.getSelectedIndex();
    return selectedIndex >= 0
        && CUSTOM_LANGUAGE_VALUE.equals(
            baseLanguageListBox.getValue(selectedIndex));
  }

  public void showValidationMessage(String message) {
    validationMessageLabel.setText(message == null ? "" : message);
    validationMessageLabel.setVisible(
        message != null && message.length() > 0);
  }

  private void populateBaseLanguageListBox() {
    baseLanguageListBox.addItem("Select a language...", "");

    for (TranslationLanguageCatalog.LanguageOption language :
        TranslationLanguageCatalog.getSuggestedLanguages()) {
      baseLanguageListBox.addItem(
          language.getDisplayLabel(), language.getCode());
    }

    baseLanguageListBox.addItem("Other...", CUSTOM_LANGUAGE_VALUE);
  }

  private void updateLanguageSelectionState() {
    clearValidationMessage();
    customLanguageSection.setVisible(isCustomLanguageSelected());
    updateContinueButtonState();
  }

  private void updateContinueButtonState() {
    int selectedIndex = baseLanguageListBox.getSelectedIndex();
    if (selectedIndex < 0) {
      continueButton.setEnabled(false);
      return;
    }

    String selectedValue = baseLanguageListBox.getValue(selectedIndex);
    if (CUSTOM_LANGUAGE_VALUE.equals(selectedValue)) {
      String customLanguageCode = customLanguageCodeTextBox.getValue();
      continueButton.setEnabled(
          customLanguageCode != null
              && customLanguageCode.trim().length() > 0);
    } else {
      continueButton.setEnabled(selectedValue.length() > 0);
    }
  }

  private void clearValidationMessage() {
    validationMessageLabel.setText("");
    validationMessageLabel.setVisible(false);
  }

  private FlowPanel createSetupStep(String number, String title, String description) {
    FlowPanel setupStep = new FlowPanel();
    setupStep.setStylePrimaryName("ode-i18n-setup-step");

    Label stepNumber = new Label(number);
    stepNumber.setStylePrimaryName("ode-i18n-setup-step-number");

    FlowPanel stepContent = new FlowPanel();
    stepContent.setStylePrimaryName("ode-i18n-setup-step-content");

    Label stepTitle = new Label(title);
    stepTitle.setStylePrimaryName("ode-i18n-setup-step-title");

    Label stepDescription = new Label(description);
    stepDescription.setStylePrimaryName(
        "ode-i18n-setup-step-description");

    stepContent.add(stepTitle);
    stepContent.add(stepDescription);

    setupStep.add(stepNumber);
    setupStep.add(stepContent);

    return setupStep;
  }

  private Anchor createExternalGuideLink(String text, String url) {
    Anchor guideLink = new Anchor(text, url);
    guideLink.setTarget("_blank");
    guideLink.getElement().setAttribute("rel", "noopener noreferrer");
    guideLink.setStylePrimaryName("ode-i18n-setup-guide-link");
    return guideLink;
  }
}