/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.util;

import static edu.kit.dama.util.ZipUtilsTest.deleteFileRecursively;
import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hartmann-v
 */
public class ZipUtilsSimpleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtilsSimpleTest.class);

  private static String srcDir = "src/test/resources/ziptest/";

  private static String baseDir = "target/test/sources/filtered/";

  private static String outputDir = "target/test/zip/";

  public ZipUtilsSimpleTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    // create inputDir without .svn directories
    Path source = Paths.get(srcDir);

    Path dest = Paths.get(baseDir);
    Files.createDirectories(dest);
    
    boolean prompt = false;
    boolean preserve = true;
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Copy.SvnTreeCopier tc = new Copy.SvnTreeCopier(source, dest, prompt, preserve);
    Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
    
    File file = new File(outputDir);
    deleteFileRecursively(file);
    file.mkdirs();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    deleteFileRecursively(new File(outputDir));
    deleteFileRecursively(new File(baseDir));
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of zipDirectory method, of class ZipUtils.
   */
  @Test
  public void testZipDirectoryTxtOnly() throws Exception {
    String folder = "filterResult";
    File zipFile = new File(outputDir + "zipDirTxt.zip");
    File pDirectory = new File(baseDir + "filter");
    String[] pExtension = new String[]{"txt"};
    File extractDir = new File(outputDir + folder);
    extractDir.mkdirs();
    ZipUtils.zipDirectory(zipFile, pDirectory, pExtension);
    ZipUtils.unzip(zipFile, extractDir);
    File expectedDir = new File(baseDir + folder);
    Assert.assertTrue(extractDir.getPath(), extractDir.exists());
    Assert.assertTrue(expectedDir.getPath(), expectedDir.exists());
    boolean compareFiles = ZipUtilsTest.compareFiles(expectedDir, extractDir);
    if (!compareFiles) {
      ZipUtilsTest.compareFiles(expectedDir, extractDir, true);
    }
    Assert.assertTrue(compareFiles);
    ZipUtilsTest.deleteFileRecursively(extractDir);
    zipFile.delete();
  }

  /**
   * Test of zipDirectory method, of class ZipUtils.
   */
  @Test
  public void testZipDirectoryTxtAndDoc() throws Exception {
    String folder = "filterResult2";
    File zipFile = new File(outputDir + "zipDirTxtAndDoc.zip");
    File pDirectory = new File(baseDir + "filter");
    String[] pExtension = new String[]{"txt", ".DOC"};
    File extractDir = new File(outputDir + folder);
    extractDir.mkdirs();
    ZipUtils.zipDirectory(zipFile, pDirectory, pExtension);
    ZipUtils.unzip(zipFile, extractDir);
    File expectedDir = new File(baseDir + folder);
    Assert.assertTrue(extractDir.getPath(), extractDir.exists());
    Assert.assertTrue(expectedDir.getPath(), expectedDir.exists());
    boolean compareFiles = ZipUtilsTest.compareFiles(expectedDir, extractDir);
    if (!compareFiles) {
      ZipUtilsTest.compareFiles(expectedDir, extractDir, true);
    }
    Assert.assertTrue(compareFiles);
    ZipUtilsTest.deleteFileRecursively(extractDir);
    zipFile.delete();
  }

  /**
   * Test of zipDirectory method, of class ZipUtils.
   */
  @Test
  public void testZipDirectoryAll() throws Exception {
    String folder = "filter";
    File zipFile = new File(outputDir + "zipDirAll.zip");
    File pDirectory = new File(baseDir + "filter");
    String[] pExtension = null;
    File extractDir = new File(outputDir + folder);
    extractDir.mkdirs();
    ZipUtils.zipDirectory(zipFile, pDirectory, pExtension);
    ZipUtils.unzip(zipFile, extractDir);
    File expectedDir = new File(baseDir + folder);
    boolean compareFiles = ZipUtilsTest.compareFiles(expectedDir, extractDir);
    if (!compareFiles) {
      ZipUtilsTest.compareFiles(expectedDir, extractDir, true);
    }
    Assert.assertTrue(compareFiles);
    ZipUtilsTest.deleteFileRecursively(extractDir);
    zipFile.delete();
  }

  /**
   * Test of zipSingleFile method, of class ZipUtils.
   */
  @Test
  public void testZipSingleFile() throws Exception {
    String folder = "fifth";
    File pDirectory = new File(baseDir + folder);
    File extractDir = new File(outputDir + folder);
    extractDir.mkdirs();
    for (File zipFile : pDirectory.listFiles()) {
      ZipUtils.zipSingleFile(extractDir, zipFile);
    }
    for (File zipFile : extractDir.listFiles()) {
      ZipUtils.unzip(zipFile, true);
    }
    boolean compareFiles = ZipUtilsTest.compareFiles(pDirectory, extractDir);
    if (!compareFiles) {
      ZipUtilsTest.compareFiles(pDirectory, extractDir, true);
    }
    Assert.assertTrue(compareFiles);
    ZipUtilsTest.deleteFileRecursively(extractDir);
  }

}
