/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.util.release.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.model.Dependency;

/**
 *
 * @author jejkal
 */
public class ReleaseConfiguration {

    private String configurationName;
    private String sourceDirectory;
    private String destinationDirectory;
    private String[] inputDirectories = new String[]{};
    private String[] inputFiles = new String[]{};

    private String[] directoriesToIgnore = new String[]{};
    private String[] filesToIgnore = new String[]{};
    private String[] filesToRemove = new String[]{};

    private String[] modulesToRemove = new String[]{};
    private String[] profilesToRemove = new String[]{};
    private String[] pluginsToRemove = new String[]{};
    private String[] propertiesToRemove = new String[]{};
    private final Map<String, String> propertiesToSet = new HashMap<>();
    private Dependency[] localDependencies = new Dependency[]{};

    private String scmConnection;
    private String scmUrl;

    private boolean removeDevelopers = true;
    private boolean removeDistributionManagement = true;
    private boolean removeCiManagement = true;

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public String[] getInputDirectories() {
        return inputDirectories;
    }

    public void setInputDirectories(String[] inputDirectories) {
        if (inputDirectories != null) {
            this.inputDirectories = inputDirectories;
        }
    }

    public String[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(String[] inputFiles) {
        if (inputFiles != null) {
            this.inputFiles = inputFiles;
        }
    }

    public String[] getDirectoriesToIgnore() {
        return directoriesToIgnore;
    }

    public void setDirectoriesToIgnore(String[] directoriesToIgnore) {
        if (directoriesToIgnore != null) {
            this.directoriesToIgnore = directoriesToIgnore;
        }
    }

    public String[] getFilesToIgnore() {
        return filesToIgnore;
    }

    public void setFilesToIgnore(String[] filesToIgnore) {
        if (filesToIgnore != null) {
            this.filesToIgnore = filesToIgnore;
        }
    }

    public String[] getFilesToRemove() {
        return filesToRemove;
    }

    public void setFilesToRemove(String[] filesToRemove) {
        if (filesToRemove != null) {
            this.filesToRemove = filesToRemove;
        }
    }

    public String[] getModulesToRemove() {
        return modulesToRemove;
    }

    public void setModulesToRemove(String[] modulesToRemove) {
        if (modulesToRemove != null) {

            this.modulesToRemove = modulesToRemove;
        }
    }

    public String[] getProfilesToRemove() {
        return profilesToRemove;
    }

    public void setProfilesToRemove(String[] profilesToRemove) {
        if (profilesToRemove != null) {
            this.profilesToRemove = profilesToRemove;
        }
    }

    public String[] getPluginsToRemove() {
        return pluginsToRemove;
    }

    public void setPluginsToRemove(String[] pluginsToRemove) {
        if (pluginsToRemove != null) {
            this.pluginsToRemove = pluginsToRemove;
        }
    }

    public String[] getPropertiesToRemove() {
        return propertiesToRemove;
    }

    public void setPropertiesToRemove(String[] propertiesToRemove) {
        if (propertiesToRemove != null) {
            this.propertiesToRemove = propertiesToRemove;
        }
    }

    public Dependency[] getLocalDependencies() {
        return localDependencies;
    }

    public void setLocalDependencies(Dependency[] localDependencies) {
        if (localDependencies != null) {
            this.localDependencies = localDependencies;
        }
    }

    public void setScmConnection(String scmConnection) {
        this.scmConnection = scmConnection;
    }

    public String getScmConnection() {
        return scmConnection;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public boolean isRemoveDevelopers() {
        return removeDevelopers;
    }

    public void setRemoveDevelopers(boolean removeDevelopers) {
        this.removeDevelopers = removeDevelopers;
    }

    public boolean isRemoveDistributionManagement() {
        return removeDistributionManagement;
    }

    public void setRemoveDistributionManagement(boolean removeDistributionManagement) {
        this.removeDistributionManagement = removeDistributionManagement;
    }

    public boolean isRemoveCiManagement() {
        return removeCiManagement;
    }

    public void setRemoveCiManagement(boolean removeCiManagement) {
        this.removeCiManagement = removeCiManagement;
    }

    public void addProperty(String key, String value) {
        propertiesToSet.put(key, value);
    }

    public Map<String, String> getPropertiesToSet() {
        return propertiesToSet;
    }
}
