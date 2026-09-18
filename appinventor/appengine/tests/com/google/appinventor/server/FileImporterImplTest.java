// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import junitx.framework.ListAssert;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Test FileImporterImpl.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FileImporterImplTest extends LocalDatastoreTestCase {
  private static final String USER_ID = "12345678";
  private static final String USER_EMAIL_ADDRESS = "joeuser@gmail.com";

  // This project name matches what is in Project1.zip and ProjectWithAssets.zip
  private static final String PROJECT_NAME_1 = "project1";

  // This project name does not match what is in Project1.zip.
  private static final String PROJECT_NAME_2 = "project2";

  private StorageIo storageIo;
  private FileImporter fileImporter;

  public static final String TESTING_SOURCE_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/tests/com/google/appinventor/server/";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();
    // Create user with given parameters
    storageIo.getUser(USER_ID, USER_EMAIL_ADDRESS);
    fileImporter = new FileImporterImpl();
  }

  private UserProject importProjectArchive(String zipFileName, String projectName)
      throws Exception {
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    return fileImporter.importProject(USER_ID, projectName, new FileInputStream(zip));
  }

  public void testImportProject() throws Exception {
    UserProject userProject = importProjectArchive("Project1.zip", PROJECT_NAME_1);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_1, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_1, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.yail");
  }

  public void testImportProject_withAssets() throws Exception {
    UserProject userProject = importProjectArchive("ProjectWithAssets.zip", PROJECT_NAME_1);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_1, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_1, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.yail");
    ListAssert.assertContains(projectSourceFiles,
        "assets/kitty.png");
    ListAssert.assertContains(projectSourceFiles,
        "assets/meow.mp3");
  }

  public void testProjectNameUsed() throws Exception {
    UserProject userProject = importProjectArchive("Project1.zip", PROJECT_NAME_2);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_2, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_2, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.yail");
  }

  public void testImportProject_withoutProjectHistory() throws Exception {
    String zipFileName = "Project1.zip";
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    String projectName = "MyProject";
    UserProject userProject = fileImporter.importProject(USER_ID, projectName,
        new FileInputStream(zip), null);
    assertNotNull(userProject);
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSrcFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSrcFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.blk");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.scm");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.yail");
  }

  public void testImportProject_withProjectHistory() throws Exception {
    String zipFileName = "Project1.zip";
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    String projectName = "MyProject";
    UserProject userProject =  fileImporter.importProject(USER_ID, projectName,
        new FileInputStream(zip), "HISTORY");
    assertNotNull(userProject);
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    assertEquals("HISTORY", storageIo.getProjectHistory(USER_ID, projectId));
    List<String> projectSrcFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSrcFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.blk");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.scm");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.yail");
  }

  public void testImportProject_preservesI18nTranslations() throws Exception {
    String translations = "{\"baseLanguage\":\"en\",\"languages\":[\"hi\"],"
        + "\"entries\":{\"welcome_user\":{\"kind\":\"dynamic\","
        + "\"baseText\":\"Hello {name}\",\"placeholders\":[\"name\"],"
        + "\"translations\":{\"hi\":\"नमस्ते {name}\"}}}}";

    Properties properties = new Properties();
    properties.setProperty("name", "originalProject");
    properties.setProperty("main", "appinventor.ai_original.originalProject.Screen1");
    properties.setProperty(
        SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS,
        translations);

    ByteArrayOutputStream propertiesBytes = new ByteArrayOutputStream();
    properties.store(propertiesBytes, "");

    ByteArrayOutputStream archiveBytes = new ByteArrayOutputStream();
    try (ZipOutputStream archive = new ZipOutputStream(archiveBytes)) {
        addArchiveEntry(
            archive,
            "youngandroidproject/project.properties",
            propertiesBytes.toByteArray());

        addArchiveEntry(
            archive,
            "src/appinventor/ai_original/originalProject/Screen1.scm",
            new byte[0]);
    }

    UserProject userProject = fileImporter.importProject(
        USER_ID,
        PROJECT_NAME_1,
        new ByteArrayInputStream(archiveBytes.toByteArray()));

    long projectId = userProject.getProjectId();

    String importedPropertiesText = storageIo.downloadFile(
        USER_ID,
        projectId,
        "youngandroidproject/project.properties",
        StorageUtil.DEFAULT_CHARSET);

    Properties importedProperties = new Properties();
    importedProperties.load(new StringReader(importedPropertiesText));

    assertEquals(PROJECT_NAME_1, importedProperties.getProperty("name"));
    assertEquals(
        "appinventor.ai_joeuser.project1.Screen1",
        importedProperties.getProperty("main"));

    assertEquals(
        translations,
        importedProperties.getProperty(
            SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS));

    JSONObject importedSettings = new JSONObject(
        storageIo.loadProjectSettings(USER_ID, projectId));

    assertEquals(
        translations,
        importedSettings
            .getJSONObject(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS)
            .getString(SettingsConstants.YOUNG_ANDROID_SETTINGS_I18N_TRANSLATIONS));
  }

  public void testEmptyZip() throws Exception {
    try {
      UserProject userProject = importProjectArchive("EmptyZip.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }

  public void testBuggyZip() throws Exception {
    try {
      UserProject userProject = importProjectArchive("Buggy.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }

  public void testNotAValidProjectArchive() throws Exception {
    try {
      UserProject userProject =
          importProjectArchive("NotAValidProjectArchive.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }

  private static void addArchiveEntry(
        ZipOutputStream archive, String name, byte[] content) throws Exception {
    archive.putNextEntry(new ZipEntry(name));
    archive.write(content);
    archive.closeEntry();
  }

}
