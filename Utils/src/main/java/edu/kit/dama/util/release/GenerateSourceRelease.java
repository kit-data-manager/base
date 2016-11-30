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
package edu.kit.dama.util.release;

import edu.kit.dama.util.release.config.ReleaseConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 * @author mf6319
 */
public class GenerateSourceRelease {

    final static String MAVEN_REPOSITORY = "/Users/jejkal/.m2/repository";

    private enum RELEASE_TYPE {
        KITDM,
        GENERIC_CLIENT,
        BARE_DEMO
    }

    //For KIT DM Source Release
    //private final static String[] sourceFolders = new String[]{"Authorization", "Commons", "Core", "DataOrganization", "DataWorkflow", "Documentation", "MetaDataManagement", "RestInterfaces", "Scheduler", "Staging", "UserInterface", "Utils"};
    //For BaReDemo Source Release
    //private final static String[] sourceFolders = new String[]{"src"};
    //Common for all
    // private final static String[] foldersToIgnore = new String[]{"target", "srcRelease"};
    // private final static String[] filesToIgnore = new String[]{"nbactions*.xml", "nb-configuration.xml", "dependency-reduced-pom.xml", "filter.ipejejkal2.properties"};
    public static void generateSourceRelease(ReleaseConfiguration config) throws IOException, XmlPullParserException {
        File source = new File(config.getSourceDirectory());
        File destination = new File(config.getDestinationDirectory(), "srcRelease");
        destination.mkdirs();

        copySources(source, destination, config);
        checkLicenseHeaders(destination);
        updatePom(destination, config);
    }

