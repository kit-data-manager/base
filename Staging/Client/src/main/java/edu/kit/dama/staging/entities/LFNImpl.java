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
package edu.kit.dama.staging.entities;

import edu.kit.dama.commons.types.ILFN;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author jejkal
 */
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("url")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("url")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public final class LFNImpl extends ILFN {

  private URL url = null;

  /**
   * Default constructor.
   */
  public LFNImpl() {
  }

  /**
   * Default constructor.
   *
   * @param pUrl The URL as string.
   */
  public LFNImpl(String pUrl) {
    try {
      url = new URL(pUrl);
    } catch (MalformedURLException mue) {
      throw new IllegalArgumentException("Argument 'pUrl' with value " + pUrl + " does not represent a valid URL", mue);
    }
  }

  /**
   * Default constructor.
   *
   * @param pUrl The URL.
   */
  public LFNImpl(URL pUrl) {
    url = pUrl;
  }

  @Override
  public String asString() {
    return url.toString();
  }

  @Override
  public void fromString(String string) {
    try {
      url = new URL(string);
    } catch (MalformedURLException mue) {
      throw new IllegalArgumentException("Argument 'string' with value " + string + " does not represent a valid URL", mue);
    }
  }

  /**
   * Get the Url.
   *
   * @return The Url.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Set the Url.
   *
   * @param url The Url.
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final LFNImpl other = (LFNImpl) obj;
    return !((this.url == null) ? (other.url != null) : !this.url.toString().equals(other.url.toString()));
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 23 * hash + (this.url != null ? this.url.toString().hashCode() : 0);
    return hash;
  }
}
