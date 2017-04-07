/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains some operating system related functionalities
 * used during staging, e.g. locking folders.
 *
 * @author jejkal
 */
public final class SystemUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtils.class);

    /**
     * Hidden constructor.
     */
    private SystemUtils() {
    }

    /**
     * Lock the provided folder pLocalFolder. At first, the current operating
     * system is checked for beeing a Unix based system. In this case, a
     * dedicated script will be used for locking. In all other cases or if the
     * script-based approach fails, a single file '.locked' is created inside
     * pLocalFolder.
     *
     * @param pFolder The folder to lock (must be a directory)
     *
     * @return TRUE on success, FALSE on any error
     */
    public static boolean lockFolder(File pFolder) {
        if (!pFolder.isDirectory()) {
            LOGGER.error("Provided argument 'pLocalFolder' ({}) must be a directory", pFolder);
            return false;
        }

        if (org.apache.commons.lang3.SystemUtils.IS_OS_UNIX && lockFolderUnix(pFolder)) {
            return true;
        }//else...fallback using '.locked' file

        File lockFile = new File(FilenameUtils.concat(pFolder.getAbsolutePath(), ".locked"));
        try {
            LOGGER.debug("Try to create lock '{}'", lockFile);
            FileUtils.touch(lockFile);
        } catch (IOException ioe) {
            LOGGER.error("Failed to lock folder '" + pFolder.getAbsolutePath() + "'", ioe);
            return false;
        }

        return true;
    }

    /**
     * Open the provided folder pLocalFolder. At first, the current operating
     * system is checked for beeing a Unix based system. In this case, a
     * dedicated script will be used for opening (setting permissions to 2770).
     * In all other cases or if the script-based approach fails, this call
     * prints an information log message and returns false.
     *
     * @param pFolder The folder to open (must be a directory)
     *
     * @return TRUE on success, FALSE on any error
     */
    public static boolean openFolder(File pFolder) {
        if (!pFolder.isDirectory()) {
            LOGGER.error("Provided argument 'pLocalFolder' ({}) must be a directory", pFolder);
            return false;
        }

        if (org.apache.commons.lang3.SystemUtils.IS_OS_UNIX && openFolderUnix(pFolder)) {
            return true;
        } else {
            LOGGER.info("Setting necessary permissions for folder {} is not supported or has failed. Maybe it is not necessary, so let's continue and pray.", pFolder);
            return false;
        }
    }

    /**
     * Change the ownership of the provided folder pFolder to the group with the
     * provided groupId. At first, the current operating system is checked for
     * beeing a Unix based system. In this case, a dedicated script will be used
     * for changing the ownership. In all other cases or if the script-based
     * approach fails, this call prints an information log message and returns
     * false.
     *
     * @param pFolder The folder to chgrp (must be a directory)
     * @param pGroupId The groupId that should own pFolder. This id can either
     * be a numeric or alphanumeric groupId in the underlying Unix environment.
     *
     * @return TRUE on success, FALSE on any error
     */
    public static boolean chgrpFolder(File pFolder, String pGroupId) {
        if (!pFolder.isDirectory()) {
            LOGGER.error("Provided argument 'pLocalFolder' ({}) must be a directory", pFolder);
            return false;
        }

        if (org.apache.commons.lang3.SystemUtils.IS_OS_UNIX && chgrpFolderUnix(pFolder, pGroupId)) {
            return true;
        } else {
            LOGGER.info("Setting necessary permissions for folder {} is not supported or has failed. Maybe it is not necessary, so let's continue and pray.", pFolder);
            return false;
        }
    }

    /**
     * Folder locking implementation for Unix based systems. At first, a script
     * responsible for locking (changing ownership to current user and access
     * permission to 700) is created under java.io.tmpdir. Afterwards, the
     * script is executed to lock pLocalFolder. If everything works fine, TRUE
     * is returned. In all other cases, FALSE is returned.
     *
     * @param pFolder The local folder which has to be locked
     *
     * @return TRUE on success, FALSE on error
     */
    private static boolean lockFolderUnix(File pFolder) {
        String scriptFile;
        try {
            scriptFile = generateLockScript();
        } catch (IOException e) {
            LOGGER.error("Failed to generate lock script", e);
            return false;
        }

        return applyScriptToFolder(scriptFile, pFolder.getAbsolutePath());
    }

    /**
     * Folder opening implementation for Unix based systems. At first, a script
     * responsible for opening (changing access permission to 2770) is created
     * under java.io.tmpdir. Afterwards, the script is executed to open
     * pLocalFolder. If everything works fine, TRUE is returned. In all other
     * cases, FALSE is returned.
     *
     * @param pFolder The local folder which has to be opened.
     *
     * @return TRUE on success, FALSE on error
     */
    private static boolean openFolderUnix(File pFolder) {
        String scriptFile;
        try {
            scriptFile = generateOpenScript();
        } catch (IOException e) {
            LOGGER.error("Failed to generate open script", e);
            return false;
        }

        return applyScriptToFolder(scriptFile, pFolder.getAbsolutePath());
    }

    /**
     * Folder opening implementation for Unix based systems. At first, a script
     * responsible for opening (changing access permission to 2770) is created
     * under java.io.tmpdir. Afterwards, the script is executed to open
     * pLocalFolder. If everything works fine, TRUE is returned. In all other
     * cases, FALSE is returned.
     *
     * @param pFolder The local folder which has to be opened.
     *
     * @return TRUE on success, FALSE on error
     */
    private static boolean chgrpFolderUnix(File pFolder, String pGroupId) {
        String scriptFile;
        try {
            scriptFile = generateChgrpScript();
        } catch (IOException e) {
            LOGGER.error("Failed to generate open script", e);
            return false;
        }

        return applyScriptToFolder(scriptFile, pFolder.getAbsolutePath(), pGroupId);
    }

    /**
     * Generate the lock script for Unix systems.
     *
     * @return The full path to the lock script.
     *
     * @throws IOException If the generation fails.
     */
    private static String generateLockScript() throws IOException {
        String scriptFile;
        FileWriter fout = null;
        try {
            scriptFile = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "lockFolder.sh");
            fout = new FileWriter(scriptFile, false);
            StringBuilder b = new StringBuilder();
            b.append("#!/bin/sh\n");
            b.append("echo Changing owner of $1 to `whoami`\n");
            b.append("chown `whoami` $1 -R\n");
            b.append("echo Changing access of $1 to 700\n");
            b.append("chmod 700 $1 -R\n");
            LOGGER.debug("Writing script data to '{}'", scriptFile);
            fout.write(b.toString());
            fout.flush();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {//ignore
                }
            }
        }
        return scriptFile;
    }

    /**
     * Generate the open script for Unix systems.
     *
     * @return The full path to the open script.
     *
     * @throws IOException If the generation fails.
     */
    private static String generateOpenScript() throws IOException {
        String scriptFile;
        FileWriter fout = null;
        try {
            scriptFile = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "stickFolder.sh");
            fout = new FileWriter(scriptFile, false);
            StringBuilder b = new StringBuilder();
            b.append("#!/bin/sh\n");
            b.append("echo Changing access of $1 to 2770\n");
            b.append("chmod 2770 $1 -R\n");
            LOGGER.debug("Writing script data to '{}'", scriptFile);
            fout.write(b.toString());
            fout.flush();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {//ignore
                }
            }
        }
        return scriptFile;
    }

    /**
     * Generate the chgrp script for Unix systems.
     *
     * @return The full path to the chgrp script.
     *
     * @throws IOException If the generation fails.
     */
    private static String generateChgrpScript() throws IOException {
        String scriptFile;
        FileWriter fout = null;
        try {
            scriptFile = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "chgrpFolder.sh");
            fout = new FileWriter(scriptFile, false);
            StringBuilder b = new StringBuilder();
            b.append("#!/bin/sh\n");
            b.append("echo Changing ownership of $2 to $1\n");
            b.append("chgrp $1 $2 -R\n");
            LOGGER.debug("Writing script data to '{}'", scriptFile);
            fout.write(b.toString());
            fout.flush();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {//ignore
                }
            }
        }
        return scriptFile;
    }

    /**
     * Lock a folder in a Unix system. Therefor, the script previously generated
     * by generateScript() is used to lock the provided folder 'pLocalFolder'.
     *
     * @param pLocalScriptFile The lock script.
     * @param pLocalFolder The folder to lock.
     *
     * @return TRUE if the locking succeeded.
     */
    private static boolean applyScriptToFolder(String pLocalScriptFile, String... pArguments) {
        BufferedReader brStdOut = null;
        BufferedReader brStdErr = null;
        try {
            String line;
            StringBuilder stdOut = new StringBuilder();
            StringBuilder stdErr = new StringBuilder();

            List<String> cmdArrayList = new ArrayList<>();
            cmdArrayList.add("sh");
            cmdArrayList.add(pLocalScriptFile);
            if (pArguments != null) {
                cmdArrayList.addAll(Arrays.asList(pArguments));
            }

            Process p = Runtime.getRuntime().exec(cmdArrayList.toArray(new String[cmdArrayList.size()]));

            brStdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            brStdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = brStdOut.readLine()) != null) {
                if (line.trim().length() > 1) {
                    stdOut.append(line).append("\n");
                }
            }
            brStdOut.close();
            while ((line = brStdErr.readLine()) != null) {
                if (line.trim().length() > 1) {
                    stdErr.append(line).append("\n");
                }
            }
            brStdErr.close();
            int result = p.waitFor();
            LOGGER.debug("Script finished execution with return code {}", result);
            if (stdOut.length() > 1) {
                LOGGER.debug("StdOut: {}", stdOut.toString());
            }
            if (stdErr.length() > 1) {
                LOGGER.warn("StdErr: {}", stdErr.toString());
            }
        } catch (IOException err) {
            LOGGER.error("Script execution failed", err);
            return false;
        } catch (InterruptedException err) {
            LOGGER.error("Script execution might have failed", err);
            return false;
        } finally {
            if (brStdErr != null) {
                try {
                    brStdErr.close();
                } catch (IOException ex) {
                }
            }
            if (brStdOut != null) {
                try {
                    brStdOut.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    /**
     * Wrapper for creating a symbolic link using java.nio
     *
     * @param pTargetFile The file the link will point to.
     * @param pLink The link location.
     *
     * @throws IOException if linking fails.
     */
    public static void createSymbolicLink(File pTargetFile, File pLink) throws IOException {
        Path target = Paths.get(pTargetFile.toURI());
        Path link = Paths.get(pLink.toURI());
        Files.createSymbolicLink(link, target);
    }

    /**
     * Perform cleanup of strings in order to make them usable as filenames.
     * Characters the cannot be used are converted to hex representation.
     * Directory separators are also converted.
     *
     * The contained code is part of Apache ActiveMQ 5.3.0 (see
     * http://grepcode.com/file/repository.springsource.com/org.apache.activemq/com.springsource.org.apache.kahadb/5.3.0/org/apache/kahadb/util/IOHelper.java)
     *
     * @param name The file or path name that should be cleaned.
     *
     * @return The cleaned string.
     */
    public static String toFileSystemSafeName(String name) {
        return toFileSystemSafeName(name, false);
    }

    /**
     * Perform cleanup of strings or paths in order to make them usable as
     * filenames. Characters the cannot be used are converted to hex
     * representation.
     *
     * The contained code is part of Apache ActiveMQ 5.3.0 (see
     * http://grepcode.com/file/repository.springsource.com/org.apache.activemq/com.springsource.org.apache.kahadb/5.3.0/org/apache/kahadb/util/IOHelper.java)
     *
     * @param name The file or path name that should be cleaned.
     * @param keepDirSeparators TRUE if directory separators should be kept. If
     * FALSE, directory separators are converted to hex.
     *
     * @return The cleaned string.
     */
    public static String toFileSystemSafeName(String name, boolean keepDirSeparators) {
        int maxFileLength = Integer.parseInt(System.getProperty("MaximumFileNameLength", "64"));
        int size = name.length();

        StringBuilder rc = new StringBuilder(size * 2);

        for (int i = 0; i < size; i++) {

            char c = name.charAt(i);

            boolean valid = c >= 'a' && c <= 'z';

            valid = valid || (c >= 'A' && c <= 'Z');

            valid = valid || (c >= '0' && c <= '9');

            valid = valid || (c == '_') || (c == '-') || (c == '.') || (c == '#')
                    || (keepDirSeparators && ((c == '/') || (c == '\\')));

            if (valid) {
                rc.append(c);

            } else // Encode the character using hex notation
            {
                rc.append('#');
                rc.append(Integer.toHexString(c).toUpperCase());
            }
        }

        String result = rc.toString();

        if (result.length() > maxFileLength) {
            result = result.substring(result.length() - maxFileLength, result.length());
        }

        return result;
    }
}
