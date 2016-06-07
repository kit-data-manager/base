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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import org.apache.commons.configuration.Configuration;

/**
 * Null transition type handler implementation that is used as a dummy for
 * skipping transition type processing. It returns null values for each call and
 * its configuration always succeeds. It also can be used to disable transition
 * type processing for specific types.
 *
 * @author jejkal
 */
public final class NullTransitionTypeHandler extends AbstractTransitionTypeHandler<Object> {

    @Override
    public DigitalObjectTransition<Object> factoryTransitionEntity() {
        return new DigitalObjectTransition<>();
    }

    @Override
    public String getTransitionEntityId(Object pTransitionEntity) {
        return null;
    }

    @Override
    public Object handleTransitionEntityData(String pTransitionEntityData) {
        return null;
    }

    @Override
    public Object loadTransitionEntity(String pTransitionEntityId) {
        return null;
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        //not needed
        return true;
    }

}
