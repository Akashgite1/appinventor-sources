// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Workspace containing translations generated from Designer properties.
 */
public final class StaticTranslationsWorkspace extends Composite {
  private final TextBox translationSearchTextBox;
  private final Button searchButton;

  public StaticTranslationsWorkspace(FlexTable translationsTable) {
    FlowPanel workspace = new FlowPanel();
    workspace.setStylePrimaryName("ode-i18n-static-workspace");

    Label workspaceTitle = new Label("App translations");
    workspaceTitle.setStylePrimaryName("ode-i18n-workspace-title");

    Label workspaceDescription = new Label("Translate text from the components in your app.");
    workspaceDescription.setStylePrimaryName("ode-i18n-workspace-description");

    FlowPanel translationSearchControls = new FlowPanel();
    translationSearchControls.setStylePrimaryName("ode-i18n-static-search-controls");

    translationSearchTextBox = new TextBox();
    translationSearchTextBox.setStylePrimaryName("ode-i18n-search-input");
    translationSearchTextBox.getElement().setPropertyString("placeholder",
        "Search screen, component, property, or text");

    searchButton = new Button("Search");
    searchButton.setStylePrimaryName("ode-i18n-search-button");

    translationSearchControls.add(translationSearchTextBox);
    translationSearchControls.add(searchButton);
    translationsTable.setStylePrimaryName("ode-i18n-table");
    translationsTable.setWidth("100%");

    workspace.add(workspaceTitle);
    workspace.add(workspaceDescription);
    workspace.add(translationSearchControls);
    workspace.add(translationsTable);

    initWidget(workspace);
  }

  public void addSearchKeyUpHandler(KeyUpHandler searchKeyUpHandler) {
    translationSearchTextBox.addKeyUpHandler(searchKeyUpHandler);
  }

  public void addSearchClickHandler(ClickHandler searchClickHandler) {
    searchButton.addClickHandler(searchClickHandler);
  }

  public String getSearchQuery() {
    String searchQuery = translationSearchTextBox.getValue();
    return searchQuery == null ? "" : searchQuery.trim();
  }
}
