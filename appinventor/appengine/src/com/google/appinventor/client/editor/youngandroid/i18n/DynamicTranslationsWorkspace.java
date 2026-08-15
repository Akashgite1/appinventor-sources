// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Workspace for creating and editing runtime translation messages.
 */
public final class DynamicTranslationsWorkspace extends Composite {
  public DynamicTranslationsWorkspace(TextBox dynamicKeyTextBox,
      TextBox dynamicBaseTextBox, TextBox dynamicPlaceholdersTextBox,
      Button addDynamicButton, FlexTable dynamicTranslationsTable) {
    FlowPanel workspace = new FlowPanel();
    workspace.setStylePrimaryName("ode-i18n-dynamic-workspace");

    Label workspaceTitle = new Label("Dynamic translations");
    workspaceTitle.setStylePrimaryName("ode-i18n-workspace-title");

    Label workspaceDescription = new Label(
        "Create reusable translated messages for text generated "
            + "while your app is running.");
    workspaceDescription.setStylePrimaryName(
        "ode-i18n-workspace-description");

    FlowPanel dynamicTranslationForm = new FlowPanel();
    dynamicTranslationForm.setStylePrimaryName(
        "ode-i18n-dynamic-form");

    FlowPanel dynamicKeyField =
        createLabeledField("Key", dynamicKeyTextBox);
    FlowPanel dynamicBaseTextField =
        createLabeledField("Base text", dynamicBaseTextBox);
    FlowPanel dynamicPlaceholdersField =
        createLabeledField("Placeholders", dynamicPlaceholdersTextBox);

    dynamicTranslationForm.add(dynamicKeyField);
    dynamicTranslationForm.add(dynamicBaseTextField);
    dynamicTranslationForm.add(dynamicPlaceholdersField);
    dynamicTranslationForm.add(addDynamicButton);

    dynamicTranslationsTable.setStylePrimaryName(
        "ode-i18n-dynamic-table");
    dynamicTranslationsTable.setWidth("100%");

    workspace.add(workspaceTitle);
    workspace.add(workspaceDescription);
    workspace.add(dynamicTranslationForm);
    workspace.add(dynamicTranslationsTable);

    initWidget(workspace);
  }

  private FlowPanel createLabeledField(
      String labelText, TextBox textBox) {
    FlowPanel field = new FlowPanel();
    field.setStylePrimaryName("ode-i18n-dynamic-form-field");

    Label fieldLabel = new Label(labelText);
    fieldLabel.setStylePrimaryName("ode-i18n-field-label");

    field.add(fieldLabel);
    field.add(textBox);

    return field;
  }
}
