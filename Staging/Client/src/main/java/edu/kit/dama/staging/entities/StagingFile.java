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
package edu.kit.dama.staging.entities;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.staging.exceptions.UnsupportedLFNException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author jejkal
 */
public class StagingFile extends ILFN {

  // private final static Logger LOGGER = LoggerFactory.getLogger(StagingFile.class);
  private AbstractFile abstractFileRepresentation = null;

  /**
   * Creates a StagingFile from another LFN. This only works if the string
   * representation of the other file is a valid URL.
   *
   * @param pOther The other LFN.
   *
   * @throws MalformedURLException If the string representation of the other
   * file is not a valid URL.
   */
  public StagingFile(ILFN pOther) throws MalformedURLException {
    this(new AbstractFile(new URL(pOther.asString())));
  }

  /**
   * Creates a StagingFile from a local file.
   *
   * @param pFile The local file.
   */
  public StagingFile(AbstractFile pFile) {
    if (pFile == null) {
      throw new IllegalArgumentException("Argument 'pFile' must not be 'null'");
    }
    abstractFileRepresentation = new AbstractFile(pFile.getUrl());
  }

  @Override
  public String asString() {
    try {
      return abstractFileRepresentation.getUrl().toURI().toString();
    } catch (URISyntaxException use) {
      throw new UnsupportedLFNException("Failed to convert as string. Invalid URL '" + abstractFileRepresentation.getUrl() + "'", use);
    }
  }

  @Override
  public void fromString(String pStringRepresentation) {
    if (pStringRepresentation == null) {
      throw new IllegalArgumentException("Argument 'pStringRepresentation' must not be 'null'");
    }

    try {
      abstractFileRepresentation = new AbstractFile(new URL(pStringRepresentation));
    } catch (MalformedURLException mue) {
      throw new UnsupportedLFNException("Failed to convert from string representation '" + pStringRepresentation + "'. Expected valid URL.", mue);
    }
  }

  /**
   * Returns whether this file is local or not (staged).
   *
   * @return TRUE if the underlaying file is not located on the local disk.
   */
  public boolean isLocal() {
    return abstractFileRepresentation.isLocal();
  }

  /**
   * Returns the AbstractFile representation of this instance.
   *
   * @return An AbstractFile object.
   */
  public AbstractFile getAbstractFile() {
    return abstractFileRepresentation;
  }

  @Override
  public String toString() {
    if (abstractFileRepresentation != null) {
      return abstractFileRepresentation.toString();
    }
    return "!Invalid!";
  }
}
