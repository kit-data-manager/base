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
package edu.kit.dama.ui.admin;

import edu.kit.dama.ui.admin.client.HelpState;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Image;

/**
 *
 * @author mf6319
 */
public class HelpExtension extends AbstractExtension {

  public void extend(Image image) {
    getState().setHelpId(image.getId());
    super.extend(image);
  }

  @Override
  public HelpState getState() {
    return (HelpState) super.getState();
  }
}
