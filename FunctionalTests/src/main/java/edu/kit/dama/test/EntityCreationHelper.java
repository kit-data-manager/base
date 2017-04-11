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
package edu.kit.dama.test;

import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import java.util.Date;

/**
 * Basic helper to support creating base entities like study and organization
 * unit.
 *
 * @author mf6319
 */
public class EntityCreationHelper {

  /**
   * Create an OrganizationUnit using the provided values.
   *
   * @param name The name.
   * @param manager The manager.
   * @param address The address.
   * @param city The city.
   * @param zipCode The zip code.
   * @param country The country.
   * @param website The website.
   *
   * @return The OrganiztationUnit.
   */
  public static final OrganizationUnit createOrganizationUnit(String name, UserData manager, String address, String city, String zipCode, String country, String website) {
    OrganizationUnit ou = new OrganizationUnit();
    ou.setOuName(name);
    ou.setManager(manager);
    ou.setAddress(address);
    ou.setCity(city);
    ou.setZipCode(zipCode);
    ou.setCountry(country);
    ou.setWebsite(website);
    return ou;
  }

  /**
   * Create a study using the provided values.
   *
   * @param topic The topic.
   * @param manager The manager.
   * @param organizationUnits A list of participating organization units.
   * @param start The start date.
   * @param end The end date.
   *
   * @return The Study.
   */
  public static final Study createStudy(String topic, UserData manager, OrganizationUnit[] organizationUnits, Date start, Date end) {
    Study s = Study.factoryNewStudy();
    s.setTopic(topic);
    s.setManager(manager);
    for (OrganizationUnit ou : organizationUnits) {
      s.addOrganizationUnit(ou);
    }
    s.setStartDate(start);
    s.setEndDate(end);
    s.setLegalNote("CHARTER OF THE UNITED FEDERATION OF PLANETS\n"
            + "\n"
            + "We the lifeforms of the United Federation of Planets determined "
            + "to save succeeding generations from the scourge of war, and to "
            + "reaffirm faith in the fundamental rights of sentient beings, in "
            + "the dignity and worth of all lifeforms, in the equal rights of "
            + "members of planetary systems large and small, and to establish "
            + "conditions under which justice and respect for the obligations "
            + "arising from treaties and other sources of interstellar law can "
            + "be maintained, and to promote social progress and better "
            + "standards of living on all worlds...");
    return s;
  }
}
