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

import edu.kit.dama.util.Copy.SvnTreeCopier;
import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hartmann-v
 */
@RunWith(Parameterized.class)
public class ZipUtilsTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtilsTest.class);

  @Parameterized.Parameters
  public static List<Object[]> testData() {
    return Arrays.asList(
            //new Object[]{"first"}, //0  
            new Object[]{"second"}, //1  
            new Object[]{"third"}, //2  
            new Object[]{"fourth"}, //3  
            new Object[]{"fifth"}, //4  
            new Object[]{"sixth"} //5
    );
  }
  private String testDir;

  private static String srcDir = "src/test/resources/ziptest/";

  private static String baseDir = "target/test/sources/filtered/";

  private static String outputDir = "target/test/zip/";

  public ZipUtilsTest(String pTestData) {
    testDir = pTestData;
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
    SvnTreeCopier tc = new SvnTreeCopier(source, dest, prompt, preserve);
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
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test of zip method, of class ZipUtils.
   */
  @Test
  public void testZip_File_File() throws Exception {

    File pZipOut = new File(outputDir + "output_" + testDir + ".zip");
    File extractDir = new File(outputDir + testDir);
    extractDir.mkdir();
    File inputDir = new File(baseDir + testDir);
    ZipUtils.zip(inputDir, inputDir.getPath(), pZipOut);
    ZipUtils.unzip(pZipOut, extractDir);
    Assert.assertTrue(compareFiles(inputDir, extractDir));
    deleteFileRecursively(extractDir);
    Assert.assertFalse(extractDir.exists());
    pZipOut.delete();
    Assert.assertFalse(pZipOut.exists());

  }

  /**
   * Test of zip method, of class ZipUtils.
   */
  @Test
  public void testZip_File() throws Exception {
    File pZipOut = new File(outputDir + "output_" + testDir + ".zip");
    File extractDir = new File(outputDir + testDir);
    extractDir.mkdir();
    File inputDir = new File(baseDir + testDir).getAbsoluteFile();
    ZipUtils.zip(inputDir, pZipOut);
    ZipUtils.unzip(pZipOut, extractDir);
    Assert.assertTrue(compareFiles(inputDir, extractDir));
    deleteFileRecursively(extractDir);
    Assert.assertFalse(extractDir.exists());
    pZipOut.delete();
    Assert.assertFalse(pZipOut.exists());
  }

  /**
   * Test of zip method, of class ZipUtils.
   */
  @Test
  public void testCompareFiles() throws Exception {
    File inputDir = new File(baseDir + testDir).getAbsoluteFile();
    Assert.assertTrue(compareFiles(inputDir, inputDir));
  }

  /**
   * Compare two files/directories. Compares only name and size of file.
   *
   * @param pSourceDir source dir
   * @param pTargetDir target dir
   * @return seems to be the same.
   */
  public static boolean compareFiles(File pSourceDir, File pTargetDir) {
    return compareFiles(pSourceDir, pTargetDir, false);
  }

  /**
   * Compare two files/directories. Compares only name and size of file.
   *
   * @param pSourceDir source dir
   * @param pTargetDir target dir
   * @param debugMode print info messages to stdout.
   * @return seems to be the same.
   */
  public static boolean compareFiles(File pSourceDir, File pTargetDir, boolean debugMode) {
    boolean success = true;
    success = pSourceDir.isDirectory() == pTargetDir.isDirectory();
    if (debugMode) {
      System.out.printf("Compare '%s' <-> '%s'\n", pSourceDir.getPath(), pTargetDir.getPath() );
      System.out.printf("Is directory '%b' <-> '%b'\n", pSourceDir.isDirectory(), pTargetDir.isDirectory());
    }
    if (success) {
      if (pSourceDir.isDirectory()) {
        File[] fileListSource = pSourceDir.listFiles();
        File[] fileListTarget = pTargetDir.listFiles();
        success = fileListSource.length == fileListTarget.length;
        if (debugMode) {
          System.out.printf("No of Files: '%d' <-> '%d'\n", fileListSource.length, fileListTarget.length);
          for (File file : fileListSource) {
            System.out.printf("Source: %s\n", file.getPath());
          }
          for (File file : fileListTarget) {
            System.out.printf("Target:                            %s\n", file.getPath());
          }
        }
        if (success) {
          for (File file : fileListSource) {
            boolean check = false;
            for (File targetFile : fileListTarget) {
              if (file.getName().equals(targetFile.getName())) {
                check = true;
                success = compareFiles(file, targetFile, debugMode);
                break;
              }
            }
            if (!check) {
              success = false;
              LOGGER.error("'{}' doesn't exist in target folder!", file.getPath());
              System.out.printf("'%s' doesn't exist in target folder!", file.getPath());
              break;
            }
          }
        }
      } else {
        success = pSourceDir.getName().equals(pTargetDir.getName()) && pSourceDir.length() == pTargetDir.length();
        if (!success) {
          LOGGER.error("'{}' [{}] <> '{}' [{}]", pSourceDir.getPath(), 
                  pSourceDir.length(),  
                  pTargetDir.getPath(), 
                  pTargetDir.length());
        }
        if (debugMode) {
          System.out.printf("'%s' [%d] <> '%s' [%d]", pSourceDir.getPath(), 
                  pSourceDir.length(),  
                  pTargetDir.getPath(), 
                  pTargetDir.length());
        }
      }
    }
    return success;
  }

  /**
   * Delete file or directory.
   * In case of directory all containing files and directories will be
   * removed also.
   * @param fileOrDirectory file/directory which should be deleted.
   */
  public static void deleteFileRecursively(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File file : fileOrDirectory.listFiles()) {
        deleteFileRecursively(file);
      }
      fileOrDirectory.delete();
    } else {
      fileOrDirectory.delete();
    }
  }
}
