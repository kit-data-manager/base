
/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.staging.adalapi.authenticator;

import edu.kit.lsdf.adalapi.authentication.AbstractAuthentication;
import edu.kit.lsdf.adalapi.authentication.AuthField;
import edu.kit.lsdf.adalapi.exception.AuthenticationInputMethodException;
import java.net.URL;
import java.util.Collection;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jejkal
 */
public class KITDMAuthenticator extends AbstractAuthentication {

    private String context = null;

    @Override
    public void configure(URL pUrl, Configuration pConfig) {
        super.configure(pUrl, pConfig);
        //obtain user info
        context = pConfig.getString("repository.context");
        super.getUserInteractionVector().clear();
        createVector();
    }

    @Override
    public boolean isConnected() {
        return context != null;
    }

    @Override
    public final Collection<AuthField> authenticate() throws AuthenticationInputMethodException {
        super.getUserInteractionVector().clear();
        createVector();
        if (isConnected()) {
            //skip interaction as we already have all infor mation
            return super.getUserInteractionVector();
        }
        //clear vector in order to set user information provided in the URL
        return getUserInput();
    }

    @Override
    public final void createVector() {
        AuthField userField = new AuthField("Username", "", "Please insert username!", false, false);
        AuthField passwordField = new AuthField("Password", "", "Please insert your password!", true, false);
        super.getUserInteractionVector().add(userField);
        super.getUserInteractionVector().add(passwordField);
    }

}
