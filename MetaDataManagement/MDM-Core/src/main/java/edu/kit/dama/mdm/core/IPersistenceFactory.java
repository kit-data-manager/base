/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.core;

/**
 * Interface for creating persistence units.
 *
 * @author hartmann-v
 */
public interface IPersistenceFactory {

    /**
     * Get an instance of persistence framework. The instance should be a
     * singleton to avoid runtime conditions while persisting data.
     *
     * @param persistenceUnit select a appropriate data base.
     * @return instance for persisting objects.
     */
    IMetaDataManager getMetaDataManager(String persistenceUnit);

    /**
     * Destroy the persistence factory, e.g. close all idle connections.
     */
    void destroy();
}
