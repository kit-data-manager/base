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
package edu.kit.dama.staging.entities;

import edu.kit.dama.commons.exceptions.InitializationError;
import edu.kit.dama.util.JSONUtils;
import edu.kit.lsdf.adalapi.protocols.interfaces.IExternalProtocolConfigurator;
import edu.kit.lsdf.adalapi.util.ProtocolSettings;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.json.JSONObject;

/**
 * Adalapi protocol configuration entity. This entity is for internal use only.
 * It allows to define Adalapi protocol configurations. These configurations are
 * loaded using the class edu.kit.dama.util.DatabaseProtocolConfiguration
 * implementing the IExternalProtocolConfigurator interface allowing to obtain
 * protocol configurations from external sources at runtime.
 *
 * Furthermore, it allows a more flexible way for distinguishing between
 * protocol instances. Here, the Adalapi only uses the protocol name which avoid
 * to be able to use two different protocol/authenticator implementations for
 * the same protocol identifier, e.g. http. This configuration implementation
 * uses a SHA1 hashed string consisting of protocol and authority component of a
 * URL that should be accessed by this configuration.
 *
 * Be aware that this identifier must be unique. This uniqueness is also
 * enforced by the underlying database. As for protocols accessing data via
 * local file access the identifier is always the same (see
 * getProtocolIdentifier() method), there must be only one instance of such a
 * configuration persisted.
 *
 * @author jejkal
 */
