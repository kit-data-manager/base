/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
import com.sun.jersey.api.client.WebResource;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.exceptions.DeserializationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for REST calls.
 *
 * @author mf6319
 */
public final class RestClientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientUtils.class);
    private static final String FORM_PARAMETERS = "Form parameters: ";

    /**
     * Constructor shouldn't be call as all methods are static.
     */
    private RestClientUtils() {
    }

    /**
     * Prepare web resource with path and parameters
     *
     * @param pWebResource instance of webresource.
     * @param pQueryParams parameters of resource.
     * @return new web resource.
     */
    public static WebResource prepareWebResource(WebResource pWebResource, MultivaluedMap pQueryParams) {
        WebResource returnValue = pWebResource;
        if (pQueryParams != null) {
            returnValue = pWebResource.queryParams(pQueryParams);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(returnValue.getURI().toString());
        }
        return returnValue;
    }

    /**
     * Perform a GET request on the qiven path with the given parameters.
     *
     * @param <C> entity class
     * @param pEntityClass The class of the entity to deserialize.
     * @param pWebResource instance of webresource.
     * @param pQueryParams url parameters
     * @return client response
     */
    public static <C> C performGet(Class<C> pEntityClass, WebResource pWebResource, MultivaluedMap pQueryParams) {
        ClientResponse returnValue;
        WebResource webResource = prepareWebResource(pWebResource, pQueryParams);
        returnValue = webResource.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
        return createObjectFromStream(pEntityClass, returnValue);
    }

    /**
     * Perform a GET request on the qiven path with the given parameters.
     *
     * @param <C> entity class
     * @param pEntityClasses An array of classes needed to deserialize the
     * entity.
     * @param pWebResource instance of webresource.
     * @param pQueryParams url parameters
     * @return client response
     */
    public static <C> C performGet(Class[] pEntityClasses, WebResource pWebResource, MultivaluedMap pQueryParams) {
        ClientResponse returnValue;
        WebResource webResource = prepareWebResource(pWebResource, pQueryParams);
        returnValue = webResource.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
        return createObjectFromStream(pEntityClasses, returnValue);
    }

    /**
     * Perform a POST request on the qiven path with the given parameters.
     *
     * @param <C> entity class.
     * @param pEntityClass The class of the entity to deserialize.
     * @param pWebResource instance of webresource.
     * @param pQueryParams url parameters
     * @param pFormParams form parameters
     * @return client response
     */
    public static <C> C performPost(Class<C> pEntityClass, WebResource pWebResource,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        ClientResponse returnValue;
        WebResource webResource = prepareWebResource(pWebResource, pQueryParams);
        logFormParams(pFormParams);
        returnValue = webResource.post(ClientResponse.class, pFormParams);
        return createObjectFromStream(pEntityClass, returnValue);
    }

    /**
     * Perform a POST request on the qiven path with the given parameters.
     *
     * @param <C> entity class
     * @param pEntityClass The class of the entity to deserialize.
     * @param pWebResource instance of webresource.
     * @param pQueryParams url parameters
     * @param pFormParams form parameters
     * @return client response
     */
    public static <C> C performPut(Class<C> pEntityClass, WebResource pWebResource,
            MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        ClientResponse returnValue;
        WebResource webResource = prepareWebResource(pWebResource, pQueryParams);
        logFormParams(pFormParams);
        returnValue = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).put(ClientResponse.class, pFormParams);
        return createObjectFromStream(pEntityClass, returnValue);
    }

    /**
     * Perform a DELETE request on the qiven path with the given parameters.
     *
     * @param <C> entity class
     * @param pEntityClass The class of the entity to deserialize.
     * @param pWebResource instance of webresource.
     * @param pQueryParams url parameters
     * @return client response
     */
    public static <C> C performDelete(Class<C> pEntityClass, WebResource pWebResource, MultivaluedMap pQueryParams) {
        ClientResponse returnValue;
        WebResource webResource = prepareWebResource(pWebResource, pQueryParams);
        returnValue = webResource.type(MediaType.APPLICATION_XML).delete(ClientResponse.class);
        return createObjectFromStream(pEntityClass, returnValue);
    }

    /**
     * Deserializes an entity from a stream provided by a ClientResponse.
     * <b>Attention:</b>May throw a DeserializationException if the
     * deserialization fails for some reason. In some cases, pEntityClass might
     * be 'null', e.g. if no deserializable output is expected. In this cases it
     * is possible, to obtain the response object by setting the return type C
     * to ClientResponse. If this is the case and pEntityClass is null,
     * pResponse will be returned.
     *
     * @param <C> entity class of ClientResponse if pResponse should be
     * returned.
     * @param pEntityClass The class of the entity to deserialize or null if no
     * deserializable result is expected.
     * @param pResponse The response which provides the entity input stream and
     * the HTTP result.
     *
     * @return The deserialized object or pResponse.
     */
    public static <C> C createObjectFromStream(final Class<C> pEntityClass, final ClientResponse pResponse) {
        C returnValue = null;
        if (pResponse == null) {
            throw new WebServiceException("Failed to create object from stream. No response availabe! (response == null)");
        }
        if (pResponse.getStatus() != 200) {
            //error ... do not try to deserialize the result.
            try {
                String data = new String(IOUtils.toByteArray(pResponse.getEntityInputStream()));
                String tmp = FileUtils.getTempDirectoryPath();
                String extension = ".html";
                if (!data.contains("<html>")) {
                    extension = ".log";
                }
                String outputFile = FilenameUtils.concat(tmp, "kitdm_rest_" + System.currentTimeMillis() + extension);

                try (FileOutputStream fout = new FileOutputStream(outputFile)) {
                    fout.write(data.getBytes());
                    fout.flush();
                }

                throw new WebServiceException("Failed to create object from stream. Service call returned status " + pResponse.getStatus() + ". More information available at: " + outputFile);
            } catch (IOException ex) {
                throw new WebServiceException("Failed to create object from stream. Service call returned status " + pResponse.getStatus() + ". Cause: See server log.");
            }
        } else if (pEntityClass != null) {
            return pResponse.getEntity(pEntityClass);
            /* try {
                Unmarshaller unmarshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createUnmarshaller();
                returnValue = (C) unmarshaller.unmarshal(getInputStream(pResponse.getEntityInputStream()));
                if (LOGGER.isDebugEnabled()) {
                    Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(returnValue, sw);
                    LOGGER.debug("createObjectFromStream: " + sw.toString());
                }
            } catch (JAXBException ex) {
                throw new DeserializationException("Failed to deserialize object of type " + pEntityClass + " from response " + pResponse, ex);
            }*/
        } else {
            try {
                //check if the ClientResponse is expected as result...
                returnValue = (C) pResponse;
            } catch (ClassCastException e) {
                LOGGER.debug("No response expected!");
                returnValue = null;
            }
        }
        return returnValue;
    }

    /**
     * Deserializes an entity from a stream provided by a ClientResponse.
     * <b>Attention:</b>May throw a DeserializationException if the
     * deserialization fails for some reason.
     *
     * @param <C> entity class
     * @param pEntityClass An array of classes needed to deserialize the entity.
     * @param pResponse The response which provides the entity input stream.
     *
     * @return The object.
     */
    public static <C> C createObjectFromStream(final Class[] pEntityClass, final ClientResponse pResponse) {
        C returnValue = null;
        if (pEntityClass != null) {
            LOGGER.debug("createObjectFromStream");
            try {
                Unmarshaller unmarshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createUnmarshaller();
                returnValue = (C) unmarshaller.unmarshal(getInputStream(pResponse.getEntityInputStream()));
                if (LOGGER.isDebugEnabled()) {
                    Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(pEntityClass).createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(returnValue, sw);
                    LOGGER.debug("createObjectFromStream: " + sw.toString());
                }
            } catch (JAXBException ex) {
                throw new DeserializationException("Failed to deserialize object from response " + pResponse, ex);
            }
        } else {
            LOGGER.debug("No response expected!");
        }
        return returnValue;
    }

    /**
     * Check for log level and log inputstream if necessary.
     *
     * @param stream inputstream to be logged.
     * @return inputstream
     */
    private static InputStream getInputStream(InputStream stream) {
        InputStream returnValue = stream;
        if (LOGGER.isDebugEnabled()) {
            byte[] data = new byte[0];
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                byte[] block = new byte[4096]; // for a 4k block size
                int count;

                while ((count = stream.read(block)) != -1) {
                    baos.write(block, 0, count);
                }
                data = baos.toByteArray();
                LOGGER.debug("Client response: " + new String(data));
            } catch (IOException ioe) {
                LOGGER.error(null, ioe);
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException ex) {
                    }
                }

                returnValue = new ByteArrayInputStream(data);
            }
        }
        return returnValue;
    }

    /**
     * Build REST URL from a given pattern and its values. There is no check for
     * correct number of arguments. While generating URL all arguments will
     * encoded to be in a correct format. E.g.: 'stupid example' will be
     * transformed to 'stupid%20example'.
     *
     * @param pattern URL with any number of place holders for arguments
     * @param arguments array of arguments for the place holders
     * @return encoded URL
     */
    public static String encodeUrl(String pattern, Object... arguments) {
        List<Object> urlArg = new ArrayList<>();
        for (Object arg : arguments) {
            if (arg == null) {
                throw new IllegalArgumentException("Null-values not supported for any element of 'arguments'.");
            }
            if (arg instanceof Number) {
                // everyting should work fine!
                NumberFormat instance = NumberFormat.getInstance(Locale.US);
                instance.setGroupingUsed(false);
                instance.setMaximumFractionDigits(9);
                String number = instance.format(arg);
                urlArg.add(number);
            } else if (arg instanceof String) {
                urlArg.add(encode((String) arg));
            } else {
                LOGGER.warn("Uncovered argument type {}", arg);
            }
        }
        String returnValue = MessageFormat.format(pattern, urlArg.toArray());

        LOGGER.debug("Final URL: {}", returnValue);

        return returnValue;
    }

    /**
     * Encode a string to be URL conform.
     *
     * @param argument string to be encoded
     *
     * @return url encoded string
     */
    private static String encode(String argument) {
        String returnValue = null;
        try {
            URI uri = new URI("http", "my.site.com", "/" + argument, null);
            returnValue = uri.toURL().getPath().substring(1);
        } catch (URISyntaxException | MalformedURLException ex) {
            LOGGER.error("Failed to encode string " + argument, ex);
        }
        return returnValue;
    }

    /**
     * Write form parameters to log.
     *
     * @param pQueryParams map with the parameters.
     */
    private static void logFormParams(MultivaluedMap pQueryParams) {
        if (LOGGER.isDebugEnabled() && (pQueryParams != null)) {
            StringBuilder sb = new StringBuilder(FORM_PARAMETERS);
            boolean firstEntry = true;
            final Iterator<Entry<String, List<String>>> iterator = pQueryParams.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, List<String>> nextEntry = iterator.next();
                String key = nextEntry.getKey();
                if (firstEntry) {
                    firstEntry = false;
                } else {
                    sb.append("&");
                }
                sb.append((String) key).append("=");
                boolean firstValue = true;
                for (String value : (List<String>) nextEntry.getValue()) {
                    if (firstValue) {
                        firstValue = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append((value));
                }
            }
            LOGGER.debug(sb.toString());
        }
    }

//  public static void main(String[] args) {
//    System.out.println(RestClientUtils.encodeUrl("/studies/{0}/investigations/", new Long(0l)));
//  }
}
