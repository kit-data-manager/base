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
package edu.kit.dama.mdm.core.test;

import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import edu.kit.dama.mdm.core.exception.ConfigurationException;
import java.util.List;
import org.junit.Before;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.File;
import org.slf4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author hartmann-v
 */
public class MetaDataManagementInitTest {

  /**
   * File name of the original and valid config file.
   */
  private static final String DEFAULT_CONFIG_FILE = "datamanager.xml";
  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(MetaDataManagementHelper.class);

  /**
   * Manager holding all Information.
   */
  @BeforeClass
  public static void prepareClassForTests() {
    LOGGER.info("Prepare class 'MetaDataManagementTest' for Tests!");
    MetaDataManagementHelper.replaceConfig(null);
  }

  @AfterClass
  public static void releaseClassAfterTests() {
    LOGGER.info("All tests done!\nRelease class 'MetaDataManagementTest' for Tests!");
    releaseSingleton();
  }

  @Before
  public void prepareMethodForTest() {
    releaseSingleton();
  }

  private static void releaseSingleton() {
    try {
      MetaDataManagementHelper.replaceConfig(null);
    } catch (RuntimeException re) {
      // ignore maybe wrong configuration file.
    }
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidXML() {
    MetaDataManagementHelper.replaceConfig("InvalidXML.xml");
    MetaDataManagement.getMetaDataManagement();
    assertFalse(true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongElementNames() {
    MetaDataManagementHelper.replaceConfig("WrongElementNames.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    assertEquals(0, mdm.getAllImplementations().size());
  }

  @Test
  public void testRootNodeName() {
    MetaDataManagementHelper.replaceConfig("WrongRootNodeName.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    assertEquals(1, mdm.getAllImplementations().size());
  }

  @Test
  public void testMetaDataConfigMutipleImplementations() {
    MetaDataManagementHelper.replaceConfig("MultipleImplementations.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = "JPA";
    assertEquals(2, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(impl));
    assertEquals("JPA", impl);
    List<String> allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(2, allPersistenceUnits.size());
    assertEquals("MDM-Core", allPersistenceUnits.get(0));
    assertEquals("MDM-Core-Test", allPersistenceUnits.get(1));
    impl = "JPA2";
    assertTrue(mdm.getAllImplementations().contains(impl));
    allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(3, allPersistenceUnits.size());
    assertEquals("MDM-Core2", allPersistenceUnits.get(0));
    assertEquals("MDM-Core-Test2", allPersistenceUnits.get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPersistenceUnit() {
    MetaDataManagementHelper.replaceConfig("MultipleImplementations.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    mdm.getMetaDataManager("noValidPersistenceUnit");
    assertTrue(false);
  }

  @Test
  public void testMetaDataConfigMutipleImplementationsWithDefault() {
    MetaDataManagementHelper.replaceConfig("MultipleImplementations.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = mdm.getDefaultImplementation();
    assertEquals(2, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(impl));
    assertTrue(mdm.getAllImplementations().contains(impl));
    List<String> allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(3, allPersistenceUnits.size());
    assertEquals("MDM-Core2", allPersistenceUnits.get(0));
    assertEquals("MDM-Core-Test2", allPersistenceUnits.get(1));
    assertEquals("PU3", allPersistenceUnits.get(2));
    allPersistenceUnits = mdm.getAllPersistenceUnits();
    assertEquals(3, allPersistenceUnits.size());
    assertEquals("MDM-Core2", allPersistenceUnits.get(0));
    assertEquals("MDM-Core-Test2", allPersistenceUnits.get(1));
    assertEquals("PU3", allPersistenceUnits.get(2));
  }

  @Test(expected = ConfigurationException.class)
  public void testMetaDataConfigNotCorrectInterface() {
    MetaDataManagementHelper.replaceConfig("NotCorrectInterface.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
  }

  @Test(expected = ConfigurationException.class)
  public void testMetaDataConfigWrongImplementationClass() {
    MetaDataManagementHelper.replaceConfig("WrongImplementationClass.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
  }

  @Test
  public void testMetaDataConfigMissingPersistenceUnits() {
    MetaDataManagementHelper.replaceConfig("MissingPersistenceUnits.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = mdm.getAllImplementations().get(0);
    assertEquals(1, mdm.getAllImplementations().size());
    assertEquals("JPA", impl);
    List<String> allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(0, allPersistenceUnits.size());
  }

  @Test
  public void testMetaDataConfigOriginal() {
    MetaDataManagementHelper.replaceConfig("MetaDataManagementConfigOriginal.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = mdm.getAllImplementations().get(0);
    assertEquals(1, mdm.getAllImplementations().size());
    assertEquals("JPA", impl);
    List<String> allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(18, allPersistenceUnits.size());
    assertEquals("MDM-Core", allPersistenceUnits.get(0));
  }

  @Test
  public void testNoDefaultImplementation() {
    MetaDataManagementHelper.replaceConfig("NoDefaultImplementation.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = mdm.getDefaultImplementation(); // first implementation should be used as default!
    assertEquals("JPA", impl);
    assertEquals(2, mdm.getAllImplementations().size());
    List<String> allPersistenceUnits = mdm.getAllPersistenceUnits(impl);
    assertEquals(2, allPersistenceUnits.size());
    assertEquals("MDM-Core", allPersistenceUnits.get(0));
  }

  @Test
  public void testTwoDefaultImplementations() {
    MetaDataManagementHelper.replaceConfig("TwoDefaultImplementations.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    //this should work since revision 650...duplicates are ignored.
    assertTrue(true);
  }

  @Test
  public void testMetaDefaultImplementationAndUnit() {
    MetaDataManagementHelper.replaceConfig("MultipleImplementations.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = "JPA";
    String defaultImpl = "JPA2";
    String defaultUnitJPA = "MDM-Core";
    String defaultUnit = "MDM-Core-Test2";
    assertEquals(2, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(impl));
    assertEquals(defaultUnit, mdm.getDefaultPersistenceUnit());
    assertEquals(defaultImpl, mdm.getDefaultImplementation());
    assertEquals(defaultUnitJPA, mdm.getDefaultPersistenceUnit(impl));
  }

  @Test
  public void testMultiplePersistenceUnits() {
    MetaDataManagementHelper.replaceConfig("MultiplePersistenceUnitDefault.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = "JPA3";
    String defaultImpl = "JPA2";
    String defaultUnitJPA3 = "PU4";
    String defaultUnit = "MDM-Core-Test2";
    assertEquals(3, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(impl));
    assertEquals(defaultUnit, mdm.getDefaultPersistenceUnit());
    assertEquals(defaultImpl, mdm.getDefaultImplementation());
    assertEquals(defaultUnitJPA3, mdm.getDefaultPersistenceUnit(impl));
  }

  @Test
  public void testDefaultPersistenceUnitsWithoutDefaultImplementation() {
    MetaDataManagementHelper.replaceConfig("OnlyPersistenceUnitDefault.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl = "JPA2";
    String defaultImpl = "JPA";
    String defaultUnitJPA2 = "MDM-Core-Test2";
    String defaultUnit = "MDM-Core-Test";
    assertEquals(2, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(impl));
    assertEquals(defaultUnit, mdm.getDefaultPersistenceUnit());
    assertEquals(defaultImpl, mdm.getDefaultImplementation());
    assertEquals(defaultUnitJPA2, mdm.getDefaultPersistenceUnit(impl));
  }

  @Test
  public void testWrongDefaultPersistenceUnitsDefinitions() {
    MetaDataManagementHelper.replaceConfig("WrongPersistenceUnitDefault.xml");
    MetaDataManagement mdm = MetaDataManagement.getMetaDataManagement();
    String impl2 = "JPA2";
    String impl3 = "JPA3";
    String defaultImpl = "JPA";
    String defaultUnitJPA = "MDM-Core";
    String defaultUnitJPA2 = "MDM-Core2";
    String defaultUnitJPA3 = "MDM-Core3";
    assertEquals(3, mdm.getAllImplementations().size());
    assertTrue(mdm.getAllImplementations().contains(defaultImpl));
    assertTrue(mdm.getAllImplementations().contains(impl2));
    assertTrue(mdm.getAllImplementations().contains(impl3));
    assertEquals(defaultUnitJPA, mdm.getDefaultPersistenceUnit());
    assertEquals(defaultImpl, mdm.getDefaultImplementation());
    assertEquals(defaultUnitJPA2, mdm.getDefaultPersistenceUnit(impl2));
    assertEquals(defaultUnitJPA3, mdm.getDefaultPersistenceUnit(impl3));
  }

  private static void copyFile(File sourceFile, File destFile) throws IOException {
    if (!destFile.exists()) {
      destFile.createNewFile();
    }

    FileChannel source = null;
    FileChannel destination = null;

    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } finally {
      if (source != null) {
        source.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }
}
