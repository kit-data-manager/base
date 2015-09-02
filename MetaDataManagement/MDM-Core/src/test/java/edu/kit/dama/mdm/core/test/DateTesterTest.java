/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.core.test;

import java.util.Calendar;
import java.util.Date;
import edu.kit.dama.mdm.core.tools.DateTester;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author hartmann-v
 */
public class DateTesterTest {

  /** For logging purposes. */
  private static Logger LOGGER = LoggerFactory.getLogger(MetaDataManagementTest.class);

  /** Test for NULL all parameter.*/
  @Test
  public void testNull() {
    DateTester.testForValidDates(null, null);
    assertTrue(true);
  }

  /** Test for NULL second parameter.*/
  @Test
  public void testNullSecond() {
    DateTester.testForValidDates(new Date(), null);
    assertTrue(true);
  }

  /** Test for NULL first parameter.*/
  @Test
  public void testNullFirst() {
    DateTester.testForValidDates(null, new Date());
    assertTrue(true);
  }

  /** Test for NULL all Parameters.*/
  @Test(expected = IllegalArgumentException.class)
  public void testForSameDate() {
    Date date = new Date();
    DateTester.testForValidDates(date, date);
    assertTrue(false);
  }

  /** Test for nearly same date.*/
  @Test
  public void testForNearlySameDate() {
    Date first = new Date();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      LOGGER.error("Thread.sleep", ex);
    }
    Date second = new Date();
    DateTester.testForValidDates(first, second);
    assertTrue(true);
  }

  /** Test for nearly same date.*/
  @Test(expected = IllegalArgumentException.class)
  public void testForNearlySameDateButFalse() {
    Date first = new Date();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      LOGGER.error("Thread.sleep", ex);
    }
    Date second = new Date();
    DateTester.testForValidDates(second, first);
    assertTrue(false);
  }

  /** Test for nearly same date.*/
  @Test
  public void testForExtremValues() {
    Date first = new Date(0);
    Date second = new Date(Long.MAX_VALUE);
    DateTester.testForValidDates(first, second);
    assertTrue(true);
  }

  /** Test for nearly same date.*/
  @Test(expected = IllegalArgumentException.class)
  public void testForMaxRangeButFalse() {
    Date first = new Date(Long.MAX_VALUE);
    Date second = new Date(0);
    DateTester.testForValidDates(first, second);
    assertTrue(false);
  }

  /** Test for same date (earliest date possible).*/
  @Test(expected = IllegalArgumentException.class)
  public void testForFirstDateButFalse() {
    Date first = new Date(0);
    Date second = new Date(0);
    DateTester.testForValidDates(first, second);
    assertTrue(false);
  }

  /** Test for same date (latest date possible).*/
  @Test(expected = IllegalArgumentException.class)
  public void testForLastDateButFalse() {
    Date first = new Date(Long.MAX_VALUE);
    Date second = new Date(Long.MAX_VALUE);
    DateTester.testForValidDates(first, second);
    assertTrue(false);
  }

  /** Test for wrong order in future. */
  @Test(expected = IllegalArgumentException.class)
  public void testForFalseDatesInFuture() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2050, Calendar.FEBRUARY, 1);
    Date first = calendar.getTime();
    calendar.set(2050, Calendar.JANUARY, 31);
    Date second = calendar.getTime();
    DateTester.testForValidDates(first, second);
    assertTrue(false);
  }

  /** Test for wrong order in past.*/
  @Test(expected = IllegalArgumentException.class)
  public void testForFalseDatesInPast() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2000, Calendar.FEBRUARY, 1);
    Date first = calendar.getTime();
    calendar.set(1999, Calendar.JANUARY, 31);
    Date second = calendar.getTime();
    DateTester.testForValidDates(first, second);
    assertTrue(false);
  }

  /** Test for correct order in future.*/
  @Test
  public void testForValuesInFuture() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2050, Calendar.DECEMBER, 31);
    Date first = calendar.getTime();
    calendar.set(2051, Calendar.JANUARY, 1);
    Date second = calendar.getTime();
    DateTester.testForValidDates(first, second);
    assertTrue(true);
  }

  /** Test for correct order in past.*/
  @Test
  public void testForValuesInPast() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1999, Calendar.DECEMBER, 31);
    Date first = calendar.getTime();
    calendar.set(2000, Calendar.JANUARY, 1);
    Date second = calendar.getTime();
    DateTester.testForValidDates(first, second);
    assertTrue(true);
  }
}
