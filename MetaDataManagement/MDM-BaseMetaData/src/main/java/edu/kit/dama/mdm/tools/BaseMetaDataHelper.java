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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.Study;

/**
 * Class for creating and searching for specific instances.
 *
 * @author hartmann-v
 */
public class BaseMetaDataHelper {

  private static final String DUMMY_INVESTIGATION_LABEL = "Dummy investigation for study: ";
  private static final String DUMMY_DIGITAL_OBJECT_LABEL = "Dummy digital object for investigation: ";

// <editor-fold defaultstate="collapsed" desc="Declaration of the namespaces">
  /**
   * prefix of namespaces for kit data manager metadata.
   */
  private static final String PREFIX_DAMA_NAMESPACE = "http://datamanager.kit.edu/dama/";

  /**
   * signature for base meta data (digital object).
   */
  private static final String POSTFIX_DAMA_NAMESPACE_BASEMETADATA = "basemetadata/";

  /**
   * signature for base meta data (digital object).
   * @deprecated Use METS format instead (since KIT Data Manager 1.4)
   */
  @Deprecated 
  private static final String POSTFIX_DAMA_NAMESPACE_METADATA = "metadata/";
  /**
   * signature for base meta data (digital object).
   */
  private static final String POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION = "dataorganization/";
  /**
   * version of the namespaces. (all dama namespaces should use the same version.
   */
  private static final String ACTUAL_VERSION = "2015-08";
  /**
   * version of the namespaces. (all dama namespaces should use the same version.
   */
  private static final String VERSION_2012 = "2012-04";
  /**
   * version of the basemetadata. (Attention: all dama namespaces should use the same version.
   */
  private static final String VERSION_BASEMETADATA = ACTUAL_VERSION;
  /**
   * version of the basemetadata. (Attention: all dama namespaces should use the same version.
   * @deprecated Use METS format instead (since KIT Data Manager 1.4)
   */
  @Deprecated
  private static final String VERSION_METADATA = VERSION_2012;
  /**
   * version of the dataorganization. (Attention: all dama namespaces should use the same version.
   */
  private static final String VERSION_DATAORGANIZATION = ACTUAL_VERSION;
  /**
   * element name of the metadata.
   */
  public static final String BASEMETADATA_ROOT_ELEMENT = "basemetadata";
  /**
   * element name of data organization.
   */
  public static final String DATAORGANIZATION_ROOT_ELEMENT = "dataOrganization";
  /**
   * element name of the content metadata.
   */
  public static final String CONTENTMETADATA_ROOT_ELEMENT = "contentmd";
  /**
   * prefix for the base metadata namespace.
   */
  public static final String DAMA_NAMESPACE_PREFIX = "bmd";
  /**
   * prefix for the community specific.
   */
  public static final String CSMD_NAMESPACE_PREFIX = "csmd";
  /**
   * Namespace for the base meta data (digital object).
   * http://datamanager.kit.edu/dama/basemetadata/2015-08/basemetadata.xsd)
   */
  public static final String DAMA_NAMESPACE_BASEMETADATA = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_BASEMETADATA;
  /**
   * Namespace for the meta data.
   * http://datamanager.kit.edu/dama/metadata/2012-04/metadata.xsd)
   * @deprecated Use METS format instead (since KIT Data Manager 1.4)
   */
  @Deprecated 
  public static final String DAMA_NAMESPACE_METADATA = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_METADATA + VERSION_METADATA;
  /**
   * XSD file for basemetadata.
   */
  public static final String BASEMETADATA_XSD = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_BASEMETADATA + VERSION_BASEMETADATA + "/basemetadata.xsd";
  /**
   * Namespace for the data organization (digital object).
   * http://datamanager.kit.edu/dama/dataorganization/)
   */
  public static final String DAMA_NAMESPACE_DATAORGANIZATION = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION;
  /**
   * XSD file for data organization.
   */
  public static final String DATAORGANIZATION_XSD = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION + VERSION_DATAORGANIZATION + "/dataorganization.xsd";
// </editor-fold>

  /**
   * Create a dummy instance of Investigation containing one dummy instance of a
   * DigitalObject which are NOT visible. Instance is added to study.
   * <b>Attention: </b>Do the persist afterwards.
   *
   * @param study instance which will contain the investigation and the digital
   * object.
   */
  public static void createDummyInvestigation(Study study) {
    Investigation investigation;
    if (study != null) {
      investigation = Investigation.factoryNewInvestigation();
      investigation.setTopic(DUMMY_INVESTIGATION_LABEL + study.getTopic());
      investigation.setVisible(Boolean.FALSE);

      createDummyDigitalObject(investigation);

      study.addInvestigation(investigation);
    }
  }

  /**
   * Create a dummy instance of DigitalObject which is NOT visible. Instance is
   * added to investigation. <b>Attention: </b>Do the persist afterwards.
   *
   * @param investigation instance which will contain the digital object.
   */
  public static void createDummyDigitalObject(Investigation investigation) {
    DigitalObject digitalObject;
    if (investigation != null) {
      digitalObject = DigitalObject.factoryNewDigitalObject(DUMMY_DIGITAL_OBJECT_LABEL + investigation.getTopic());
      digitalObject.setVisible(Boolean.FALSE);

      investigation.addDataSet(digitalObject);
    }
  }

  /**
   * Returns the base metadata schema which is the most general schema
   * applicable to all digital objects stored in KIT Data Manager.
   *
   * @return The base metadata schema.
   */
  public static final MetaDataSchema getBaseMetaDataSchema() {
    return new MetaDataSchema(DAMA_NAMESPACE_PREFIX, DAMA_NAMESPACE_BASEMETADATA);
  }
}
