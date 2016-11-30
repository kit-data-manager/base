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
package edu.kit.dama.mdm.content.mets.util;

/**
 * Declaration of all namespaces needed by METS.
 *
 * @author hartmann-v
 */
public class MetsNamespaceDefinition {

  /**
   * URL to METS profile.
   */
  public static final String KITDM_PROFILE = "http://datamanager.kit.edu/dama/metadata/2016-08/Metadata4AppliedSciences-METS-profile.xml";

  // <editor-fold defaultstate="collapsed" desc="XSI">
  /**
   * Namespace of schema definition.
   */
  public static final String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";
  /**
   * Defining prefix for schema definition.
   */
  public static final String XSI_NAMESPACE_PREFIX = "xmlns:xsi";
  /**
   * Defining schema location.
   */
  public static final String SCHEMALOCATION = "xsi:schemaLocation";
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="dublin core">
  /**
   * Namespace for OAI Dublin Core.
   */
  public static final String OAI_DUBLIN_CORE_NAMESPACE = "http://www.openarchives.org/OAI/2.0/oai_dc/";
  /**
   * Schema definition for OAI Dublin Core.
   */
  public static final String OAI_DUBLIN_CORE_XSD = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Namespaces Basemetadata & Dataorganization">
  /**
   * prefix of namespaces for kit data manager metadata.
   */
  private static final String PREFIX_DAMA_NAMESPACE = "http://datamanager.kit.edu/dama/";

  /**
   * signature for base meta data (digital object).
   */
  private static final String POSTFIX_DAMA_NAMESPACE_BASEMETADATA = "basemetadata";
  /**
   * signature for base meta data (digital object).
   */
  private static final String POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION = "dataorganization";
  
  /**
   * Namespace for the base meta data (digital object).
   * http://datamanager.kit.edu/dama/basemetadata/2015-08/basemetadata.xsd)
   */
  public static final String DAMA_NAMESPACE_BASEMETADATA = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_BASEMETADATA;
  /**
   * Namespace for the data organization (digital object).
   * http://datamanager.kit.edu/dama/dataorganization/)
   */
  public static final String DAMA_NAMESPACE_DATAORGANIZATION = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Prefixes for base metadata and community specific metadata">
  /**
   * prefix for the base metadata namespace.
   */
  public static final String DAMA_NAMESPACE_PREFIX = "bmd";
  /**
   * prefix for the community specific.
   */
  public static final String CSMD_NAMESPACE_PREFIX = "csmd";
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="version strings">
  /**
   * version of the namespaces. (all dama namespaces should use the same
   * version.
   */
  private static final String ACTUAL_VERSION = "/2015-08";
  /**
   * version of the basemetadata. (Attention: all dama namespaces should use the
   * same version.
   */
  private static final String VERSION_BASEMETADATA = ACTUAL_VERSION;
  /**
   * version of the dataorganization. (Attention: all dama namespaces should use
   * the same version.
   */
  private static final String VERSION_DATAORGANIZATION = ACTUAL_VERSION;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="XSD files">
  /**
   * XSD file for basemetadata.
   */
  public static final String BASEMETADATA_XSD = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_BASEMETADATA + VERSION_BASEMETADATA + "/basemetadata.xsd";
  /**
   * XSD file for data organization.
   */
  public static final String DATAORGANIZATION_XSD = PREFIX_DAMA_NAMESPACE + POSTFIX_DAMA_NAMESPACE_DATAORGANIZATION + VERSION_DATAORGANIZATION + "/dataorganization.xsd";
  // </editor-fold> 

  // <editor-fold defaultstate="collapsed" desc="Root elements of the dama sections">
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
  // </editor-fold>

}