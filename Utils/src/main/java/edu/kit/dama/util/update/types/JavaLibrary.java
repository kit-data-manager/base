/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.util.update.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Java library entry containing all information needed to compare differen
 * versions of one library. While constructing a JavaLibrary object the name of
 * the provided file is analyzed using regular expression to extract name and
 * version information. Also the information whether the library is a snapshot
 * or not is determined. Finally, the MD5 hash of the entire file is calculated
 * in order to compare two versions of a JavaLibrary with each other on the byte
 * level in case two files with the same name are different.
 *
 * @author mf6319
 */
public final class JavaLibrary {

  private final String fullname;
  private String md5;
  private final String name;
  private final String version;
  private final boolean snapshot;
  private final File location;
  private final boolean valid;

  /**
   * Default constructor. During construction library name, version, snapshot
   * status and MD5 hash are obtained. If the filename does not match the
   * regular expression, name and version are set 'null' and {@link #isValid()}
   * will return 'false'.
   *
   * @param pFile The jar file.
   */
  public JavaLibrary(File pFile) {
    location = pFile;
    fullname = pFile.getName();
    try (FileInputStream fis = new FileInputStream(pFile)) {
      md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
    } catch (IOException ex) {
      md5 = "";
    }
    String nameVersion = StringUtils.substringBeforeLast(fullname, ".");

    Matcher m = Pattern.compile("(.*)[-]([0-9\\.]+)(-SNAPSHOT)?").matcher(nameVersion);
    if (m.find()) {
      name = m.group(1);
      version = m.group(2);
      snapshot = m.group(3) != null;
      valid = true;
    } else {
      //does not fit file pattern
      name = null;
      version = null;
      snapshot = false;
      valid = false;
    }
  }

  public String getHash() {
    return md5;
  }

  public String getFullname() {
    return fullname;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public File getLocation() {
    return location;
  }

  public boolean isSnapshot() {
    return snapshot;
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String toString() {
    return getName() + " (" + getVersion() + (isSnapshot() ? "-SNAPSHOT)" : ")");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JavaLibrary) {
      //check full name and hash...if they are the same, everything is fine
      return getFullname().equals(((JavaLibrary) obj).getFullname()) && getHash().equals(((JavaLibrary) obj).getHash());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 17 * hash + Objects.hashCode(this.fullname);
    hash = 17 * hash + Objects.hashCode(this.md5);
    return hash;
  }

}
