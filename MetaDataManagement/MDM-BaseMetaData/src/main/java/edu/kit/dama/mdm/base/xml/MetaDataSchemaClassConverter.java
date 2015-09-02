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
package edu.kit.dama.mdm.base.xml;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import edu.kit.dama.mdm.base.MetaDataSchema;

/**
 * Converter for xStream. A metadata schema entity with the schemaIdentifiert
 * 'ns' and the schema Url http://kit.edu is converted to
 * <i>xmlns:ns="http://kit.edu"</i>
 * and vice versa.
 *
 *
 * @author hartmann-v
 */
public class MetaDataSchemaClassConverter implements SingleValueConverter {

  @Override
  public String toString(Object obj) {
    return "xmlns:" + ((MetaDataSchema) obj).getSchemaIdentifier() + "=\"" + ((MetaDataSchema) obj).getMetaDataSchemaUrl() + "\"";
  }

  @Override
  public Object fromString(String name) {
    if (name != null) {
      //sample content: xmlns:ns=\"http://kit.edu\"
      String[] schema = name.trim().split("=");
      //schema = [xmlns:ns, \"http://kit.edu\"]
      String nsString = schema[0].trim().split(":")[1];
      //nsString = ns
      String schemaUrl = schema[1].substring(1, schema[1].length() - 1);
      //schemaUrl = http://kit.edu
      return new MetaDataSchema(nsString, schemaUrl);
    }
    //invalid
    return null;
  }

  @Override
  public boolean canConvert(Class type) {
    return type.equals(MetaDataSchema.class);
  }

//  public static void main(String[] args) {
//    System.out.println(new MetaDataSchemaClassConverter().fromString("xmlns:dc=\"http://heise.de\""));
//  }
}
