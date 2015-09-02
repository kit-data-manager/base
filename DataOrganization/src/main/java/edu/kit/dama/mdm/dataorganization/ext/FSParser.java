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
package edu.kit.dama.mdm.dataorganization.ext;

import edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.DataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.FileNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.Attribute;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author pasic
 */
public class FSParser {

    public DataOrganizationNode parseDONodes(File from, int maxDepth,
            String viewName) throws IOException {
        DataOrganizationNode ret;
        if (0 < maxDepth) {
            if (from.isDirectory()) {
                CollectionNode cn = new CollectionNode();
                cn.setName(from.getName());
                File[] ls = from.listFiles();
                if (null != ls) {
                    for (File child : ls) {
                        cn.addChild(parseDONodes(child, maxDepth - 1, viewName));
                    }
                }
                ret = cn;
            } else {
                ret = new FileNode(null);
                FileNode ff = new FileNode(null);
                ff.setLogicalFileName(null);
                long lastmod = from.lastModified();
                long sz = from.length();
                boolean hidden = from.isHidden();
                ret.addAttribute(new Attribute("last-modified", String.valueOf(
                        lastmod)));
                ret.addAttribute(new Attribute("size", String.valueOf(
                        sz)));
                ret.addAttribute(new Attribute("hidden", String.valueOf(
                        hidden)));
                ret.addAttribute(new Attribute("true-path", "Beethoven/op56/" +
                        from.getName()));
                ret.setName(from.getName());
            }
        } else {
            throw new IOException();
        }
        ret.setViewName(viewName);
        return ret;
    }

    public DataOrganizationNode parseDONodes(File from, int maxDepth) throws
            IOException {
        return parseDONodes(from, maxDepth, "default");
    }
}
