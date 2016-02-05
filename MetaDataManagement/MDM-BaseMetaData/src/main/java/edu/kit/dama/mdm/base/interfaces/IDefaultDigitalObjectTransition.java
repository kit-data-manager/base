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
package edu.kit.dama.mdm.base.interfaces;

import java.util.Set;

/**
 *
 * @author jejkal
 */
public interface IDefaultDigitalObjectTransition extends ISimpleDigitalObjectTransition {

    String getTransitionEntityId();

    /**
     * Get the list of input objects and their according views that are inputs
     * of this transition.
     *
     * @return The list of input objects and their views.
     */
    Set<? extends IDefaultObjectViewMapping> getInputObjectViewMappings();

    /**
     * Get the set of output objects that are result of this transition. In
     * typical use cases only one output object should be produced, but in
     * special cases also multiple outputs are imaginable.
     *
     * @return The set of output objects.
     */
    Set<? extends ISimpleDigitalObject> getOutputObjects();

    /**
     * Get the creation timestamp of this transition.
     *
     * @return The creation timestamp.
     */
    Long getCreationTimestamp();

}
