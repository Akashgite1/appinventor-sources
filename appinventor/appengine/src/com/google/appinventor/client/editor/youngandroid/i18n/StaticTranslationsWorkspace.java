// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Workspace containing translations generated from Designer properties.
 */
public final class StaticTranslationsWorkspace extends Composite {
  public StaticTranslationsWorkspace(FlexTable translationsTable) {
    FlowPanel workspace = new FlowPanel();
    workspace.setStylePrimaryName("ode-i18n-static-workspace");

    Label workspaceTitle = new Label("App translations");
    workspaceTitle.setStylePrimaryName("ode-i18n-workspace-title");

    Label workspaceDescription = new Label(
        "Translate text from the components in your app.");
    workspaceDescription.setStylePrimaryName(
        "ode-i18n-workspace-description");

    translationsTable.setStylePrimaryName("ode-i18n-table");
    translationsTable.setWidth("100%");

    workspace.add(workspaceTitle);
    workspace.add(workspaceDescription);
    workspace.add(translationsTable);

    initWidget(workspace);
  }
}
