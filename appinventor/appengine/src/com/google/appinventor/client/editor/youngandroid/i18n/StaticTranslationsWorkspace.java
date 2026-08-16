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
  private final Label paginationSummaryLabel;
  private final Button previousPageButton;
  private final Label pageNumberLabel;
  private final Button nextPageButton;

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

    FlowPanel paginationControls = new FlowPanel();
    paginationControls.setStylePrimaryName("ode-i18n-pagination");

    paginationSummaryLabel = new Label();
    paginationSummaryLabel.setStylePrimaryName("ode-i18n-pagination-summary");

    FlowPanel paginationNavigation = new FlowPanel();
    paginationNavigation.setStylePrimaryName("ode-i18n-pagination-navigation");

    previousPageButton = new Button("Previous");
    previousPageButton.setStylePrimaryName("ode-i18n-pagination-button");

    pageNumberLabel = new Label();
    pageNumberLabel.setStylePrimaryName("ode-i18n-pagination-page-number");

    nextPageButton = new Button("Next");
    nextPageButton.setStylePrimaryName("ode-i18n-pagination-button");

    paginationNavigation.add(previousPageButton);
    paginationNavigation.add(pageNumberLabel);
    paginationNavigation.add(nextPageButton);

    paginationControls.add(paginationSummaryLabel);
    paginationControls.add(paginationNavigation);

    translationsTable.setStylePrimaryName("ode-i18n-table");
    translationsTable.setWidth("100%");

    workspace.add(workspaceTitle);
    workspace.add(workspaceDescription);
    workspace.add(translationSearchControls);
    workspace.add(translationsTable);
    workspace.add(paginationControls);

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

  public void addPreviousPageClickHandler(ClickHandler clickHandler) {
    previousPageButton.addClickHandler(clickHandler);
  }

  public void addNextPageClickHandler(ClickHandler clickHandler) {
    nextPageButton.addClickHandler(clickHandler);
  }

  public void updatePagination(int firstVisibleEntry, int lastVisibleEntry,
      int totalEntryCount, int currentPage, int totalPages) {
    paginationSummaryLabel.setText(
        "Showing " + firstVisibleEntry + " to " + lastVisibleEntry
            + " of " + totalEntryCount + " entries");
    pageNumberLabel.setText("Page " + currentPage + " of " + totalPages);
    previousPageButton.setEnabled(currentPage > 1);
    nextPageButton.setEnabled(currentPage < totalPages);
  }
}
