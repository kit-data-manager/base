/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.transfer.client.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class TestDataBuilder {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TestDataBuilder.class);
  private static String targetDir = "d:/testData/";
  private static final int BASE_FILES = 100000;
  private static final int SUB_DIRS = 0;
  private static final int SUB_SUB_DIRS = 0;
  private static final int FILES_IN_SUB_SUB_DIRS = 0;
  private static final double MAX_BYTES = 255.0;

  /**
   * Hidden default constructor
   */
  private TestDataBuilder() {
  }

  /**
   * Set the target dir where to write the data
   *
   * @param pTargetDir The target dir
   */
  public static void setTargetDir(String pTargetDir) {
    targetDir = pTargetDir;
  }

  /**
   * The entry point.
   *
   * @param args Command line args.
   */
  public static void main(String[] args) {
    try {
      FileUtils.forceMkdir(new File(targetDir));

      byte[] oneMegaByte = buildRandomData((int) FileUtils.ONE_KB);

      for (int i = 0; i < BASE_FILES; i++) {
        File f = new File(targetDir + i + ".bin");
        LOGGER.debug(" - Writing file {}", f);
        FileUtils.writeByteArrayToFile(f, oneMegaByte);
      }
      for (int j = 0; j < SUB_DIRS; j++) {
        String subdir = targetDir + "subDir" + j;
        for (int k = 0; k < SUB_SUB_DIRS; k++) {
          String subsubdir = subdir + "/subsubdir" + k;
          oneMegaByte = buildRandomData((int) FileUtils.ONE_KB);
          for (int i = 0; i < FILES_IN_SUB_SUB_DIRS; i++) {
            File f = new File(subsubdir + File.separator + i + "-" + j + "-" + k + ".bin");
            LOGGER.debug(" - Writing file {}", f);
            FileUtils.writeByteArrayToFile(f, oneMegaByte);
          }
        }
      }
    } catch (IOException ioe) {
      LOGGER.error("Failed to build data", ioe);
    }
  }

  /**
   * Build a random data array with the provided size
   *
   * @param size The byte array size
   *
   * @return The byte array containing random data
   */
  private static byte[] buildRandomData(int size) {
    byte[] oneMegaByte = new byte[size];
    for (int i = 0; i < size; i++) {
      oneMegaByte[i] = (byte) (Math.random() * MAX_BYTES);
    }
    return oneMegaByte;
  }
}
