// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.TRANSLATIONS_FILE;

import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;

/**
 * File node representing the project's persisted translation data.
 */
final class TranslationFileNode extends FileNode {
  private static final long serialVersionUID = 1L;

  private final ProjectRootNode projectRootNode;

  TranslationFileNode(ProjectRootNode projectRootNode) {
    super("Translations", TRANSLATIONS_FILE);
    this.projectRootNode = projectRootNode;
  }

  @Override
  public ProjectRootNode getProjectRoot() {
    return projectRootNode;
  }

  @Override
  public long getProjectId() {
    return projectRootNode.getProjectId();
  }

  @Override
  public String getProjectType() {
    return projectRootNode.getProjectType();
  }
}
