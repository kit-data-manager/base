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
package edu.kit.dama.rest.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @param <C> Wrapped class type.
 *
 * @author mf6319
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEntityWrapper<C> implements IEntityWrapper<C> {

    /**
     * The count field representing either the result of a count operation (e.g.
     * get overall entity count), the number of rows affected by an update
     * (typically 0 or 1) or the size of the list 'entities'.
     */
    private Integer count = 0;

    /**
     * Default constructor.
     */
    public AbstractEntityWrapper() {
        this(new LinkedList<C>());
    }

    /**
     * Default constructor.
     *
     * @param pCount The value of count.
     */
    public AbstractEntityWrapper(Integer pCount) {
        this();
        setCount(pCount);
    }

    /**
     * Default constructor taking a list of entities.
     *
     * @param pEntities A list of entities.
     */
    public AbstractEntityWrapper(List<C> pEntities) {
        List<C> lEntities = new LinkedList<>();
        lEntities.addAll(pEntities);
        setWrappedEntities(lEntities);
    }

    /**
     * Default constructor taking an array of entities.
     *
     * @param pEntities An array of entities which must not be null .
     */
    public AbstractEntityWrapper(C... pEntities) {
        List<C> lEntities = new LinkedList<>();
        lEntities.addAll(Arrays.asList(pEntities));
        setWrappedEntities(lEntities);
    }

    /**
     * Set a list of wrapped entities.
     *
     * @param pEntities A list of entities.
     */
    @JsonIgnore
    public final void setWrappedEntities(List<C> pEntities) {
        if (pEntities != null) {
            //remove null values
            pEntities.remove(null);
            setEntities(pEntities);
        } else {
            setEntities(new LinkedList<C>());
        }
        setCount(getWrappedEntities().size());
    }

    /**
     * Set all entities which are part of this wrapper. This method may also
     * take null if no entity is there.
     *
     * @param pEntities All associated entities.
     */
    public abstract void setEntities(List<C> pEntities);

    /**
     * Get all wrapped entities. This method wraps getEntities() but will never
     * return null. If no entity is wrapped, an empty list is returned.
     *
     * @return All wrapped entities or an empty list.
     */
    @JsonIgnore
    public final List<C> getWrappedEntities() {
        List<C> result = getEntities();

        if (result != null) {
            return result;
        }

        return new LinkedList<>();
    }

    /**
     * Get all entities which are part of this wrapper. This method may also
     * return null if no entity is there.
     *
     * @return All associated entities.
     */
    public abstract List<C> getEntities();

    /**
     * Set the value of the count field. In some cases the value is set
     * automatically, e.g. while setting a value of 'entities' during
     * construction or by setEntities().
     *
     * @param count The new value.
     */
    public final void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Get the value of the count field.
     *
     * @return The value of the count field.
     */
    public final Integer getCount() {
        return count;
    }
}
