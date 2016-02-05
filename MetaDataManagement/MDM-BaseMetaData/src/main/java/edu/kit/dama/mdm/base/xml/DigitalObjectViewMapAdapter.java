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
package edu.kit.dama.mdm.base.xml;

import edu.kit.dama.mdm.base.DigitalObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 *
 * @author mf6319
 */
public class DigitalObjectViewMapAdapter extends XmlAdapter<DigitalObjectViewMapAdapter.AdaptedMap, Map<DigitalObject, String>> {

  public static class AdaptedMap {

    @XmlVariableNode("key")
    List<AdaptedEntry> entries = new ArrayList<AdaptedEntry>();

  }

  public static class AdaptedEntry {

    @XmlTransient
    public String key;

    @XmlValue
    public String value;

  }

  @Override
  public AdaptedMap marshal(Map<DigitalObject, String> map) throws Exception {
    AdaptedMap adaptedMap = new AdaptedMap();
    for (Entry<DigitalObject, String> entry : map.entrySet()) {
      AdaptedEntry adaptedEntry = new AdaptedEntry();
      adaptedEntry.key = Long.toString(entry.getKey().getBaseId());
      adaptedEntry.value = entry.getValue();
      adaptedMap.entries.add(adaptedEntry);
    }
    return adaptedMap;
  }

  @Override
  public Map<DigitalObject, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
    List<AdaptedEntry> adaptedEntries = adaptedMap.entries;
    Map<DigitalObject, String> map = new HashMap<>(adaptedEntries.size());
    for (AdaptedEntry adaptedEntry : adaptedEntries) {
      DigitalObject o = new DigitalObject();
      o.setBaseId(Long.parseLong(adaptedEntry.key));
      map.put(o, adaptedEntry.value);
    }
    return map;
  }
}
