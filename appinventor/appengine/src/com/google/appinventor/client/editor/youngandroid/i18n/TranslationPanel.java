// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class TranslationPanel extends Composite {

  /**
   * Identifies the translation workspace currently displayed.
   */
  private enum TranslationWorkspaceMode {
    STATIC_TRANSLATIONS,
    DYNAMIC_TRANSLATIONS
  }

  private static final int TRANSLATIONS_PER_PAGE = 20;
  private final YaProjectEditor projectEditor;
  private final FlexTable table;
  private final FlexTable dynamicTranslationsTable;
  private final Map<String, Map<String, String>> translationValues;
  private final Map<String, TranslationEntry> translationEntries;
  private final Map<String, DynamicTranslationEntry> dynamicTranslationEntries;
  private final List<String> languages;
  private String baseLanguage;
  private final TextBox dynamicKeyTextBox;
  private final TextBox dynamicBaseTextBox;
  private final TextBox dynamicPlaceholdersTextBox;
  private String selectedLanguage;
  private static final String LOCATOR_SEPARATOR = "\u0000";
  private final Map<String, String> locatorToTranslationKey;
  private final List<String> staticTranslationEntryOrder;
  private final TranslationWorkspaceToolbar translationWorkspaceToolbar;
  private final StaticTranslationsWorkspace staticTranslationsWorkspace;
  private final DynamicTranslationsWorkspace dynamicTranslationsWorkspace;
  private TranslationWorkspaceMode activeTranslationWorkspaceMode;
  private final TranslationSetupPanel translationSetupPanel;
  private final TranslationLanguageSidebar translationLanguageSidebar;
  private final TranslationWorkspaceEmptyState translationWorkspaceEmptyState;
  private final FlowPanel translationWorkspaceContent;
  private String staticTranslationsSearchQuery;
  private String dynamicTranslationsSearchQuery;
  private int staticTranslationsPageIndex;
  private int dynamicTranslationsPageIndex;

  private boolean savedTranslationsLoaded;

  private static final Logger LOG = Logger.getLogger(TranslationPanel.class.getName());

  public TranslationPanel(YaProjectEditor projectEditor) {
    this.projectEditor = projectEditor;
    this.table = new FlexTable();
    this.dynamicTranslationsTable = new FlexTable();
    this.translationValues = new HashMap<String, Map<String, String>>();
    this.translationEntries = new HashMap<String, TranslationEntry>();
    this.dynamicTranslationEntries = new HashMap<String, DynamicTranslationEntry>();
    this.savedTranslationsLoaded = false;
    this.languages = new ArrayList<String>();
    this.baseLanguage = null;
    this.selectedLanguage = null;
    this.dynamicKeyTextBox = new TextBox();
    this.dynamicBaseTextBox = new TextBox();
    this.dynamicPlaceholdersTextBox = new TextBox();
    this.locatorToTranslationKey = new HashMap<String, String>();
    this.staticTranslationEntryOrder = new ArrayList<String>();
    this.staticTranslationsSearchQuery = "";
    this.dynamicTranslationsSearchQuery = "";
    this.staticTranslationsPageIndex = 0;
    this.dynamicTranslationsPageIndex = 0;
    this.activeTranslationWorkspaceMode = TranslationWorkspaceMode.STATIC_TRANSLATIONS;

    FlowPanel root = new FlowPanel();
    root.setStylePrimaryName("ode-i18n-panel");
    root.setWidth("100%");
    root.setHeight("100%");

    FlowPanel translationPageHeader = new FlowPanel();
    translationPageHeader.setStylePrimaryName("ode-i18n-page-header");

    Label title = new Label("Translations");
    title.setStylePrimaryName("ode-i18n-title");

    Label description = new Label(
        "Translate your app's text into multiple languages. "
            + "Changes are saved automatically.");
    description.setStylePrimaryName("ode-i18n-page-description");

    translationPageHeader.add(title);
    translationPageHeader.add(description);

    dynamicKeyTextBox.setWidth("180px");
    dynamicKeyTextBox.getElement().setPropertyString("placeholder", "welcome_message");

    dynamicBaseTextBox.setWidth("320px");
    dynamicBaseTextBox.getElement().setPropertyString("placeholder", "Hello {name}");

    dynamicPlaceholdersTextBox.setWidth("180px");
    dynamicPlaceholdersTextBox.getElement().setPropertyString("placeholder", "name,count");

    Button addDynamicButton = new Button("Add Dynamic Key");
    addDynamicButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addDynamicTranslationEntry();
      }
    });

    translationWorkspaceToolbar = new TranslationWorkspaceToolbar();

    translationWorkspaceToolbar.addStaticTranslationsClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showStaticTranslationsWorkspace();
        }
    });

    translationWorkspaceToolbar.addDynamicTranslationsClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showDynamicTranslationsWorkspace();
        }
    });

    translationSetupPanel = new TranslationSetupPanel();
    translationSetupPanel.addContinueClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          completeTranslationSetup();
        }
    });

    translationLanguageSidebar = new TranslationLanguageSidebar();

    translationLanguageSidebar.addCurrentLanguageChangeHandler(
      new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          String language =
              translationLanguageSidebar.getSelectedCurrentLanguage();

          if (language.length() > 0
              && !language.equals(selectedLanguage)) {
            selectedLanguage = language;
            refresh();
          }
        }
    });

    translationLanguageSidebar.addLanguageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          addSelectedTranslationLanguage();
        }
    });

    translationLanguageSidebar.addDeleteLanguageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          deleteSelectedLanguage();
        }
    });

    translationLanguageSidebar.addExportClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showJsonDialog(
              "Export Translation JSON",
              "Copy or inspect the current i18n JSON below.",
              exportJson());
        }
    });

    staticTranslationsWorkspace = new StaticTranslationsWorkspace(table);

    staticTranslationsWorkspace.addSearchKeyUpHandler(
      new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          applyStaticTranslationsSearch();
        }
    });

    staticTranslationsWorkspace.addSearchClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          applyStaticTranslationsSearch();
        }
    });

    staticTranslationsWorkspace.addPreviousPageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showPreviousStaticTranslationsPage();
        }
    });

    staticTranslationsWorkspace.addNextPageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showNextStaticTranslationsPage();
        }
    });

    dynamicTranslationsWorkspace = new DynamicTranslationsWorkspace(
            dynamicKeyTextBox,
            dynamicBaseTextBox,
            dynamicPlaceholdersTextBox,
            addDynamicButton,
            dynamicTranslationsTable);

    dynamicTranslationsWorkspace.addSearchKeyUpHandler(
      new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          applyDynamicTranslationsSearch();
        }
    });

    dynamicTranslationsWorkspace.addSearchClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          applyDynamicTranslationsSearch();
        }
    });

    dynamicTranslationsWorkspace.addPreviousPageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showPreviousDynamicTranslationsPage();
        }
    });

    dynamicTranslationsWorkspace.addNextPageClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showNextDynamicTranslationsPage();
        }
    });

    translationWorkspaceEmptyState = new TranslationWorkspaceEmptyState();

    FlowPanel activeWorkspaceContainer = new FlowPanel();
    activeWorkspaceContainer.setStylePrimaryName("ode-i18n-active-workspace");
    activeWorkspaceContainer.add(staticTranslationsWorkspace);
    activeWorkspaceContainer.add(dynamicTranslationsWorkspace);
    activeWorkspaceContainer.add(translationWorkspaceEmptyState);

    FlowPanel workspaceLayout = new FlowPanel();
    workspaceLayout.setStylePrimaryName("ode-i18n-workspace-layout");
    workspaceLayout.add(translationLanguageSidebar);
    workspaceLayout.add(activeWorkspaceContainer);

    translationWorkspaceContent = new FlowPanel();
    translationWorkspaceContent.setStylePrimaryName("ode-i18n-workspace-content");
    translationWorkspaceContent.add(translationPageHeader);
    translationWorkspaceContent.add(translationWorkspaceToolbar);
    translationWorkspaceContent.add(workspaceLayout);

    root.add(translationSetupPanel);
    root.add(translationWorkspaceContent);

    updateTranslationView();

    initWidget(root);
  }

  /**
   * Displays translations generated from Designer properties.
   */
  private void showStaticTranslationsWorkspace() {
    activeTranslationWorkspaceMode = TranslationWorkspaceMode.STATIC_TRANSLATIONS;
    updateVisibleTranslationWorkspace();
  }

  /**
   * Displays translations created for runtime lookup.
   */
  private void showDynamicTranslationsWorkspace() {
    activeTranslationWorkspaceMode = TranslationWorkspaceMode.DYNAMIC_TRANSLATIONS;
    updateVisibleTranslationWorkspace();
  }

  /**
   * Synchronizes workspace visibility and toolbar presentation.
   */
  private void updateTranslationView() {
    boolean setupComplete = isTranslationSetupComplete();

    translationSetupPanel.setVisible(!setupComplete);
    translationWorkspaceContent.setVisible(setupComplete);

    if (!setupComplete) {
      return;
    }

    translationLanguageSidebar.updateLanguages(
        baseLanguage, languages, selectedLanguage);
    translationWorkspaceEmptyState.setBaseLanguage(baseLanguage);
    updateVisibleTranslationWorkspace();
  }

  private void updateVisibleTranslationWorkspace() {
    boolean hasTranslationLanguage = selectedLanguage != null
      && languages.contains(selectedLanguage);
    boolean staticTranslationsVisible = hasTranslationLanguage
      && activeTranslationWorkspaceMode
        == TranslationWorkspaceMode.STATIC_TRANSLATIONS;

    staticTranslationsWorkspace.setVisible(staticTranslationsVisible);
    dynamicTranslationsWorkspace.setVisible(
        hasTranslationLanguage && !staticTranslationsVisible);
    translationWorkspaceEmptyState.setVisible(!hasTranslationLanguage);

    translationWorkspaceToolbar.setStaticTranslationsActive(
        staticTranslationsVisible);
    translationWorkspaceToolbar.setDynamicTranslationsActive(
        hasTranslationLanguage && !staticTranslationsVisible);

    String displayedLanguage = hasTranslationLanguage
        ? "Translating to: "
            + TranslationLanguageCatalog.getDisplayLabel(selectedLanguage)
        : "Base: "
            + TranslationLanguageCatalog.getDisplayLabel(baseLanguage);
    translationWorkspaceToolbar.setSelectedLanguage(displayedLanguage);
  }

  private void completeTranslationSetup() {
    String selectedBaseLanguage =
        translationSetupPanel.getSelectedBaseLanguageCode();

    if (selectedBaseLanguage.length() == 0) {
      String validationMessage =
          translationSetupPanel.isCustomLanguageSelected()
              ? "Enter a valid BCP 47 language tag."
              : "Choose your app's base language.";
      translationSetupPanel.showValidationMessage(validationMessage);
      return;
    }

    baseLanguage = selectedBaseLanguage;
    refresh();
    updateTranslationsSetting();
  }

  private void addSelectedTranslationLanguage() {
    String language = translationLanguageSidebar.getLanguageToAdd();

    if (language.length() == 0) {
      String validationMessage =
          translationLanguageSidebar.isCustomLanguageSelected()
              ? "Enter a valid BCP 47 language tag."
              : "Choose a translation language.";
      translationLanguageSidebar.showValidationMessage(
          validationMessage);
      return;
    }

    if (language.equals(baseLanguage)) {
      translationLanguageSidebar.showValidationMessage(
          "The translation language must differ from the base language.");
      return;
    }

    if (languages.contains(language)) {
      translationLanguageSidebar.showValidationMessage(
          "This translation language has already been added.");
      return;
    }

    addLanguage(language, true);
    refresh();
    updateTranslationsSetting();
  }

  private void applyStaticTranslationsSearch() {
    staticTranslationsSearchQuery = staticTranslationsWorkspace.getSearchQuery();
    staticTranslationsPageIndex = 0;
    refreshStaticTranslationsTable();
  }

  private void applyDynamicTranslationsSearch() {
    dynamicTranslationsSearchQuery = dynamicTranslationsWorkspace.getSearchQuery();
    dynamicTranslationsPageIndex = 0;
    refreshDynamicTranslationsTable();
  }

  private void showPreviousStaticTranslationsPage() {
    if (staticTranslationsPageIndex > 0) {
      staticTranslationsPageIndex--;
      refreshStaticTranslationsTable();
    }
  }

  private void showNextStaticTranslationsPage() {
    int totalPages =
        getPaginationPageCount(getFilteredStaticTranslationKeys().size());

    if (staticTranslationsPageIndex + 1 < totalPages) {
      staticTranslationsPageIndex++;
      refreshStaticTranslationsTable();
    }
  }

  private void showPreviousDynamicTranslationsPage() {
    if (dynamicTranslationsPageIndex > 0) {
      dynamicTranslationsPageIndex--;
      refreshDynamicTranslationsTable();
    }
  }

  private void showNextDynamicTranslationsPage() {
    int totalPages =
        getPaginationPageCount(getFilteredDynamicTranslationKeys().size());

    if (dynamicTranslationsPageIndex + 1 < totalPages) {
      dynamicTranslationsPageIndex++;
      refreshDynamicTranslationsTable();
    }
  }

  public void refresh() {
    loadSavedTranslations();

    if (!isTranslationSetupComplete()) {
      updateTranslationView();
      return;
    }

    ensureSelectedLanguage();
    translationEntries.clear();
    staticTranslationEntryOrder.clear();

    List<String> formNames = projectEditor.getFormNames();

    for (String formName : formNames) {
      List<String> componentNames = projectEditor.getComponentInstances(formName);

      for (String componentName : componentNames) {
        String componentType = projectEditor.getComponentType(formName, componentName);

        for (String propertyName : projectEditor.getComponentPropertyNames(formName, componentName)) {
          if (!isTranslatableProperty(propertyName)) {
            continue;
          }

          String propertyValue = projectEditor.getComponentPropertyValue(formName, componentName,propertyName);
          String generatedKey = getOrCreateTranslationKey(formName, componentName, propertyName);
          translationEntries.put(generatedKey,
              new TranslationEntry(
                  generatedKey,
                  formName,
                  componentName,
                  componentType,
                  propertyName,
                  propertyValue
          ));

          staticTranslationEntryOrder.add(generatedKey);
        }
      }
    }

    if (selectedLanguage != null) {
      refreshStaticTranslationsTable();
      refreshDynamicTranslationsTable();
    }

    updateTranslationView();
  }

  private void refreshStaticTranslationsTable() {
    clearTable();
    addHeader();

    List<String> filteredTranslationKeys = getFilteredStaticTranslationKeys();
    int totalEntryCount = filteredTranslationKeys.size();
    int totalPages = getPaginationPageCount(totalEntryCount);

    staticTranslationsPageIndex =
        Math.min(staticTranslationsPageIndex, totalPages - 1);

    int firstEntryIndex =
        staticTranslationsPageIndex * TRANSLATIONS_PER_PAGE;
    int endEntryIndex =
        Math.min(firstEntryIndex + TRANSLATIONS_PER_PAGE, totalEntryCount);
    int tableRow = 1;

    for (int entryIndex = firstEntryIndex; entryIndex < endEntryIndex; entryIndex++) {
      String translationKey = filteredTranslationKeys.get(entryIndex);
      TranslationEntry translationEntry = translationEntries.get(translationKey);

      if (translationEntry == null) {
        continue;
      }

      table.setText(tableRow, 0, translationEntry.getScreenName());
      table.setText(tableRow, 1, translationEntry.getComponentName());
      table.setText(tableRow, 2, translationEntry.getComponentType());
      table.setText(tableRow, 3, translationEntry.getPropertyName());
      table.setText(tableRow, 4, translationEntry.getBaseText());
      table.setWidget(tableRow, 5,createTranslationTextBox(translationKey, selectedLanguage));

      tableRow++;
    }

    if (totalEntryCount == 0) {
      String emptyMessage = staticTranslationsSearchQuery.length() == 0
          ? "No translations found."
          : "No matching translations found.";

      table.setText(1, 0, emptyMessage);
      table.getFlexCellFormatter().setColSpan(1, 0, 6);
    }

    staticTranslationsWorkspace.updatePagination(
        totalEntryCount == 0 ? 0 : firstEntryIndex + 1,
        endEntryIndex,
        totalEntryCount,
        staticTranslationsPageIndex + 1,
        totalPages
    );
  }

  private List<String> getFilteredStaticTranslationKeys() {
    List<String> filteredTranslationKeys = new ArrayList<String>();
    String normalizedSearchQuery = staticTranslationsSearchQuery.toLowerCase();

    for (String translationKey : staticTranslationEntryOrder) {
      TranslationEntry translationEntry = translationEntries.get(translationKey);

      if (translationEntry == null) {
        continue;
      }

      if (normalizedSearchQuery.length() == 0
          || matchesStaticTranslationSearch(
              translationKey,
              translationEntry,
              normalizedSearchQuery)) {
        filteredTranslationKeys.add(translationKey);
      }
    }

    return filteredTranslationKeys;
  }

  private boolean matchesStaticTranslationSearch(String translationKey,
    TranslationEntry translationEntry, String normalizedSearchQuery) {
    return containsSearchText(translationEntry.getScreenName(),normalizedSearchQuery)
        || containsSearchText(translationEntry.getComponentName(),
            normalizedSearchQuery)
        || containsSearchText(translationEntry.getComponentType(),
            normalizedSearchQuery)
        || containsSearchText(translationEntry.getPropertyName(),
            normalizedSearchQuery)
        || containsSearchText(translationEntry.getBaseText(),
            normalizedSearchQuery)
        || containsSearchText(getTranslationValue(translationKey, selectedLanguage),
            normalizedSearchQuery);
  }

  /**
   * Rebuilds the table containing runtime translation entries.
   */
  private void refreshDynamicTranslationsTable() {
    clearDynamicTranslationsTable();

    dynamicTranslationsTable.setText(0, 0, "Key");
    dynamicTranslationsTable.setText(0, 1, "Base Text");
    dynamicTranslationsTable.setText(0, 2,
        TranslationLanguageCatalog.getDisplayLabel(selectedLanguage));
    dynamicTranslationsTable.setText(0, 3, "Actions");
    dynamicTranslationsTable.getRowFormatter().setStylePrimaryName(
        0, "ode-i18n-table-header");

    List<String> filteredDynamicTranslationKeys =
        getFilteredDynamicTranslationKeys();
    int totalEntryCount = filteredDynamicTranslationKeys.size();
    int totalPages = getPaginationPageCount(totalEntryCount);

    dynamicTranslationsPageIndex =
        Math.min(dynamicTranslationsPageIndex, totalPages - 1);

    int firstEntryIndex =
        dynamicTranslationsPageIndex * TRANSLATIONS_PER_PAGE;
    int endEntryIndex =
        Math.min(firstEntryIndex + TRANSLATIONS_PER_PAGE, totalEntryCount);
    int dynamicTranslationRow = 1;

    for (int entryIndex = firstEntryIndex;
        entryIndex < endEntryIndex; entryIndex++) {
      final String dynamicTranslationKey =
          filteredDynamicTranslationKeys.get(entryIndex);
      DynamicTranslationEntry dynamicTranslationEntry =
          dynamicTranslationEntries.get(dynamicTranslationKey);

      if (dynamicTranslationEntry == null) {
        continue;
      }

      Button deleteDynamicTranslationButton = new Button("Delete");
      deleteDynamicTranslationButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            deleteDynamicTranslationEntry(dynamicTranslationKey);
          }
      });

      dynamicTranslationsTable.setText(dynamicTranslationRow, 0, dynamicTranslationKey);
      dynamicTranslationsTable.setText(
          dynamicTranslationRow, 1, dynamicTranslationEntry.getBaseText());
      dynamicTranslationsTable.setWidget(dynamicTranslationRow, 2,
          createTranslationTextBox(dynamicTranslationKey, selectedLanguage));
      dynamicTranslationsTable.setWidget(
          dynamicTranslationRow, 3, deleteDynamicTranslationButton);

      dynamicTranslationRow++;
    }

    if (totalEntryCount == 0) {
      String emptyMessage = dynamicTranslationsSearchQuery.length() == 0
          ? "No dynamic translations found."
          : "No matching dynamic translations found.";

      dynamicTranslationsTable.setText(1, 0, emptyMessage);
      dynamicTranslationsTable.getFlexCellFormatter().setColSpan(1, 0, 4);
    }

    dynamicTranslationsWorkspace.updatePagination(
        totalEntryCount == 0 ? 0 : firstEntryIndex + 1,
        endEntryIndex,
        totalEntryCount,
        dynamicTranslationsPageIndex + 1,
        totalPages);
  }

  private List<String> getFilteredDynamicTranslationKeys() {
    ArrayList<String> dynamicTranslationKeys = new ArrayList<String>(
            dynamicTranslationEntries.keySet());
    Collections.sort(dynamicTranslationKeys);

    List<String> filteredDynamicTranslationKeys = new ArrayList<String>();
    String normalizedSearchQuery = dynamicTranslationsSearchQuery.toLowerCase();

    for (String dynamicTranslationKey : dynamicTranslationKeys) {
      DynamicTranslationEntry dynamicTranslationEntry = dynamicTranslationEntries.get(
              dynamicTranslationKey);

      if (dynamicTranslationEntry == null) {
        continue;
      }

      if (normalizedSearchQuery.length() == 0 || matchesDynamicTranslationSearch(
        dynamicTranslationKey, dynamicTranslationEntry, normalizedSearchQuery)) {
          filteredDynamicTranslationKeys.add(dynamicTranslationKey);
      }
    }

    return filteredDynamicTranslationKeys;
  }

  private boolean matchesDynamicTranslationSearch(
    String dynamicTranslationKey,
    DynamicTranslationEntry dynamicTranslationEntry,
    String normalizedSearchQuery) {
    if (containsSearchText(dynamicTranslationKey, normalizedSearchQuery)
        || containsSearchText(
            dynamicTranslationEntry.getBaseText(), normalizedSearchQuery)
        || containsSearchText(getTranslationValue(dynamicTranslationKey, selectedLanguage),
            normalizedSearchQuery)) {
      return true;
    }

    for (String placeholder : dynamicTranslationEntry.getPlaceholders()) {
      if (containsSearchText(placeholder, normalizedSearchQuery)) {
        return true;
      }
    }

    return false;
  }

  private boolean containsSearchText(String searchableText, String normalizedSearchQuery) {
    return searchableText != null && searchableText.toLowerCase().contains(
      normalizedSearchQuery);
  }

  private int getPaginationPageCount(int totalEntryCount) {
    return Math.max(1, (totalEntryCount + TRANSLATIONS_PER_PAGE - 1) / TRANSLATIONS_PER_PAGE);
  }

  /**
   * Removes all rendered rows before rebuilding the dynamic table.
   */
  private void clearDynamicTranslationsTable() {
    while (dynamicTranslationsTable.getRowCount() > 0) {
      dynamicTranslationsTable.removeRow(0);
    }
  }

  /**
   * Adds the column headings for Designer-property translations.
   */
  private void addHeader() {
    table.setText(0, 0, "Screen");
    table.setText(0, 1, "Component");
    table.setText(0, 2, "Type");
    table.setText(0, 3, "Property");
    table.setText(0, 4, "Base Text");
    table.setText(0, 5,
        TranslationLanguageCatalog.getDisplayLabel(selectedLanguage));
    table.getRowFormatter().setStylePrimaryName(0, "ode-i18n-table-header");
  }

  private void showJsonDialog(String title, String message, String json) {
    final DialogBox dialog = new DialogBox(false, true);
    dialog.setText(title);
    dialog.setGlassEnabled(false);
    dialog.setAnimationEnabled(true);

    VerticalPanel panel = new VerticalPanel();
    panel.setSpacing(8);
    panel.setWidth("720px");

    Label messageLabel = new Label(message);

    TextArea jsonTextArea = new TextArea();
    jsonTextArea.setText(json);
    jsonTextArea.setWidth("700px");
    jsonTextArea.setVisibleLines(16);
    jsonTextArea.getElement().setAttribute("spellcheck", "false");

    Anchor closeLink = new Anchor("Close");
    closeLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    panel.add(messageLabel);
    panel.add(jsonTextArea);
    panel.add(closeLink);

    dialog.setWidget(panel);
    dialog.center();
  }

  private boolean isTranslationSetupComplete() {
    return baseLanguage != null && baseLanguage.length() > 0;
  }

  private void updateTranslationsSetting() {
    if (!isTranslationSetupComplete()) {
      return;
    }

    projectEditor.changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS,
        exportJson());
  }

  private String getJsonString(JSONObject object, String name) {
    if (object == null || object.get(name) == null || object.get(name).isString() == null) {
      return "";
    }

    return object.get(name).isString().stringValue();
  }

  private List<String> getJsonStringArray(JSONObject object, String name) {
    List<String> values = new ArrayList<String>();

    if (object == null || object.get(name) == null || object.get(name).isArray() == null) {
      return values;
    }

    JSONArray array = object.get(name).isArray();
    for (int i = 0; i < array.size(); i++) {
      JSONValue value = array.get(i);
      if (value != null && value.isString() != null) {
        values.add(value.isString().stringValue());
      }
    }

    return values;
  }

  private void loadSavedTranslations() {
    if (savedTranslationsLoaded) {
      return;
    }

    savedTranslationsLoaded = true;

    try {
      String savedJson = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS);

      if (savedJson == null || savedJson.length() == 0) {
        return;
      }

      JSONValue parsed = JSONParser.parseStrict(savedJson);
      JSONObject root = parsed.isObject();
      if (root == null) {
        return;
      }

      String savedBaseLanguage =
          TranslationLanguageCatalog.normalizeLanguageCode(
              getJsonString(root, "baseLanguage"));

      if (savedBaseLanguage.length() > 0) {
        baseLanguage = savedBaseLanguage;
      }

      JSONValue languagesValue = root.get("languages");
      if (languagesValue != null && languagesValue.isArray() != null) {
        JSONArray savedLanguages = languagesValue.isArray();
        languages.clear();

        for (int i = 0; i < savedLanguages.size(); i++) {
          JSONValue languageValue = savedLanguages.get(i);
          if (languageValue != null && languageValue.isString() != null) {
            addLanguage(languageValue.isString().stringValue(), false);
          }
        }

      }

      JSONValue entriesValue = root.get("entries");
      if (entriesValue == null || entriesValue.isObject() == null) {
        return;
      }

      JSONObject entries = entriesValue.isObject();
      for (String key : entries.keySet()) {
        JSONValue entryValue = entries.get(key);
        if (entryValue == null || entryValue.isObject() == null) {
          continue;
        }

        JSONObject entry = entryValue.isObject();

        String kind = getJsonString(entry, "kind");
        if ("dynamic".equals(kind)) {
          String baseText = getJsonString(entry, "baseText");
          List<String> placeholders = getJsonStringArray(entry, "placeholders");

          dynamicTranslationEntries.put(key,
              new DynamicTranslationEntry(key, baseText, placeholders));
        }

        JSONValue sourceValue = entry.get("source");
        JSONObject source = null;
        if (sourceValue != null && sourceValue.isObject() != null) {
          source = sourceValue.isObject();
        }

        if (source != null) {
          String screenName = getJsonString(source, "screen");
          String componentName = getJsonString(source, "component");
          String propertyName = getJsonString(source, "property");

          if (screenName.length() > 0 && componentName.length() > 0
              && propertyName.length() > 0) {
            locatorToTranslationKey.put(makeLocator(screenName, componentName, propertyName),
                key);
          }
        }

        JSONValue translationsValue = entry.get("translations");
        if (translationsValue == null || translationsValue.isObject() == null) {
          continue;
        }

        JSONObject translations = translationsValue.isObject();
        for (String language : translations.keySet()) {
          JSONValue translatedValue = translations.get(language);
          if (translatedValue != null && translatedValue.isString() != null) {
            addLanguage(language, false);
            setTranslationValue(key, language, translatedValue.isString().stringValue());
          }
        }
      }
    } catch (RuntimeException e) {
      // Ignore invalid saved data for now. The table can still rebuild from the current project.
    }
  }

  /**
   * Rebuilds translations after a Designer change and schedules project-settings autosave.
   */
  public void handleDesignerContentChanged() {
    refresh();
    updateTranslationsSetting();
  }

  public void handleComponentRenamed(String screenName, String oldName, String newName) {
    loadSavedTranslations();

    if (oldName == null || oldName.length() == 0 || newName == null || newName.length() == 0
        || oldName.equals(newName)) {
      return;
    }

    boolean changed = false;
    ArrayList<String> oldLocators = new ArrayList<String>(locatorToTranslationKey.keySet());

    for (String oldLocator : oldLocators) {
      String[] parts = splitLocator(oldLocator);
      if (parts.length != 3) {
        continue;
      }

      if (!screenName.equals(parts[0]) || !oldName.equals(parts[1])) {
        continue;
      }

      String propertyName = parts[2];
      String key = locatorToTranslationKey.remove(oldLocator);
      String newLocator = makeLocator(screenName, newName, propertyName);

      LOG.info("i18n preserving key on rename: key=" + key
          + " oldLocator=" + oldLocator
          + " newLocator=" + newLocator);

      locatorToTranslationKey.put(newLocator, key);
      changed = true;
    }

    refresh();

    if (changed) {
      updateTranslationsSetting();
    }
  }

  public void handleComponentRemoved(String screenName, String componentName) {
    loadSavedTranslations();

    if (componentName == null || componentName.length() == 0) {
      return;
    }

    boolean changed = false;
    ArrayList<String> locatorsToRemove = new ArrayList<String>();

    for (String locator : locatorToTranslationKey.keySet()) {
      String[] parts = splitLocator(locator);
      if (parts.length != 3) {
        continue;
      }

      if (screenName.equals(parts[0]) && componentName.equals(parts[1])) {
        locatorsToRemove.add(locator);
      }
    }

    for (String locator : locatorsToRemove) {
      String key = locatorToTranslationKey.remove(locator);
      if (key != null) {
        translationValues.remove(key);
        translationEntries.remove(key);
        changed = true;
      }
    }

    refresh();

    if (changed) {
      updateTranslationsSetting();
    }
  }

  private String exportJson() {
    JSONObject root = new JSONObject();

    root.put("baseLanguage",
      new JSONString(baseLanguage == null ? "" : baseLanguage));

    JSONArray languagesJson = new JSONArray();
    for (int i = 0; i < languages.size(); i++) {
      languagesJson.set(i, new JSONString(languages.get(i)));
    }
    root.put("languages", languagesJson);

    JSONObject entries = new JSONObject();
    ArrayList<String> keys = new ArrayList<String>(translationEntries.keySet());
    Collections.sort(keys);

    for (String key : keys) {
      TranslationEntry entry = translationEntries.get(key);
      if (entry == null) {
        continue;
      }

      JSONObject entryObject = new JSONObject();
      entryObject.put("kind", new JSONString("static"));

      JSONObject source = new JSONObject();
      source.put("screen", new JSONString(entry.getScreenName()));
      source.put("component", new JSONString(entry.getComponentName()));
      source.put("type", new JSONString(entry.getComponentType()));
      source.put("property", new JSONString(entry.getPropertyName()));
      source.put("baseText", new JSONString(entry.getBaseText()));
      entryObject.put("source", source);

      JSONObject translations = new JSONObject();
      for (String language : languages) {
        String translatedValue = getTranslationValue(key, language);
        if (translatedValue.length() > 0) {
          translations.put(language, new JSONString(translatedValue));
        }
      }
      entryObject.put("translations", translations);

      entries.put(key, entryObject);
    }

    ArrayList<String> dynamicKeys = new ArrayList<String>(dynamicTranslationEntries.keySet());
    Collections.sort(dynamicKeys);

    for (String key : dynamicKeys) {
      DynamicTranslationEntry entry = dynamicTranslationEntries.get(key);
      if (entry == null) {
        continue;
      }

      JSONObject entryObject = new JSONObject();
      entryObject.put("kind", new JSONString("dynamic"));
      entryObject.put("baseText", new JSONString(entry.getBaseText()));

      JSONArray placeholders = new JSONArray();
      for (int i = 0; i < entry.getPlaceholders().size(); i++) {
        placeholders.set(i, new JSONString(entry.getPlaceholders().get(i)));
      }
      entryObject.put("placeholders", placeholders);

      JSONObject translations = new JSONObject();
      for (String language : languages) {
        String translatedValue = getTranslationValue(key, language);
        if (translatedValue.length() > 0) {
          translations.put(language, new JSONString(translatedValue));
        }
      }
      entryObject.put("translations", translations);

      entries.put(key, entryObject);
    }

    root.put("entries", entries);

    return root.toString();
  }

  private void addDynamicTranslationEntry() {
    loadSavedTranslations();

    String key = dynamicKeyTextBox.getValue();
    String baseText = dynamicBaseTextBox.getValue();
    String placeholdersText = dynamicPlaceholdersTextBox.getValue();

    if (key != null) {
      key = key.trim();
    }

    if (baseText != null) {
      baseText = baseText.trim();
    }

    if (!isValidDynamicKey(key)) {
      Window.alert("Use a safe dynamic key such as welcome_message or errors.network_timeout.");
      return;
    }

    if (baseText == null || baseText.length() == 0) {
      Window.alert("Base text is required for a dynamic translation.");
      return;
    }

    if (dynamicTranslationEntries.containsKey(key) || translationEntries.containsKey(key)) {
      Window.alert("A translation key with this name already exists.");
      return;
    }

    List<String> placeholders = parsePlaceholders(placeholdersText);
    if (placeholders == null) {
      Window.alert("Placeholders must be comma-separated names like name, count, or user_id.");
      return;
    }

    dynamicTranslationEntries.put(key, new DynamicTranslationEntry(key, baseText, placeholders));

    dynamicKeyTextBox.setValue("");
    dynamicBaseTextBox.setValue("");
    dynamicPlaceholdersTextBox.setValue("");

    refresh();
    updateTranslationsSetting();
  }

  private void deleteDynamicTranslationEntry(String key) {
    if (key == null || key.length() == 0) {
      return;
    }

    boolean confirmed = Window.confirm("Delete dynamic translation key '" + key + "'?");
    if (!confirmed) {
      return;
    }

    dynamicTranslationEntries.remove(key);
    translationValues.remove(key);

    refresh();
    updateTranslationsSetting();
  }

  private boolean isValidDynamicKey(String key) {
    return key != null
        && key.length() > 0
        && key.matches("[A-Za-z][A-Za-z0-9_.-]*");
  }

  private List<String> parsePlaceholders(String placeholdersText) {
    List<String> placeholders = new ArrayList<String>();

    if (placeholdersText == null || placeholdersText.trim().length() == 0) {
      return placeholders;
    }

    String[] parts = placeholdersText.split(",");
    for (String part : parts) {
      String placeholder = part.trim();
      if (placeholder.length() == 0) {
        continue;
      }

      if (!isValidPlaceholderName(placeholder)) {
        return null;
      }

      if (!placeholders.contains(placeholder)) {
        placeholders.add(placeholder);
      }
    }

    return placeholders;
  }

  private boolean isValidPlaceholderName(String placeholder) {
    return placeholder != null
        && placeholder.length() > 0
        && placeholder.matches("[A-Za-z_][A-Za-z0-9_]*");
  }

  private void addLanguage(String language, boolean selectLanguage) {
    String normalizedLanguage =
        TranslationLanguageCatalog.normalizeLanguageCode(language);

    if (normalizedLanguage.length() == 0) {
      return;
    }

    if (!languages.contains(normalizedLanguage)) {
      languages.add(normalizedLanguage);
    }

    if (selectLanguage) {
      selectedLanguage = normalizedLanguage;
    }
  }

  private void ensureSelectedLanguage() {
    if (languages.isEmpty()) {
      selectedLanguage = null;
      return;
    }

    if (selectedLanguage == null || !languages.contains(selectedLanguage)) {
      selectedLanguage = languages.get(0);
    }
  }

  private void deleteSelectedLanguage() {
    ensureSelectedLanguage();

    if (selectedLanguage == null) {
      return;
    }

    String languageToDelete = selectedLanguage;
    String languageLabel =
        TranslationLanguageCatalog.getDisplayLabel(languageToDelete);
    boolean confirmed = Window.confirm(
        "Delete " + languageLabel
            + " and all translation values for this language?");

    if (!confirmed) {
      return;
    }

    languages.remove(languageToDelete);

    ArrayList<String> emptyKeys = new ArrayList<String>();
    for (String key : translationValues.keySet()) {
      Map<String, String> values = translationValues.get(key);
      if (values == null) {
        continue;
      }

      values.remove(languageToDelete);
      if (values.isEmpty()) {
        emptyKeys.add(key);
      }
    }

    for (String key : emptyKeys) {
      translationValues.remove(key);
    }

    selectedLanguage = languages.isEmpty() ? null : languages.get(0);
    refresh();
    updateTranslationsSetting();
  }

  private String getTranslationValue(String translationKey, String language) {
    Map<String, String> values = translationValues.get(translationKey);
    if (values == null) {
      return "";
    }

    String value = values.get(language);
    return value == null ? "" : value;
  }

  private void setTranslationValue(String translationKey, String language, String value) {
    Map<String, String> values = translationValues.get(translationKey);
    if (values == null) {
      values = new HashMap<String, String>();
      translationValues.put(translationKey, values);
    }

    if (value == null || value.length() == 0) {
      values.remove(language);
      if (values.isEmpty()) {
        translationValues.remove(translationKey);
      }
    } else {
      values.put(language, value);
    }
  }

  private TextBox createTranslationTextBox(final String translationKey, final String language) {
    final TextBox textBox = new TextBox();
    textBox.setWidth("100%");
    textBox.setValue(getTranslationValue(translationKey, language));

    textBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setTranslationValue(translationKey, language, textBox.getValue());
        updateTranslationsSetting();
      }
    });

    return textBox;
  }

  private boolean isTranslatableProperty(String propertyName) {
    return "Text".equals(propertyName)
        || "Hint".equals(propertyName)
        || "Title".equals(propertyName)
        || "Prompt".equals(propertyName);
  }

  private void clearTable() {
    while (table.getRowCount() > 0) {
      table.removeRow(0);
    }
  }

  private String makeLocator(String screenName, String componentName, String propertyName) {
    return safe(screenName) + LOCATOR_SEPARATOR
        + safe(componentName) + LOCATOR_SEPARATOR
        + safe(propertyName);
  }

  private String[] splitLocator(String locator) {
    return locator.split(LOCATOR_SEPARATOR, -1);
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private String getOrCreateTranslationKey(String screenName, String componentName,
      String propertyName) {
    String locator = makeLocator(screenName, componentName, propertyName);
    String existingKey = locatorToTranslationKey.get(locator);
    if (existingKey != null && existingKey.length() > 0) {
      return existingKey;
    }

    String generatedKey = TranslationKeyGenerator.generate(screenName, componentName, propertyName);
    locatorToTranslationKey.put(locator, generatedKey);
    return generatedKey;
  }

}
