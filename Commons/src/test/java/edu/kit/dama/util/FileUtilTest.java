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
package edu.kit.dama.util;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mf6319
 */
public class FileUtilTest {

  @Test
  public void testHomeDirectory() {
    File userDir = org.apache.commons.io.FileUtils.getUserDirectory();
    Assert.assertTrue(FileUtils.isAccessible(userDir));
  }

  @Test
  public void testInvalidDir() {
    //a: should fail under windows and shot not exists under linux
    Assert.assertFalse(FileUtils.isAccessible(new File("a:/someInvalidDir"), 1000));
  }
}