@Entity
public class AdalapiProtocolConfiguration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String identifier;
    @Column(nullable = false)
    private String protocolClass;
    @Column(nullable = false)
    private String authenticatorClass;
    private String customProperties;

    /**
     * ProtocolConfiguration factory method for creating a new
     * ProtocolConfiguration that can be persisted afterwards. Within this
     * method, a configuration identifier is generated using {@link #getProtocolIdentifier(java.net.URL)
     * }. All other arguments are checked and set in the returned entity.
     *
     * @param pUrl A sample URL used to generate the unique identifier.
     * @param protocolClass The protocol implementation class.
     * @param authenticatorClass The authenticator class.
     *
     * @return An AdalapiProtocolConfiguration.
     *
     * @throws InitializationError If e.g. protocol- or authenticator class
     * could not be found.
     */
    public final static AdalapiProtocolConfiguration factoryConfiguration(URL pUrl, String protocolClass, String authenticatorClass) throws InitializationError {
        return factoryConfiguration(pUrl, protocolClass, authenticatorClass, null);
    }

    /**
     * ProtocolConfiguration factory method for creating a new
     * ProtocolConfiguration that can be persisted afterwards. Within this
     * method, a configuration identifier is generated using {@link #getProtocolIdentifier(java.net.URL)
     * }. All other arguments are checked and set in the returned entity.
     *
     * @param pUrl A sample URL used to generate the unique identifier.
     * @param protocolClass The protocol implementation class.
     * @param authenticatorClass The authenticator class.
     * @param pCustomProperties Custom properties that can be used while
     * configuring both, the protocol and the authenticator.
     *
     * @return An AdalapiProtocolConfiguration.
     *
     * @throws InitializationError If e.g. protocol- or authenticator class
     * could not be found or if customProperties could not be handled.
     */
    public final static AdalapiProtocolConfiguration factoryConfiguration(URL pUrl, String protocolClass, String authenticatorClass, Map<String, Object> pCustomProperties) throws InitializationError {
        AdalapiProtocolConfiguration config = new AdalapiProtocolConfiguration();
        config.setIdentifier(getProtocolIdentifier(pUrl));

        try {
            Class protocolClazz = Thread.currentThread().getContextClassLoader().loadClass(protocolClass.trim());
            Constructor defaultConst;
            try {
                defaultConst = protocolClazz.getConstructor(new Class[]{URL.class, Configuration.class});
            } catch (NoSuchMethodException nsme) {
                defaultConst = protocolClazz.getConstructor(new Class[]{URL.class});
            }
            config.setProtocolClass(protocolClass);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new InitializationError("Invalid protocol class '" + protocolClass + "'.", ex);
        }
        try {
            Class authenticatorClazz = Thread.currentThread().getContextClassLoader().loadClass(authenticatorClass.trim());
            config.setAuthenticatorClass(authenticatorClass);
        } catch (ClassNotFoundException ex) {
            throw new InitializationError("Invalid authenticator class '" + authenticatorClass + "'.", ex);
        }

        config.setCustomPropertiesAsObject(pCustomProperties);
        return config;
    }

    /**
     * Get the unique protocol identifier for the provided Url. The identifier
     * is generated using the schema:
     *
     * protocol[@host][:port]
     *
     * Valid identifiers according to this schema are e.g. http@myHost;
     * http@myHost:8080; ftp@anotherHost; file
     *
     * As there is no host/port information for Urls accessed via file protocol,
     * there is only one valid identifier for file Urls.
     *
     * @param pUrl A sample URL (protocol and authority are sufficient, e.g.
     * http://remoteHost:8080) as it should be accessed by the provided protocol
     * implementation.
     *
     * @return The identifier string.
     */
    public final static String getProtocolIdentifier(URL pUrl) {
        String protocol = pUrl.getProtocol();
        if (protocol == null) {
            throw new IllegalArgumentException("The provided Url " + pUrl + " has no protocol specified.");
        }
        String host = pUrl.getHost();
        int port = pUrl.getPort();

        if (host == null) {
            return protocol;
        } else {
            return protocol + "@" + host + ((port > -1) ? ":" + Integer.toString(port) : "");
        }
    }

    /**
     * Default constructor. For creating new ProtocolConfigurations for
     * persistence {@link #factoryConfiguration(java.net.URL, java.lang.String, java.lang.String, java.util.Map)
     * } should be used instead of invoking this constructor directly as the
     * factory method will create a proper identifier.
     */
    public AdalapiProtocolConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    protected void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getProtocolClass() {
        return protocolClass;
    }

    public void setProtocolClass(String protocolClass) {
        this.protocolClass = protocolClass;
    }

    public String getAuthenticatorClass() {
        return authenticatorClass;
    }

    public void setAuthenticatorClass(String authenticatorClass) {
        this.authenticatorClass = authenticatorClass;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * Set the provided custom properties. The argument is serialized into a
     * single JSON string in order to allow to persist it in the database.
     *
     * @param customProperties The key-value properties map.
     */
    public void setCustomPropertiesAsObject(Map<String, Object> customProperties) {
        this.customProperties = JSONUtils.mapToJson(customProperties).toString();
    }

    /**
     * Get the custom properties. The result is serialized from a single JSON
     * string that can be obtained via {@link #getCustomProperties() }.
     *
     * @return The custom properties map.
     */
    public Map<String, Object> getCustomPropertiesAsObject() {
        return JSONUtils.jsonToMap(new JSONObject(this.customProperties));
    }

    /**
     * Convert this entity in an ADALAPI-compliant configuration. Therefor, all
     * attributes and custom properties are put into a Configuration object as
     * described in the IExternalProtocolConfigurator interface.
     *
     * @return The configuration object.
     *
     * @see IExternalProtocolConfigurator
     */
    public Configuration toConfiguration() {
        Configuration config = new HierarchicalConfiguration();
        config.addProperty(ProtocolSettings.PROTOCOL_IDENTIFIER_PROPERTY, identifier);
        config.addProperty(ProtocolSettings.PROTOCOL_CLASS_PROPERTY, protocolClass);
        config.addProperty(ProtocolSettings.AUTH_CLASS_PROPERTY, authenticatorClass);
        Map<String, Object> props = getCustomPropertiesAsObject();
        for (String o : props.keySet()) {
            config.addProperty(o, props.get(o));
        }
        return config;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Identifier ").append(getIdentifier()).append("\n");
        builder.append("ProtocolImpl ").append(getProtocolClass()).append("\n");
        builder.append("Authenticator ").append(getAuthenticatorClass()).append("\n");
        Map<String, Object> properties = getCustomPropertiesAsObject();
        for (String key : properties.keySet()) {
            builder.append(key).append(" ").append(properties.get(key)).append("\n");
        }

        return builder.toString();
    }

}
