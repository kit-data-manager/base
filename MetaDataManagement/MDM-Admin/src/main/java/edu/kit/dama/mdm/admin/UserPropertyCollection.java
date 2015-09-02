/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.admin;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@Entity
public class UserPropertyCollection {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserPropertyCollection.class);
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String userId;
  private String collectionIdentifier;
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<UserProperty> properties = new HashSet<UserProperty>();

  /**
   * Default constructor.
   */
  public UserPropertyCollection() {
  }

  /**
   * Default constructor.
   *
   * @param pCollectionIdentifier The unique identifier of the property
   * collection.
   * @param pUserId The id of the associated user.
   */
  public UserPropertyCollection(String pCollectionIdentifier, String pUserId) {
    LOGGER.debug("Creating property collection with identifier {} for user {}", new Object[]{pCollectionIdentifier, pUserId});
    collectionIdentifier = pCollectionIdentifier;
    userId = pUserId;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the id.
   *
   * @return The id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the collection identifier.
   *
   * @param collectionIdentifier The collection identifier.
   */
  public void setCollectionIdentifier(String collectionIdentifier) {
    this.collectionIdentifier = collectionIdentifier;
  }

  /**
   * Get the collection identifier.
   *
   * @return The collection identifier.
   */
  public String getCollectionIdentifier() {
    return collectionIdentifier;
  }

  /**
   * Get the user id.
   *
   * @return The user id.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the user id.
   *
   * @param userId The user id.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Set the properties.
   *
   * @param properties The properties.
   */
  public void setProperties(Set<UserProperty> properties) {
    this.properties = properties;
  }

  /**
   * Get the properties.
   *
   * @return The properties.
   */
  public Set<UserProperty> getProperties() {
    return properties;
  }

  /**
   * Add a property.
   *
   * @param pProperty The property to add.
   */
  public void addProperty(UserProperty pProperty) {
    if (pProperty == null) {
      throw new IllegalArgumentException("Argument pProperty must not be null");
    }
    setStringProperty(pProperty.getPropertyKey(), pProperty.getPropertyValue());
  }

  /**
   * Returns the value of the property with the provided key as string. If the
   * property is not found, the provided default value is returned.
   *
   * @param pKey The property key.
   * @param pDefaultValue The default value.
   *
   * @return The value of the property or the provided default value.
   */
  public String getStringProperty(final String pKey, String pDefaultValue) {
    if (pKey == null) {
      throw new IllegalArgumentException("Argument pKey must not be null");
    }
    UserProperty existingProp = (UserProperty) CollectionUtils.find(properties, new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return ((UserProperty) o).getPropertyKey().equals(pKey);
      }
    });

    if (existingProp != null) {
      return existingProp.getPropertyValue();
    }
    return pDefaultValue;
  }

  /**
   * Returns the value of the property with the provided key as string. If the
   * property is not found, the provided default value is returned.
   *
   * @param pKey The property key.
   * @param pDefaultValue The default value.
   *
   * @return The value of the property or the provided default value.
   */
  public Long getLongProperty(final String pKey, Long pDefaultValue) {
    if (pDefaultValue == null) {
      return Long.parseLong(getStringProperty(pKey, null));
    }
    return Long.parseLong(getStringProperty(pKey, pDefaultValue.toString()));
  }

  /**
   * Returns the value of the property with the provided key as string. If the
   * property is not found, the provided default value is returned.
   *
   * @param pKey The property key.
   * @param pDefaultValue The default value.
   *
   * @return The value of the property or the provided default value.
   */
  public Float getFloatProperty(final String pKey, Float pDefaultValue) {
    if (pDefaultValue == null) {
      return Float.parseFloat(getStringProperty(pKey, null));
    }
    return Float.parseFloat(getStringProperty(pKey, pDefaultValue.toString()));
  }

  /**
   * Returns the value of the property with the provided key as string. If the
   * property is not found, the provided default value is returned.
   *
   * @param pKey The property key.
   * @param pDefaultValue The default value.
   *
   * @return The value of the property or the provided default value.
   */
  public Boolean getBooleanProperty(final String pKey, Boolean pDefaultValue) {
    if (pDefaultValue == null) {
      return Boolean.parseBoolean(getStringProperty(pKey, null));
    }
    return Boolean.parseBoolean(getStringProperty(pKey, pDefaultValue.toString()));
  }

  /**
   * Set the value of property with key pKey to pValue. If there is no property
   * with the provided key, a new property is added. If the provided value is
   * null, the property with the provided key will be removed.
   *
   * @param pKey The property key. This value must not be null.
   * @param pValue The property value. If pValue is null, the property will be
   * removed.
   *
   * @return TRUE if a property has been modified/added/removed, FALSE if the
   * removal failed.
   */
  public boolean setStringProperty(final String pKey, String pValue) {
    if (pKey == null) {
      throw new IllegalArgumentException("Argument pKey must not be null");
    }
    boolean result = true;

    if (pValue == null) {
      LOGGER.debug("Provided property value is null. Removing property.");
      return removeProperty(pKey);
    }

    LOGGER.debug("Setting property with key {} to value {}", new Object[]{pKey, pValue});
    UserProperty existingProp = (UserProperty) CollectionUtils.find(properties, new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return ((UserProperty) o).getPropertyKey().equals(pKey);
      }
    });
    if (existingProp == null) {
      LOGGER.debug("Adding new property");
      properties.add(new UserProperty(pKey, pValue));
    } else {
      LOGGER.debug("Updating existing property");
      existingProp.setPropertyValue(pValue);
    }
    return result;
  }

  /**
   * Set the value of property with key pKey to pValue. If there is no property
   * with the provided key, a new property is added. If the provided value is
   * null, the property with the provided key will be removed.
   *
   * @param pKey The property key. This value must not be null.
   * @param pValue The property value. If pValue is null, the property will be
   * removed.
   *
   * @return TRUE if a property has been modified/added/removed, FALSE if the
   * removal failed.
   */
  public boolean setLongProperty(final String pKey, Long pValue) {
    if (pValue == null) {
      return setStringProperty(pKey, null);
    }
    return setStringProperty(pKey, pValue.toString());
  }

  /**
   * Set the value of property with key pKey to pValue. If there is no property
   * with the provided key, a new property is added. If the provided value is
   * null, the property with the provided key will be removed.
   *
   * @param pKey The property key. This value must not be null.
   * @param pValue The property value. If pValue is null, the property will be
   * removed.
   *
   * @return TRUE if a property has been modified/added/removed, FALSE if the
   * removal failed.
   */
  public boolean setFloatProperty(final String pKey, Float pValue) {
    if (pValue == null) {
      return setStringProperty(pKey, null);
    }
    return setStringProperty(pKey, pValue.toString());
  }

  /**
   * Set the value of property with key pKey to pValue. If there is no property
   * with the provided key, a new property is added. If the provided value is
   * null, the property with the provided key will be removed.
   *
   * @param pKey The property key. This value must not be null.
   * @param pValue The property value. If pValue is null, the property will be
   * removed.
   *
   * @return TRUE if a property has been modified/added/removed, FALSE if the
   * removal failed.
   */
  public boolean setBooleanProperty(final String pKey, Boolean pValue) {
    if (pValue == null) {
      return setStringProperty(pKey, null);
    }
    return setStringProperty(pKey, pValue.toString());
  }

  /**
   * Remove the property with the provided key.
   *
   * @param pKey The key of the property.
   *
   * @return TRUE if the property has been removed, FALSE if there is no
   * property with the provided key.
   */
  public boolean removeProperty(final String pKey) {
    if (pKey == null) {
      throw new IllegalArgumentException("Argument pKey must not be null");
    }
    LOGGER.debug("Removing property with key {}", pKey);
    UserProperty existingProp = (UserProperty) CollectionUtils.find(properties, new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return ((UserProperty) o).getPropertyKey().equals(pKey);
      }
    });

    boolean result = true;

    if (existingProp != null) {
      properties.remove(existingProp);
      LOGGER.debug("Property removed");
    } else {
      LOGGER.debug("No property found for key {}", pKey);
      result = false;
    }
    return result;
  }
}
