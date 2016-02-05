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
package edu.kit.dama.ui.admin.client;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import edu.kit.dama.ui.admin.HelpExtension;

/**
 *
 * @author mf6319
 */
@Connect(HelpExtension.class)
public class HelpConnector extends AbstractExtensionConnector {

  @Override
  protected void extend(ServerConnector target) {
    final Widget hyperlink = ((ComponentConnector) target).getWidget();
    final String cellId = getState().getHelpId();
    hyperlink.addDomHandler(new MouseOverHandler() {

      @Override
      public void onMouseOver(MouseOverEvent event) {
      /*  Element t_wrapper = DOM.getElementById(cellId + "_wrapper");
        t_wrapper.addClassName("invisible");
        t_wrapper.removeClassName("visible");*/
        Element t_help = DOM.getElementById(cellId + "_help");
        t_help.addClassName("v-label-visible");
        t_help.removeClassName("v-label-invisible");
      }

    }, MouseOverEvent.getType());

    hyperlink.addDomHandler(new MouseOutHandler() {

      @Override
      public void onMouseOut(MouseOutEvent event) {
       /* Element t_wrapper = DOM.getElementById(cellId + "_wrapper");
        t_wrapper.addClassName("visible");
        t_wrapper.removeClassName("invisible");*/

        Element t_help = DOM.getElementById(cellId + "_help");
        t_help.addClassName("v-label-invisible");
        t_help.removeClassName("v-label-visible");
      }

    }, MouseOutEvent.getType());
  }

  @Override
  public HelpState getState() {
    return (HelpState) super.getState();
  }
}
