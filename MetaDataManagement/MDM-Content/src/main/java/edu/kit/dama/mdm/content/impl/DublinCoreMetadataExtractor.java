/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.content.impl;

import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.w3c.dom.Element;

/**
 * Implementation of DC metadata extractor. This implementation maps attributes
 * of KIT Data Manager base metadata to the Dublin Core schema according to the
 * recommendations (see http://www.ietf.org/rfc/rfc5013.txt).
 *
 * @author mf6319
 */
public class DublinCoreMetadataExtractor extends AbstractMetadataExtractor {

  /**
   * ISO 8601 [W3CDTF] date format
   */
  public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * Default constructor.
   *
   * @param pUniqueIdentifier The unique identifier of the extractor.
   */
  public DublinCoreMetadataExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
    //no properties needed
  }

  @Override
  protected String[] getExtractorPropertyKeys() {
    //no extra properties available
    return null;
  }

  @Override
  protected String getExtractorPropertyDescription(String pProperty) {
    return null;
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
  }

  @Override
  protected void performPreTransferExtraction(TransferTaskContainer pContainer) throws StagingProcessorException {
  }

  @Override
  public String createMetadataDocument(TransferTaskContainer pContainer) throws StagingProcessorException {
    StringBuilder xmlBuilder = new StringBuilder();
    xmlBuilder.append("<oai_dc:dc \n"
            + "     xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" \n"
            + "     xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n"
            + "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
            + "     xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ \n"
            + "     http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">");

    xmlBuilder.append("<dc:title>").append(getDigitalObject().getLabel()).append("</dc:title>");
    UserData uploader = getDigitalObject().getUploader();
    if (uploader != null) {
      xmlBuilder.append("<dc:creator>").append(uploader.getFullname()).append("</dc:creator>");
      xmlBuilder.append("<dc:publisher>").append(uploader.getFullname()).append("</dc:publisher>");
    }

    for (UserData experimenter : getDigitalObject().getExperimenters()) {
      //don't list uploader a second time here
      if (uploader == null || !experimenter.equals(uploader)) {
        xmlBuilder.append("<dc:contributor>").append(experimenter.getFullname()).append("</dc:contributor>");
      }
    }

    if (getDigitalObject().getInvestigation() != null) {
      xmlBuilder.append("<dc:subject>").append(getDigitalObject().getInvestigation().getTopic()).append("</dc:subject>");
      String description = getDigitalObject().getInvestigation().getDescription();
      if (description != null) {
        xmlBuilder.append("<dc:description>").append(description).append("</dc:description>");
      }
      if (getDigitalObject().getInvestigation().getStudy() != null) {
        String legalNotes = getDigitalObject().getInvestigation().getStudy().getLegalNote();
        if (legalNotes != null) {
          xmlBuilder.append("<dc:rights>").append(legalNotes).append("</dc:rights>");
        }
      }
    }
    if (getDigitalObject().getStartDate() != null) {
      xmlBuilder.append("<dc:date>").append(new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(getDigitalObject().getStartDate())).append("</dc:date>");
    }
    //not possible in our case - use binary type 'application/octet-stream'
    xmlBuilder.append("<dc:format>").append("application/octet-stream").append("</dc:format>");

    //see http://dublincore.org/documents/2012/06/14/dcmi-terms/?v=dcmitype  
    xmlBuilder.append("<dc:type>").append("Dataset").append("</dc:type>");
    xmlBuilder.append("<dc:identifier>").append(getDigitalObject().getDigitalObjectId().getStringRepresentation()).append("</dc:identifier>");

    //>>>not possible, yet -> later: getDigitalObject().getPredecessor() for processing results!?
    //xmlBuilder.append("<dc:source>").append(----).append("</dc:source>");
    //xmlBuilder.append("<dc:relation>").append(----).append("</dc:relation>");
    //>>>not relevant!? Otherwise refer to RFC 4646
    //xmlBuilder.append("<dc:language>").append("").append("</dc:language>");
    //Not relevant?!
    //xmlBuilder.append("<dc:coverage>").append("").append("</dc:coverage>");
    xmlBuilder.append("</oai_dc:dc>");

    return xmlBuilder.toString();
  }

  @Override
  protected Element createCommunitySpecificElement(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    //not needed here  
    return null;
  }

  @Override
  public String getName() {
    return "DublinCoreMetadataExtractor";
  }

//  public static void main(String[] args) throws Exception {
//    DigitalObject dob = DigitalObject.factoryNewDigitalObject("1234567890");
//    dob.setLabel("Test");
//    dob.setStartDate(new Date(100));
//    ExtractDCMetadata ex = new ExtractDCMetadata("12345");
//    ex.setDigitalObject(dob);
//    String result = ex.createMetadataDocument(null);
//
//    String expectedResult = "<oai_dc:dc \n"
//            + "     xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" \n"
//            + "     xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n"
//            + "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
//            + "     xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ \n"
//            + "     http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:title>Test</dc:title><dc:date>1970-01-01T01:00:00+0100</dc:date><dc:format>application/octet-stream</dc:format><dc:type>Dataset</dc:type><dc:identifier>1234567890</dc:identifier></oai_dc:dc>";
//
//    System.out.println(result);
//    System.out.println("Everything is as expected: " + expectedResult.equals(result));
//  }
}
