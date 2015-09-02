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
package edu.kit.dama.commons.types;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import java.util.Properties;

/**
 *
 * @author mf6319
 */
public interface IConfigurable {

  /**
   * Return all internal property keys. These keys are used to configure the
   * instance at runtime.
   *
   * @return An array of internal property keys.
   */
  String[] getInternalPropertyKeys();

  /**
   * Get a plain description for the provided property key 'pKey'. This method
   * may be used for user interfaces to give the user some idea of the property
   * and valid ranges.
   *
   * @param pKey The key for which a description should be returned.
   *
   * @return The description.
   */
  String getInternalPropertyDescription(String pKey);

  /**
   * Return all user definable property keys. These keys are used to configure
   * the instance at runtime.
   *
   * @return An array of user property keys.
   */
  String[] getUserPropertyKeys();

  /**
   * Get a plain description for the provided property key 'pKey'. This method
   * may be used for user interfaces to give the user some idea of the property
   * and valid ranges.
   *
   * @param pKey The key for which a description should be returned.
   *
   * @return The description.
   */
  String getUserPropertyDescription(String pKey);

  /**
   * Validate all properties provided by the properties object. The validation
   * process should check at least all mandatory properties for valid values.
   * Optionally, also user properties can be checked but might not be available,
   * e.g. if calling {@link #validateProperties(java.util.Properties)} before
   * any user interaction took place during server-sided configuration.
   *
   * If a property is missing or has an invalid value, a
   * {@link PropertyValidationException} must the thrown including a human
   * readable message.
   *
   * In any case, if {@link #validateProperties(java.util.Properties)} succeeds,
   * {@link #configure(java.util.Properties)} must also succeed. Compared to the
   * actual configuration the validation of properties should have no side
   * effects.
   *
   * @param pProperties The properties object to check.
   *
   * @throws PropertyValidationException If the validation fails, e.g. due to a
   * missing mandatory property.
   */
  void validateProperties(Properties pProperties) throws PropertyValidationException;

  /**
   * Perform the actual configuration using the provided properties object.
   *
   * In order to avoid the necessity of separate checks, as a first step
   * {@link #validateProperties(java.util.Properties)} should be performed.
   * Afterwards, the configuration can take place.
   *
   * The actual configuration procedure might have side effects on the
   * underlying system, e.g. the creation of directories, files or changing
   * configuration values.
   *
   * @param pProperties The properties object used to perform the configuration.
   *
   * @throws PropertyValidationException If the validation before the
   * configuration fails, e.g. due to a missing mandatory property.
   * @throws ConfigurationException If the configuration fails.
   */
  void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException;
}
