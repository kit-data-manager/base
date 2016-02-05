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
package edu.kit.dama.staging.util;

import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class StagingUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingUtils.class);

  /**
   * Hidden constructor.
   */
  StagingUtils() {
  }

  /**
   * Prepare the provided path for acess. The path should be obtained from a
   * configuration file or some other configuration source. It may or may not
   * contain variables to replace. Currently, only $tmp is supported and will be
   * replaced by the user temp directory. In addition, the resulting folder will
   * be created if it does not exist.
   *
   * @param pPath The path to prepare.
   *
   * @return The prepared path with replaced variables.
   *
   * @throws ConfigurationException If pPath is null or if the resulting path
   * could not be created.
   */
  public static String preparePath(String pPath) throws ConfigurationException {
    if (pPath == null) {
      throw new ConfigurationException("Argument pPath must not be null.");
    }
    //replace tmp-dir
    String tmp = System.getProperty("java.io.tmpdir");
    if (!tmp.startsWith("/")) {
      tmp = "/" + tmp;
    }
    String result = pPath.replaceAll(Pattern.quote("$tmp"), Matcher.quoteReplacement(tmp));

    if (!new File(result).exists()) {
      LOGGER.debug("Creating path at {}", result);
      if (new File(result).mkdirs()) {
        LOGGER.debug("Path successfully created at {}", result);
      } else {
        throw new ConfigurationException("Failed to create path at " + result);
      }
    } else {
      LOGGER.debug("Path at {} already exists.", result);
    }
    return result;
  }

  /**
   * Get the temporary directory all transfers. This directory is located in the
   * user home directory under ~/.lsdf/. Within the temporary directory the
   * transfer can store status information or checkpoint data to be able to
   * resume failed transfers.
   *
   * @return The transfers temporary directory
   *
   * @throws IOException If there was not set any TID for this transfer or if
   * there are problems getting the user's home directory
   */
  public static String getTempDir() throws IOException {
    File userHome = SystemUtils.getUserHome();
    if (userHome == null || !userHome.isDirectory() || !userHome.canRead() || !userHome.canWrite()) {
      throw new IOException("Invalid user home directory '" + userHome + "'");
    }
    return FilenameUtils.concat(userHome.getCanonicalPath(), ".lsdf");
  }

  /**
   * Get the temporary directory of the transfer associated with the provided
   * transfer container. This directory is located in the user home directory
   * under ~/.lsdf/&lt;TID&gt;, where &lt;TID&gt; stands for the transfer ID of this
   * transfer. To avoid problems with invalid pathnames, the TID gets UTF-8
   * URL-encoded. Within the temporary directory the transfer can store status
   * information or checkpoint data to be able to resume failed transfers.
   *
   * @param pContainer The transfer container for which the temp dir should be
   * returned.
   *
   * @return The transfer's temporary directory
   *
   * @throws IOException If there was not set any TID for this transfer or if
   * there are problems getting the users home directory
   */
  public static String getTempDir(TransferTaskContainer pContainer) throws IOException {
    if (pContainer != null) {
      LOGGER.debug("Determining transfer tmp directory.");
      File userHome = SystemUtils.getUserHome();
      if (!isAccessibleDirectory(userHome)) {
        //user home not usable (maybe no home directory available). Try system temp dir...
        userHome = SystemUtils.getJavaIoTmpDir();
        if (!isAccessibleDirectory(userHome)) {
          throw new IOException("Failed to obtain valid temp directory. UserHome (" + SystemUtils.getUserHome() + ") and TmpDir (" + userHome + ") are not usable.");
        } else {
          LOGGER.debug("Using tmp directory.");
        }
      } else {
        LOGGER.debug("Using user home directory.");
      }

      return FilenameUtils.concat(FilenameUtils.concat(userHome.getCanonicalPath(), ".lsdf"), URLEncoder.encode(pContainer.getUniqueTransferIdentifier(), "UTF-8"));
    } else {
      throw new IOException("Failed to obtain temporary transfer directory. Transfer container must not be 'null'");
    }
  }

  /**
   * Checking directory for beeing a directory, readable and writable.
   *
   * @param pDir The directory to check.
   *
   * @return TRUE if pDir is a directory, readable and writable.
   */
  private static boolean isAccessibleDirectory(File pDir) {
    return pDir != null && pDir.isDirectory() && pDir.canRead() && pDir.canWrite();
  }

}
