/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.ui.simon.util;

import edu.kit.dama.ui.simon.impl.AbstractProbe;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class SimonConfigurator implements ConfigurationListener, FileAlterationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimonConfigurator.class);
    private static final SimonConfigurator SINGLETON = new SimonConfigurator();
    private List<AbstractProbe> probes = new LinkedList<>();
    private List<PropertiesConfiguration> configs;
    private File configPath = null;
    private FileAlterationObserver observer = null;
    private FileAlterationMonitor configMontitor = new FileAlterationMonitor(5000);

    SimonConfigurator() {
        this.configs = new LinkedList<>();
        try {
            LOGGER.debug("Starting configuration monitor");
            configMontitor.start();
        } catch (Exception e) {
            LOGGER.info("Failed to start configuration monitor. Configuration changed won't be tracked.");
        }
    }

    public static SimonConfigurator getSingleton() {
        return SINGLETON;
    }

    public void setConfigLocation(File pPath) throws ConfigurationException {
        if (pPath == null) {
            throw new IllegalArgumentException("Argument pPath must not be null");
        }
        if (!pPath.exists() || !pPath.isDirectory()) {
            throw new ConfigurationException("Argument pPath must be an existing directory");
        }

        if (!pPath.equals(configPath)) {
            LOGGER.debug("Reloading configuration");
            configPath = pPath;
            probes.clear();
            configs.clear();
            configure();
        } else {
            LOGGER.debug("New config file is equal to current config file. Skipping reconfiguration.");
        }

        if (observer != null) {
            LOGGER.debug("Stopping directory observer");
            try {
                LOGGER.debug(" - Removing observer from monitor");
                configMontitor.removeObserver(observer);
                LOGGER.debug(" - Destroying observer");
                observer.destroy();
            } catch (Exception e) {
                LOGGER.info("Failed to stop observer, ignoring this.", e);
            }
            observer = null;
        }

        LOGGER.debug("Setting up directory observer for current configuration path {}", pPath);
        observer = new FileAlterationObserver(pPath);
        try {
            LOGGER.debug(" - Initializing directory observer");
            observer.initialize();
            LOGGER.debug(" - Directory observer successfully initialized. Adding listener.");
            observer.addListener(this);
            configMontitor.addObserver(observer);
        } catch (Exception e) {
            LOGGER.info("Failed to initialize directory observer. Configuration changes won't be tracked.", e);
            observer = null;
        }

    }

    public final void recheckConfigurations() throws ConfigurationException {
        LOGGER.debug("Reloading configuration");
        probes.clear();
        configs.clear();
        configure();
    }

    private void configure() throws ConfigurationException {
        if (!configs.isEmpty()) {
            configs.clear();
        }

        for (File f : configPath.listFiles()) {
            LOGGER.debug("Loading new probe configuration from file {}", f);
            try {
                LOGGER.debug(" - Adding configuration file");
                configs.add(new PropertiesConfiguration(f));
            } catch (ConfigurationException ex) {
                LOGGER.info("File " + f + " seems not to be a valid probe configuration from file. Possible misconfiguration of simon.location?", ex);
            }
        }

        //load the configuration
        setupProbes();
    }

    private void setupProbes() {
        for (PropertiesConfiguration config : configs) {
            try {
                String probeClass = config.getString("probe.class");
                String probeName = config.getString("probe.name", "Unnamed Probe (" + config.getFile().getName() + ")");
                String probeCategory = config.getString("probe.category");
                if (probeClass != null) {
                    LOGGER.debug(" - Creating instance for probe {}", probeName);
                    Class clazz = Class.forName(probeClass);
                    AbstractProbe probe = (AbstractProbe) clazz.newInstance();
                    probe.setName(probeName);
                    probe.setCategory(probeCategory);
                    LOGGER.debug(" - Configuring probe instance");
                    if (probe.configure(config)) {
                        LOGGER.debug("Probe successfully configured.");
                    } else {
                        LOGGER.info("Failed to configure probe " + probeName + ". Ignoring probe.");
                    }
                    probes.add(probe);
                } else {
                    LOGGER.info("Property probe.class for probe " + probeName + " not defined. Ignoring probe.");
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.info("Failed to instantiate probe from file " + config.getPath() + ". Ignoring probe.", ex);
            }
        }
    }

    public AbstractProbe[] getProbes() {
        return probes.toArray(new AbstractProbe[probes.size()]);
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        try {
            configure();
        } catch (ConfigurationException ex) {
            LOGGER.info("Unable to reconfigure Simon. Continuing with old configuration.", ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="FileAlternationListener methods">
    @Override
    public void onStart(FileAlterationObserver fao) {
        //triggered on every check cycle...do nothing
    }

    @Override
    public void onDirectoryCreate(File file) {
        //do nothing
        LOGGER.debug("Directory observer detected directory creation. Ignoring.");
    }

    @Override
    public void onDirectoryChange(File file) {
        //don't know yet
        LOGGER.debug("Directory observer detected directory change. Ignoring.");
    }

    @Override
    public void onDirectoryDelete(File file) {
        try {
            recheckConfigurations();
        } catch (ConfigurationException ex) {
            LOGGER.info("Failed to recheck configurations in 'onDirectoryDelete' event", ex);
        }
    }

    @Override
    public void onFileCreate(File file) {
        //add probe
        try {
            recheckConfigurations();
        } catch (ConfigurationException ex) {
            LOGGER.info("Failed to recheck configurations in 'onFileCreate' event for file " + file, ex);
        }
    }

    @Override
    public void onFileChange(File file) {
        //reconfigure probe
        try {
            recheckConfigurations();
        } catch (ConfigurationException ex) {
            LOGGER.info("Failed to recheck configurations in 'onFileChange' event for file " + file, ex);
        }
    }

    @Override
    public void onFileDelete(File file) {
        //remove probe
        try {
            recheckConfigurations();
        } catch (ConfigurationException ex) {
            LOGGER.info("Failed to recheck configurations in 'onFileDelete' event for file " + file, ex);
        }
    }

    @Override
    public void onStop(FileAlterationObserver fao) {
        //triggered on every check cycle...do nothing
    }

    //</editor-fold>
//  public static void main(String[] args) throws Exception {
//    SimonConfigurator.getSingleton().setConfigLocation(new File("d:/simon"));
//
//    for (AbstractProbe p : SimonConfigurator.getSingleton().getProbes()) {
//      System.out.println(p.getCurrentStatus());
//      p.refreshProbeStatus();
//      System.out.println(p.getCurrentStatus());
//    }
//  }
}