    public static void copySources(File source, File destination, final ReleaseConfiguration config) throws IOException {
        for (String folder : config.getInputDirectories()) {
            File input = new File(source, folder);
            if (!input.exists()) {
                //warn
                continue;
            }
            new File(destination, folder).mkdirs();
            FileUtils.copyDirectory(input, new File(destination, folder), new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    //check folders to ignore
                    for (String folder : config.getDirectoriesToIgnore()) {
                        if (pathname.isDirectory() && folder.equals(pathname.getName())) {
                            return false;
                        }
                    }

                    //check files to ignore
                    for (String file : config.getFilesToIgnore()) {
                        if (pathname.isFile() && new WildcardFileFilter(file).accept(pathname)) {
                            return false;
                        }
                    }

                    return true;
                }
            });
        }

        for (String file : config.getFilesToRemove()) {
            FileUtils.deleteQuietly(new File(destination, file));
        }

        for (String file : config.getInputFiles()) {
            File input = new File(source, file);
            if (input.exists()) {
                FileUtils.copyFile(new File(source, file), new File(destination, file));
            } else {
                //warn
            }
        }
    }

    public static void checkLicenseHeaders(File destination) throws IOException {
        //check license headers
        Collection<File> files = FileUtils.listFiles(destination, new String[]{"java", "xml"}, true);
        for (File f : files) {
            boolean haveLicense = false;
            try (BufferedReader bf = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = bf.readLine()) != null) {
                    if (line.contains("Licensed under the Apache License")) {
                        haveLicense = true;
                        break;
                    }
                }
            }
            if (!haveLicense) {
                System.out.println("File  " + f + " seems to have no/an invalid license header.");
            }
        }
    }

    public static void updatePom(File destination, ReleaseConfiguration config) throws IOException, XmlPullParserException {
        File pomfile = new File(destination, "pom.xml");

        FileUtils.copyFile(pomfile, new File(destination, "pom.original.xml"));
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        FileReader reader = new FileReader(pomfile);
        Model model = mavenreader.read(reader);
        model.setPomFile(pomfile);

        if (config.isRemoveDevelopers()) {
            model.setDevelopers(new ArrayList<Developer>());
        }

        if (config.isRemoveDistributionManagement()) {
            model.setDistributionManagement(null);
        }

        if (config.isRemoveCiManagement()) {
            model.setCiManagement(null);
        }

        if (config.getScmConnection() != null || config.getScmUrl() != null) {
            Scm scm = new Scm();
            scm.setConnection(config.getScmConnection());
            scm.setUrl(config.getScmUrl());
            model.setScm(scm);
        }

        for (final String profile : config.getProfilesToRemove()) {

            Profile toRemove = (Profile) org.apache.commons.collections.CollectionUtils.find(model.getProfiles(), new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return profile.equals(((Profile) o).getId());
                }
            });

            if (toRemove != null) {
                model.getProfiles().remove(toRemove);
            }
        }

        for (final String plugin : config.getPluginsToRemove()) {

            Plugin toRemove = (Plugin) org.apache.commons.collections.CollectionUtils.find(model.getBuild().getPlugins(), new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return plugin.equals(((Plugin) o).getArtifactId());
                }
            });

            if (toRemove != null) {
                model.getBuild().getPlugins().remove(toRemove);
            }
        }

        for (final String module : config.getModulesToRemove()) {

            String toRemove = (String) org.apache.commons.collections.CollectionUtils.find(model.getModules(), new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return module.equals((String) o);
                }
            });

            if (toRemove != null) {
                model.getModules().remove(toRemove);
            }
        }

        for (String property : config.getPropertiesToRemove()) {
            model.getProperties().remove(property);
        }

        Set<Entry<String, String>> entries = config.getPropertiesToSet().entrySet();

        for (Entry<String, String> entry : entries) {
            model.getProperties().put(entry.getKey(), entry.getValue());
        }

        if (config.getLocalDependencies().length != 0) {
            //add local dependencies
            for (Dependency dependency : config.getLocalDependencies()) {
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                String version = dependency.getVersion();
                String dependencyPath = groupId.replaceAll("\\.", File.separator) + File.separator + artifactId + File.separator + version;
                String mavenRepoPath = MAVEN_REPOSITORY + File.separator + dependencyPath;
                String localRepoPath = config.getDestinationDirectory() + File.separator + "srcRelease" + File.separator + "libs" + File.separator + dependencyPath;
                if (!new File(localRepoPath).mkdirs() && !new File(localRepoPath).exists()) {
                    throw new IOException("Failed to create local repository path at " + localRepoPath);
                }
                String artifactFileName = artifactId + "-" + version + ".jar";
                String pomFileName = artifactId + "-" + version + ".pom";

                File artifact = new File(mavenRepoPath, artifactFileName);
                File pom = new File(mavenRepoPath, pomFileName);
                if (artifact.exists() && pom.exists()) {
                    FileUtils.copyFile(artifact, new File(localRepoPath, artifactFileName));
                    FileUtils.copyFile(pom, new File(localRepoPath, pomFileName));
                } else {
                    throw new IOException("Dependency " + groupId + ":" + artifactId + ":" + version + " not found at " + mavenRepoPath);
                }
            }

            //check local repo                
            boolean haveLocalRepo = false;
            for (Repository repository : model.getRepositories()) {
                String repoUrl = repository.getUrl();
                URL u = new URL(repoUrl);
                if ("file".equals(u.getProtocol())) {
                    haveLocalRepo = true;
                    break;
                    /**
                     * <repository>
                     * <id>localRepository</id>
                     * <url>file://${basedir}/${root.relative.path}/libs</url>
                     * </repository>
                     */
                }
            }

            if (!haveLocalRepo) {
                //add local repo
                Repository localRepository = new Repository();
                localRepository.setId("localRepository");
                localRepository.setUrl("file://${basedir}/lib/");
                localRepository.setName("Local file repository");
                model.getRepositories().add(0, localRepository);
            }
        }

        // MavenProject project = new MavenProject(model);
        //check parent (fail if exists)
        //
        MavenXpp3Writer mavenwriter = new MavenXpp3Writer();
        mavenwriter.write(new FileOutputStream(new File(destination, "pom.xml")), model);
    }

    public static ReleaseConfiguration getGenericRepoClientSourceReleaseConfig(String source, String destination) {
        ReleaseConfiguration config = new ReleaseConfiguration();
        config.setConfigurationName("GenericClient");
        config.setSourceDirectory("/Users/jejkal/NetBeansProjects/KITDM_EXT/tags/GenericRepoClient-1.4");
        config.setDestinationDirectory("/Users/jejkal/NetBeansProjects/KITDM_EXT/trunk/GenericRestClient");
        //config.setScmConnection("https://github.com/kit-data-manager/genericRepoClient");
        config.setScmUrl("https://github.com/kit-data-manager/genericRepoClient");
        config.setInputDirectories(new String[]{"src", "lib"});
        config.setInputFiles(new String[]{"pom.xml"});
        config.setFilesToIgnore(new String[]{"nbactions*.xml", "nb-configuration.xml", "dependency-reduced-pom.xml", "filter.ipejejkal2.properties", "filter.bess.properties", "filter.nsc.properties", "filter.anka.properties", "filter.vm.properties"});
        config.setDirectoriesToIgnore(new String[]{"target", "srcRelease"});
        config.setRemoveDevelopers(true);
        config.setRemoveDistributionManagement(true);
        Dependency d1 = new Dependency();
        d1.setGroupId("edu.kit.cmdline");
        d1.setArtifactId("CommandlineTools");
        d1.setVersion("1.1");
        Dependency d2 = new Dependency();
        d2.setGroupId("edu.kit");
        d2.setArtifactId("ADALAPI");
        d2.setVersion("2.3");
        Dependency d3 = new Dependency();
        d3.setGroupId("au.edu.apsr");
        d3.setArtifactId("mtk");
        d3.setVersion("1.1");
        config.setLocalDependencies(new Dependency[]{d1, d2});
        config.addProperty("group", "release");
        return config;
    }

    public static Dependency factoryDependency(String groupId, String artifactId, String version) {
        Dependency d = new Dependency();
        d.setGroupId(groupId);
        d.setArtifactId(artifactId);
        d.setVersion(version);
        return d;
    }

    public static ReleaseConfiguration getKITDMSourceReleaseConfig(String source, String destination) {
        ReleaseConfiguration config = new ReleaseConfiguration();
        config.setConfigurationName("KIT Data Manager");
        config.setSourceDirectory(source);
        config.setDestinationDirectory(destination);

        config.setScmUrl("https://github.com/kit-data-manager/base");
        config.setScmConnection("https://github.com/kit-data-manager/base");
        config.setInputDirectories(new String[]{"Authorization", "Commons", "Core", "DataOrganization", "DataOrganization_Neo4j", "DataWorkflow", "Documentation", "MetaDataManagement", "RestInterfaces", "Samples", "Scheduler", "Staging", "src/test", "UserInterface", "Utils"});

        config.setInputFiles(new String[]{"pom.xml"});
        config.setFilesToIgnore(new String[]{"nbactions*.xml", "nb-configuration.xml", "dependency-reduced-pom.xml", "filter.ipejejkal2.properties", "filter.bess.properties", "filter.nsc.properties", "filter.anka.properties", "filter.vm.properties", "migrationObject.xml"});
        config.setFilesToRemove(new String[]{"Samples/src/main/resources/META-INF/persistence.xml", "Samples/src/main/resources/logback.xml", "Utils/src/main/resources/logback.xml", "Utils/src/main/resources/META-INF/persistence.xml"});

        config.setDirectoriesToIgnore(new String[]{"target", "srcRelease", "metadata", ".settings"});

        config.setModulesToRemove(new String[]{"FunctionalTests"});
        config.setPropertiesToRemove(new String[]{"firstName", "lastName", "nameId"});
        config.setRemoveDevelopers(true);
        config.setRemoveDistributionManagement(true);
        Dependency commandlineTools = factoryDependency("edu.kit.cmdline", "CommandlineTools", "1.1");
        Dependency adalapi = factoryDependency("edu.kit", "ADALAPI", "2.3");
        Dependency mail = factoryDependency("org.fzk.globus", "mail", "4.0.8");
        Dependency soton = factoryDependency("org.fzk.globus", "soton-hicog", "4.0.2");
        Dependency gridutil = factoryDependency("org.fzk.ipe", "grid-util", "2.1");
        Dependency tools = factoryDependency("org.fzk.ipe", "Tools", "1.5");
        Dependency mtk = factoryDependency("au.edu.apsr", "mtk", "1.1");

        config.setLocalDependencies(new Dependency[]{commandlineTools, adalapi, mail, soton, gridutil, tools, mtk});
        return config;
    }

    public static ReleaseConfiguration getBaReDemoSourceReleaseConfig(String source, String destination) {
        ReleaseConfiguration config = new ReleaseConfiguration();
        config.setConfigurationName("Basic Repository Demonstrator");
        config.setSourceDirectory(source);
        config.setDestinationDirectory(destination);

        config.setScmUrl("https://github.com/kit-data-manager/basic-repository-demonstrator");
        config.setScmConnection("https://github.com/kit-data-manager/basic-repository-demonstrator");
        config.setInputDirectories(new String[]{"Docker", "libs", "src"});

        config.setInputFiles(new String[]{"pom.xml", "makeDist.sh",});
        config.setFilesToIgnore(new String[]{"nbactions*.xml", "nb-configuration.xml", "dependency-reduced-pom.xml", "filter.ipejejkal.properties"});

        config.setDirectoriesToIgnore(new String[]{"target", "srcRelease", ".settings"});

        config.setPropertiesToRemove(new String[]{"firstName", "lastName", "nameId"});
        config.setPluginsToRemove(new String[]{"maven-release-plugin", "maven-surefire-plugin"});
        config.setRemoveDevelopers(true);
        config.setRemoveDistributionManagement(true);
        Dependency adminui = factoryDependency("edu.kit.dama", "AdminUI", "1.3");
        config.setLocalDependencies(new Dependency[]{adminui});
        return config;
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: GenerateSourceRelease TYPE SOURCE DESTINATION");
            System.err.println("");
            System.err.println("TYPE\tThe release type. Must be one of KITDM, GENERIC_CLIENT or BARE_DEMO");
            System.err.println("SOURCE\tThe source folder containing all sources for the selected release type.");
            System.err.println("DESTINATION\tThe destination folder where all sources of the release are placed.");
            System.exit(1);
        }

        String releaseType = args[0];
        RELEASE_TYPE type = RELEASE_TYPE.KITDM;
        try {
            type = RELEASE_TYPE.valueOf(releaseType);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid release type. Valid relase types arguments are KITDM, GENERIC_CLIENT or BARE_DEMO");
            System.exit(1);
        }

        String source = args[1];
        String destination = args[2];
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        if ((sourceFile.exists() && !sourceFile.isDirectory()) || (destinationFile.exists() && !destinationFile.isDirectory())) {
            System.err.println("Either source or destination are no directories.");
            System.exit(1);
        }

        if (!sourceFile.exists() || !sourceFile.canRead()) {
            System.err.println("Source either does not exist or is not readable.");
            System.exit(1);
        }

        if ((destinationFile.exists() && !sourceFile.canWrite()) || (!destinationFile.exists() && !destinationFile.mkdirs())) {
            System.err.println("Destination is either not writable or cannot be created.");
            System.exit(1);
        }

        ReleaseConfiguration config = null;
        switch (type) {
            case KITDM:
                config = getKITDMSourceReleaseConfig(source, destination);
                break;
            case GENERIC_CLIENT:
                config = getGenericRepoClientSourceReleaseConfig(source, destination);
                break;
            case BARE_DEMO:
                config = getBaReDemoSourceReleaseConfig(source, destination);
                break;

        }

        generateSourceRelease(config);

        System.out.println("Generating Release finished.");
        System.out.println("Please manually check pom.xml:");
        System.out.println(" - Remove profiles");
        System.out.println(" - Update links to SCM, ciManagement and internal repositories");
    }
}
