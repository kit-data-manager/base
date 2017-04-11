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
package edu.kit.dama.test;

import edu.kit.lsdf.adalapi.authentication.AbstractAuthentication;
import edu.kit.lsdf.adalapi.authentication.AuthField;
import edu.kit.lsdf.adalapi.exception.AuthenticationInputMethodException;
import java.util.Collection;

/**
 * Auto-Authenticator implementation for accessing test data via WebDav with
 * pre-defined credentials.
 *
 * @author mf6319
 */
public class AutoAuthentication extends AbstractAuthentication {

  @Override
  public final void createVector() {
    AuthField userField = new AuthField("user", "admin", "", false, false);
    AuthField groupField = new AuthField("password", "dama14", "", true, false);

    super.getUserInteractionVector().add(userField);
    super.getUserInteractionVector().add(groupField);
  }

  @Override
  public final Collection<AuthField> authenticate() throws AuthenticationInputMethodException {
    return getUserInteractionVector();
  }
}
