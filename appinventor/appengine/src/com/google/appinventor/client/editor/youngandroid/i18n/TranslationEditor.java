// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Command;

/**
 * Top-level Translation editor shown beside Designer and Blocks.
 */
public final class TranslationEditor extends FileEditor {
  public static final String EDITOR_TYPE = "TranslationEditor";
  public static final String ENTITY_NAME = "Translations";

  private final YaProjectEditor yaProjectEditor;
  private final TranslationPanel translationPanel;
  private boolean loadComplete;

  public TranslationEditor(YaProjectEditor projectEditor, ProjectRootNode projectRootNode) {
    super(projectEditor, new TranslationFileNode(projectRootNode));
    yaProjectEditor = projectEditor;
    translationPanel = new TranslationPanel(this, projectEditor);
    initWidget(translationPanel);
  }

  public TranslationPanel getTranslationPanel() {
    return translationPanel;
  }

  @Override
  public DropTargetProvider getDropTargetProvider() {
    return new DropTargetProvider() {
      @Override
      public DropTarget[] getDropTargets() {
        return new DropTarget[0];
      }
    };
  }

  @Override
  public void loadFile(final Command afterFileLoaded) {
    if (loadComplete) {
      if (afterFileLoaded != null) {
        afterFileLoaded.execute();
      }
      return;
    }

    final long projectId = getProjectId();
    final String fileId = getFileId();

    OdeAsyncCallback<ChecksumedLoadFile> callback =
        new OdeAsyncCallback<ChecksumedLoadFile>(MESSAGES.loadError()) {
          @Override
          public void onSuccess(ChecksumedLoadFile result) {
            String fileContent;

            try {
              fileContent = result.getContent();
            } catch (ChecksumedFileException e) {
              onFailure(e);
              return;
            }

            translationPanel.loadJson(fileContent);
            loadComplete = true;

            if (afterFileLoaded != null) {
              afterFileLoaded.execute();
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof ChecksumedFileException) {
              Ode.getInstance().recordCorruptProject(
                  projectId, fileId, caught.getMessage());
            }
            super.onFailure(caught);
          }
        };

    Ode.getInstance().getProjectService().load2(projectId, fileId, callback);
  }

  void scheduleAutoSave() {
    Ode.getInstance().getEditorManager().scheduleAutoSave(this);
  }

  @Override
  public String getTabText() {
    return "Translations";
  }

  @Override
  public void onShow() {
    super.onShow();

    if (loadComplete) {
      translationPanel.refresh();
    }
  }

  @Override
  public String getRawFileContent() {
    return translationPanel.exportJson();
  }

  @Override
  public void onSave() {
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
  }

  @Override
  public String getEditorType() {
    return EDITOR_TYPE;
  }

  @Override
  public String getEntityName() {
    return ENTITY_NAME;
  }
}
