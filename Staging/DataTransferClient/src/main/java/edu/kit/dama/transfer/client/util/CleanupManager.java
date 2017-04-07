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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @TODO Store cleanup information separately next to container
 * @author jejkal
 */
public class CleanupManager {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupManager.class);
    /**
     * The singleton instance
     */
    private static CleanupManager singleton = null;
    /**
     * The map of files to cleanup for each transfer, represented by a unique ID
     */
    private Map<String, List<File>> filesToCleanup = null;

    /**
     * Get the singleton instance
     *
     * @return The CleanupManager singleton
     */
    public static synchronized CleanupManager getSingleton() {
        if (singleton == null) {
            singleton = new CleanupManager();
        }
        return singleton;
    }

    /**
     * Default constructor (internal)
     */
    CleanupManager() {
        filesToCleanup = new HashMap<>();
    }

    /**
     * Add a file for cleanup
     *
     * @param pId The ID for the transfer
     * @param pFile The file to cleanup
     */
    public final void addFile(String pId, File pFile) {
        if (pId == null || pFile == null) {
            return;
        }
        LOGGER.debug("Registering file {} for cleanup", pFile);
        List<File> files = filesToCleanup.get(pId);
        if (files == null) {
            files = new LinkedList<>();
            filesToCleanup.put(pId, files);
        }
        if (!files.contains(pFile)) {
            files.add(pFile);
        }
    }

    /**
     * Add a file from cleanup
     *
     * @param pId The ID for the transfer
     * @param pFile The file not to cleanup
     */
    public final void removeFile(String pId, File pFile) {
        if (pId == null || pFile == null) {
            return;
        }
        List<File> files = filesToCleanup.get(pId);
        if (files != null) {
            files.remove(pFile);
        }
    }

    /**
     * Delete all files associated with transfer with the provided ID.
     *
     * @param pId The transfer ID for which registered files will be removed
     */
    public final void performCleanup(String pId) {
        if (pId == null) {
            return;
        }
        LOGGER.debug("Starting cleanup for ID {}", pId);
        List<File> files = filesToCleanup.get(pId);
        if (files == null) {
            LOGGER.debug(" * No cleanup necessary");
        } else {
            LOGGER.debug(" * Cleaning up {} file(s)", files.size());
            for (File file : files) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(file);
                            LOGGER.debug("Cleanup succeeded for directory {}", file);
                        } catch (IOException ioe) {
                            LOGGER.debug("Failed to cleanup directory " + file, ioe);
                        }
                    } else {
                        if (!FileUtils.deleteQuietly(file)) {
                            LOGGER.debug("Failed to cleanup file {}", file);
                        } else {
                            LOGGER.debug("Cleanup succeeded for file {}", file);
                        }
                    }
                } else {
                    LOGGER.debug("The file {} scheduled for cleanup does not exist. Skipping entry.", file);
                }
            }
        }
        LOGGER.debug("Cleanup finished");
    }
}
