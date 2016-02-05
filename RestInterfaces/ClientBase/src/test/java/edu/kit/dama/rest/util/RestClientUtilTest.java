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
package edu.kit.dama.rest.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.xml.ws.WebServiceException;
import junit.framework.TestCase;

/**
 *
 * @author hartmann-v
 */
public class RestClientUtilTest extends TestCase {

  public RestClientUtilTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAllFormats() {
    Object[] arguments = {"string", new Integer(1), new Double(0.3)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/string/1/0.3", result);
  }

  public void testNumberFormats() {
    Object[] arguments = {new Long(1000), new Integer(10000), new Double(0.0003)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/1000/10000/0.0003", result);
  }

  public void testLongFormats() {
    Object[] arguments = {new Long(123456789), new Long(123456), new Long(0)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/123456789/123456/0", result);
  }

  public void testIntegerFormats() {
    Object[] arguments = {new Integer(123456789), new Integer(123456), new Integer(0)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/123456789/123456/0", result);
  }

  public void testDoubleFormats() {
    Object[] arguments = {new Double(123456789.123456), new Double(123456.789), new Double(0.00000001)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/123456789.123456/123456.789/0.00000001", result);
  }

  public void testFloatFormats() {
    Object[] arguments = {new Float(123456792), "float values don't work well", new Float(0.00000001)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/123456792/float%20values%20don't%20work%20well/0.00000001", result);
  }

  public void testAllFormatsWithEncoding() {
    Object[] arguments = {"stri ng", new Integer(1), new Double(0.3)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/stri%20ng/1/0.3", result);
  }

  public void testAllFormatsWithEncodingWrongParameterNo() {
    Object[] arguments = {"stri ng", new Integer(1)};
    String result = RestClientUtils.encodeUrl("/hallo/{0}/{1}/{2}", arguments);
    assertEquals("/hallo/stri%20ng/1/{2}", result);
  }

  public void testCreateObjectFromStream() {
    // if there is no result expected null has to be delivered.
    Object result = null;
    try {
      result = RestClientUtils.createObjectFromStream((Class) null, null);
      assertTrue(false);
    } catch (WebServiceException wse) {
      assertTrue(true);
    }
    assertNull(result);
  }
}
