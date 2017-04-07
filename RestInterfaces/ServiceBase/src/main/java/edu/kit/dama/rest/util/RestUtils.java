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
package edu.kit.dama.rest.util;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.rest.base.exceptions.DeserializationException;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.AuthenticatorFactory;
import edu.kit.dama.rest.util.auth.exception.AuthenticatorDisabledException;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import edu.kit.dama.util.Constants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class RestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    /**
     * Hiden constructor.
     */
    private RestUtils() {
    }

    /**
     * Perform an OAuth authorization based on the provided HttpContext. This
     * method maps to authenticate(HttpContext hc, GroupId pGroupId) but uses
     * the default user group as group id.
     *
     * @param hc The HttpContext used to extract access key and secret.
     *
     * @return The authorization context.
     */
    public static IAuthorizationContext authorize(HttpContext hc) {
        return authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    }

    /**
     * Perform an OAuth authorization based on the provided HttpContext for the
     * provided GroupId. This method extracts access key and secret from the
     * request and obtains the user id for these two tokens. If a user id could
     * be determined, an authorization context is created together with the
     * provided group id and the max. user role in this group. This call fails
     * if access key and token do not match, if no user id is found for the
     * provided access tokens or if the obtained user is no member of the group
     * with the provided id. In all these cases a WebServiceException is thrown.
     *
     * @param hc The HttpContext used to extract access key and secret.
     * @param pGroupId The group id used to build the authorization context.
     *
     * @return The authorization context including pGroupId and the according
     * max. role of the obtained user.
     */
    public static IAuthorizationContext authorize(HttpContext hc, GroupId pGroupId) {
        if (pGroupId == null || pGroupId.getStringRepresentation() == null) {
            throw new IllegalArgumentException(
                    "Argument pGroupId and its string representation must not be 'null'");
        }

        int authCnt = 1;
        Iterator<AbstractAuthenticator> authenticators = AuthenticatorFactory.getInstance().getAuthenticators().iterator();
        while (authenticators.hasNext()) {
            AbstractAuthenticator auth = authenticators.next();
            LOGGER.debug("Authentication attempt #{}, authenticator '{}'", authCnt, auth.getAuthenticatorId());
            try {
                return auth.authenticate(hc, pGroupId);
            } catch (MissingCredentialException ex) {
                LOGGER.trace("Authentication not possible due to missing credentials. " + ((authenticators.hasNext()) ? "Processing with next authenticator." : "No more authenticators available."), ex);
                authCnt++;
            } catch (AuthenticatorDisabledException ex) {
                LOGGER.trace("Authentication not possible due to disabled authenticator. " + ((authenticators.hasNext()) ? "Processing with next authenticator." : "No more authenticators available."), ex);
                authCnt++;
            } catch (UnauthorizedAccessAttemptException ex) {
                //credentials were found, authenticator is enabled for the endpoint, but authentication really failed....return NULL
                LOGGER.error("Authentication failed. Aborting process and returning no authorization context.", ex);
                return null;
            }
        }

        //if we arrive here, no authenticator worked out but also no error occured. Provide access as 'WORLD'.
        LOGGER.debug("Returning authorization context for 'WORLD'");
        return new AuthorizationContext(new UserId(Constants.WORLD_USER_ID), new GroupId(Constants.WORLD_GROUP_ID), Role.GUEST);
    }

    /**
     * Serializes one or more entities of the same type to an XML object graph
     * to a stream which is returned. This method can be returned directly by a
     * REST call. On the client side the deserialization can take place
     * according to the object graph definition.
     *
     * @param pEntityClass An array of classes of supported entities.
     * @param pGraphName The object graph name used to serialize the object. If
     * no graph name is provided the entire object is serialized.
     * @param pEntities One or more entities serialized and written to the
     * returned StreamingOutput.
     *
     * @return The StreamingOutput which can be returned by a REST method.
     */
    public static StreamingOutput createObjectGraphStream(
            final Class pEntityClass[], final String pGraphName,
            final Object... pEntities) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream arg0) {
                try {
                    LOGGER.debug("Performing marshalling");
                    Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    if (pGraphName != null) {
                        marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, pGraphName);
                    }
                    marshaller.marshal((pEntities.length == 1) ? pEntities[0] : Arrays.asList(pEntities), arg0);
                    LOGGER.debug("Marshalling finished. Flushing output stream.");
                    arg0.flush();
                    LOGGER.debug("Output flushed.");
                } catch (IOException e) {
                    LOGGER.error("Failed to write result to output stream.", e);
                } catch (JAXBException e) {
                    LOGGER.error("Failed to marshal result to output stream.", e);
                } catch (ClassCastException e) {
                    LOGGER.error(
                            "A passed entity is no instance of any provided class", e);
                }
            }
        };
    }

    /**
     * Serializes one or more entities of the same type to an XML object graph
     * to a stream which is returned. This method can be returned directly by a
     * REST call. On the client side the deserialization can take place
     * according to the object graph definition.
     *
     * @param pEntityClass The class of the entity to serialize.
     * @param pGraphName The object graph name used to serialize the object. If
     * no graph name is provided the entire object is serialized.
     * @param pEntities One or more entities serialized and written to the
     * returned StreamingOutput.
     *
     * @return The StreamingOutput which can be returned by a REST method.
     */
    public static StreamingOutput createObjectGraphStream(
            final Class pEntityClass, final String pGraphName,
            final Object... pEntities) {
        return createObjectGraphStream(new Class[]{pEntityClass}, pGraphName,
                pEntities);
    }

    /**
     * Deserializes an entity from a stream provided by a ClientResponse. This
     * method throws a DeserializationException if the deserialization fails for
     * some reason.
     *
     * @param <C> The response object type.
     * @param pEntityClass The class of the entity to deserialize.
     * @param pResponse The response which provides the entity input stream.
     *
     * @return The object.
     */
    public static <C> C createObjectFromStream(final Class pEntityClass,
            final ClientResponse pResponse) {
        try {
            Unmarshaller unmarshaller
                    = org.eclipse.persistence.jaxb.JAXBContext.newInstance(
                            pEntityClass).createUnmarshaller();
            return (C) unmarshaller.unmarshal(pResponse.getEntityInputStream());
        } catch (JAXBException ex) {
            throw new DeserializationException(
                    "Failed to deserialize object of type " + pEntityClass
                    + " from response " + pResponse, ex);
        }
    }

    public static <C> C transformObject(final Class pEntityClass[], final String pGraphName,
            final Object pEntity) {

        try {
            LOGGER.debug("Performing marshalling of object.");
            Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (pGraphName != null) {
                marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, pGraphName);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            marshaller.marshal(pEntity, bout);
            LOGGER.debug("Marshalling finished. Flushing output stream.");
            bout.flush();
            LOGGER.debug("Output flushed. Converting XML result '{}' back to object.", new String(bout.toByteArray()));
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            Unmarshaller unmarshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(
                    pEntityClass).createUnmarshaller();
            return (C) unmarshaller.unmarshal(bin);
        } catch (IOException e) {
            LOGGER.error("Failed to write result to output stream.", e);
        } catch (JAXBException e) {
            LOGGER.error("Failed to marshal result to output stream.", e);
        } catch (ClassCastException e) {
            LOGGER.error("A passed entity is no instance of any provided class", e);
        }
        return null;
    }

    /**
     * Check the service access for being local. This check should be done as a
     * first step of every checked service method. In order to get the necessary
     * HttpServletRequest as well as the ResourceConfig both have to be injected
     * as arguments and must be annotates as <i>@javax.ws.rs.core.Context</i>
     * within the according REST interface method.
     *
     * In addition, a provided HttpContext can be annotated as
     * <i>javax.ws.rs.core.Context</i> for OAuth checks.
     *
     * To check the access <i>servletRequest.getRemoteAddr()</i> is compared to
     * a semicolon separated list of IPs defined as an init-param named
     * 'allow.from' in the web.xml. The param is accessed via the provided
     * <i>resource</i>
     * argument. If this init-param is not defined, only access from 127.0.0.1
     * will be granted.
     *
     * If the obtained remote address cannot be mapped to any IP in 'allow.from'
     * a WebApplicationException with error code 403 (HTTP.FORBIDDEN) is thrown.
     * Otherwise, this method will return without any additional output.
     *
     * @param servletRequest The request used to check the remote host.
     * @param resource The resource config used to obtain the init-param
     * containing a list of allowed IPs.
     */
    public static void checkAccess(javax.servlet.http.HttpServletRequest servletRequest, ResourceConfig resource) {
        boolean accessAllowed = false;

        String allowFrom = (String) resource.getProperty("allow.from");
        String[] allowedIps;
        if (allowFrom == null) {
            allowedIps = new String[]{"127.0.0.1"};
        } else {
            allowedIps = allowFrom.trim().split(";");
        }

        LOGGER.debug("Checking service access authorization.");
        if (servletRequest != null && servletRequest.getRemoteAddr() != null) {
            LOGGER.debug("Checking {} allowed IPs against remote host {}", allowedIps.length, servletRequest.getRemoteAddr());
            for (String a : allowedIps) {
                LOGGER.debug("Checking allowedIp {}", a);
                try {
                    InetAddress addr = InetAddress.getByName(servletRequest.getRemoteAddr());
                    String h1 = addr.getCanonicalHostName();
                    String h2 = InetAddress.getByName(a).getCanonicalHostName();
                    LOGGER.debug("Comparing hostnames {} and {}", h1, h2);
                    if (h1.equals(h2)) {
                        LOGGER.debug("Service access granted for IP {}.", a);
                        accessAllowed = true;
                        break;
                    }
                } catch (UnknownHostException ex) {
                    LOGGER.error("Failed to check remote host for beeing local.");
                }
            }
        } else {
            LOGGER.warn("ServletRequest is either null or remote host is not provided. Access not allowed.");
        }
        LOGGER.debug("Access is: {}", accessAllowed);
        if (!accessAllowed) {
            throw new WebApplicationException(403);
        }
    }

}
