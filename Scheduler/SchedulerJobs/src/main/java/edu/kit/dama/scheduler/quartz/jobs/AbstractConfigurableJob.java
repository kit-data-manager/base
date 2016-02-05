/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.scheduler.quartz.jobs;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.commons.types.IConfigurable;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.Properties;
import org.quartz.Job;

/**
 *
 * @author jejkal
 */
public abstract class AbstractConfigurableJob implements Job, IConfigurable {

    @Override
    public final void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
        //not supported
    }

    @Override
    public final String[] getUserPropertyKeys() {
        //not supported
        return new String[]{};
    }

    @Override
    public final String getUserPropertyDescription(String pKey) {
        //not supported
        return "";
    }

    /**
     * Convert the provided key-value properties object to a string that is
     * provided in the JobContext during job execution. The default
     * implementation serialized to properties object into a string. This
     * behaviour might be changed if required.
     *
     * @param pProperties The properties object.
     *
     * @return The string representation of pProperties.
     */
    public String propertiesToJobParameters(Properties pProperties) {
        try {
            return PropertiesUtil.propertiesToString(pProperties);
        } catch (IOException ex) {
            return null;
        }
    }

}
