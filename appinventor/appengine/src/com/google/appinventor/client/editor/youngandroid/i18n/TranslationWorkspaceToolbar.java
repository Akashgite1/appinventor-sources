// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Toolbar for switching between static and dynamic translation workspaces.
 */
public final class TranslationWorkspaceToolbar extends Composite {
  private final Button staticTranslationsButton;
  private final Button dynamicTranslationsButton;
  private final Label selectedLanguageLabel;

  public TranslationWorkspaceToolbar() {
    FlowPanel toolbar = new FlowPanel();
    toolbar.setStylePrimaryName("ode-i18n-workspace-toolbar");

    FlowPanel workspaceSwitchControls = new FlowPanel();
    workspaceSwitchControls.setStylePrimaryName("ode-i18n-workspace-switch");

    staticTranslationsButton = new Button("Static translations");
    staticTranslationsButton.addStyleName("ode-i18n-workspace-switch-button");

    dynamicTranslationsButton = new Button("Dynamic translations");
    dynamicTranslationsButton.addStyleName("ode-i18n-workspace-switch-button");

    selectedLanguageLabel = new Label();
    selectedLanguageLabel.setStylePrimaryName("ode-i18n-selected-language");

    workspaceSwitchControls.add(staticTranslationsButton);
    workspaceSwitchControls.add(dynamicTranslationsButton);

    toolbar.add(workspaceSwitchControls);
    toolbar.add(selectedLanguageLabel);

    initWidget(toolbar);
  }

  public void addStaticTranslationsClickHandler(ClickHandler clickHandler) {
    staticTranslationsButton.addClickHandler(clickHandler);
  }

  public void addDynamicTranslationsClickHandler(ClickHandler clickHandler) {
    dynamicTranslationsButton.addClickHandler(clickHandler);
  }

  public void setSelectedLanguage(String languageCode) {
    selectedLanguageLabel.setText(languageCode == null ? "" : languageCode);
  }

  public void setStaticTranslationsActive(boolean active) {
    setWorkspaceButtonActive(staticTranslationsButton, active);
  }

  public void setDynamicTranslationsActive(boolean active) {
    setWorkspaceButtonActive(dynamicTranslationsButton, active);
  }

  private void setWorkspaceButtonActive(Button button, boolean active) {
    if (active) {
      button.addStyleName("ode-i18n-workspace-switch-button-active");
    } else {
      button.removeStyleName("ode-i18n-workspace-switch-button-active");
    }
  }
}
