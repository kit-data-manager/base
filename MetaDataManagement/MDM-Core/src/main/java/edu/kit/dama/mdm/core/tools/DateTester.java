/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.core.tools;

import java.util.Date;

/**
 * Utility class for date tests.
 *
 * @author hartmann-v
 */
public final class DateTester {

  /**
   * Constructor to disable default constructor for extern classes.
   */
  private DateTester() {
  }

  /**
   * Test whether start date is less than end date. If not throws an exception.
   *
   * @param startDate the earlier date
   * @param endDate the following date
   */
  public static void testForValidDates(final Date startDate,
          final Date endDate) {
    if (startDate != null && endDate != null && !startDate.before(endDate)) {
      throw new IllegalArgumentException("Property 'validFrom' ("
              + startDate.toString()
              + ") has to be earlier than property 'validUntil' ("
              + endDate.toString() + ")!");
    }
  }
}
