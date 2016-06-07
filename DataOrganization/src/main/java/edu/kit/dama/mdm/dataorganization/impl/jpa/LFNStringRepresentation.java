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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.commons.types.ILFN;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.persistence.Embeddable;
import org.slf4j.LoggerFactory;

/**
 * This class is meant to make possible to persist logical fine name types of
 * various.
 *
 * @author pasic
 */
@Embeddable
public class LFNStringRepresentation implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LFNStringRepresentation.class);

    private static final long serialVersionUID = 7526472295622776127L;
    private String value;
    private String fullyQualifiedTypeName = "NULL";

    /**
     * Constructor.
     */
    protected LFNStringRepresentation() {
    }

    /**
     * Creates a corresponding LFNStringRepresentation.
     *
     * @param lfn The lfn
     */
    LFNStringRepresentation(ILFN lfn) {
        fullyQualifiedTypeName = lfn.getClass().getCanonicalName();
        value = lfn.asString();
    }

    /**
     * Gets the string value of the LFN.
     *
     * @return The value.
     */
    String getValue() {
        return value;
    }

    /**
     * Sets the string value of the LFN.
     *
     * @param value The value.
     */
    void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the canonical name of the LFN class.
     *
     * @return The fully qualified type name.
     */
    String getFullyQualifiedTypeName() {
        return fullyQualifiedTypeName;
    }

    /**
     * Gets the LFN. Performs class loading, instantiation via default
     * constructor, and parse from string representation.
     *
     * @return The LFN.
     */
    ILFN getLFN() {
        ILFN ilfn = null;
        if (!"NULL".equals(fullyQualifiedTypeName)) {
            try {
                LOGGER.debug("Getting LFN for class {} with value {}", fullyQualifiedTypeName, value);
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(fullyQualifiedTypeName);
                Constructor constr = clazz.getConstructor();
                ilfn = (ILFN) constr.newInstance();
            } catch (InstantiationException ex) {
                LOGGER.error("Failed to create instance of ILFN implementation", ex);
            } catch (IllegalAccessException ex) {
                LOGGER.error("Failed to access default constructor of ILFN implementation", ex);
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Failed to find ILFN implementation class '" + fullyQualifiedTypeName + "'", ex);
            } catch (NoSuchMethodException nsme) {
                LOGGER.error("ILFN implementation '" + fullyQualifiedTypeName + "' does not contain default constructor", nsme);
            } catch (InvocationTargetException ite) {
                LOGGER.error("Failed to create new instance of ILFN implementation", ite);
            }
            if (ilfn != null) {
                LOGGER.debug("Setting LFN value from string {}", value);
                ilfn.fromString(value);
            } else {
                LOGGER.warn("Construction of LFN implementation failed.");
            }
        }
        return ilfn;
    }

    /**
     * Sets the LFN.
     *
     * @param lfn The LFN.
     */
    void setLFN(ILFN lfn) {
        LOGGER.debug("Setting lfn {}", lfn);
        if (lfn != null) {
            value = lfn.asString();
            fullyQualifiedTypeName = lfn.getClass().getCanonicalName();
        }
    }
}
