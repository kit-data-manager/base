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
package edu.kit.dama.staging.adalapi.protocol;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.lsdf.adalapi.protocols.AccessProtocol;
import edu.kit.lsdf.adalapi.protocols.WebDav;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class SimpleHttp extends AccessProtocol<CloseableHttpClient> {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDav.class);
    /**
     * The associated protocol URL
     */
    private URL protocolURL = null;
    /**
     * Use data compression
     */
    private boolean useCompression = false;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_DOCUMENT_NOT_FOUND_ERROR = 404;
    private static final int READ_BUFFER_SIZE = 1024;
    private int bufferSize = READ_BUFFER_SIZE;

    /**
     * The default constructor for providing a specific configuration
     *
     * @param pURL The url which will be accessed via WebDav
     * @param pConfiguration The custom configuration of the protocol
     *
     * @see AccessProtocol
     *
     * @throws AdalapiException If instantiation fails
     */
    public SimpleHttp(URL pURL, Configuration pConfiguration) throws AdalapiException {
        super(pURL, pConfiguration);
    }

    @Override
    public final void initializeProtocol(URL pUrl) throws AdalapiException {
        LOGGER.debug("Initializing WebDav protocol for URL {}", pUrl);
        protocolURL = pUrl;
    }

    @Override
    public final void configure(Configuration pConfiguration) {
        //allow to set socket timeout for connection (default: 30s)
        useCompression = pConfiguration.getBoolean("compression", Boolean.FALSE);
        bufferSize = pConfiguration.getInt("bufferSize", READ_BUFFER_SIZE);
        if (useCompression) {
            LOGGER.debug("Using compression");
        }
        LOGGER.debug("Special WebDav protocol features:: Compression enabled: {}, Buffer size: {}", new Object[]{useCompression, bufferSize});
    }

    @Override
    public final CloseableHttpClient connectProtocolClient() throws AdalapiException {
        final CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods 
                .build();

        /* AbstractAuthentication auth = getAuthenticationMethod();

        if (!auth.isConnected()) {
            LOGGER.debug("Authentication is not connected, requesting user interaction");
            try {
                auth.authenticate();
            } catch (AuthenticationInputMethodException uaim) {
                throw new AdalapiSecurityException("Failed to authenticate", uaim);
            }
            LOGGER.debug("User interaction finished. Trying to connect protocol client");
        }
         */
        return httpclient;
    }

    @Override
    public final void reconnect() throws AdalapiException {
        //not available
    }

    @Override
    public final void close() {
        //not supported
    }

    @Override
    public boolean isUTF8Supported() {
        return true;
    }
    //<editor-fold defaultstate="collapsed" desc=" Transfer operations">

    @Override
    public final void downloadFileToFileInternal(URL pServerFile, URL pLocalFile) throws AdalapiException {
        LOGGER.debug("Downloading file '{}' to file '{}'", new Object[]{pServerFile, pLocalFile});
        CloseableHttpResponse response = null;

        try {
            HttpGet httpget = new HttpGet(pServerFile.toURI());
            response = getProtocolClient().execute(httpget);

            HttpEntity entity = response.getEntity();
            String contentType = entity.getContentType().getValue();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            LOGGER.debug("Using content type '{}' to stream URL content to client.", contentType);
            final InputStream is = entity.getContent();
            final OutputStream os = new FileOutputStream(new File(pLocalFile.toURI()));

            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(data)) != -1) {
                os.write(data, 0, bytesRead);
            }
        } catch (IOException | URISyntaxException ex) {
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {

                }
            }
        }

    }

    @Override
    public final AbstractFile uploadFileToFileInternal(URL pLocalFile, URL pRemoteFile) throws AdalapiException {
        //not supported
        throw new UnsupportedOperationException("Upload is not supported for this protocol implementation.");
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc=" Check operations ">
    @Override
    public final boolean isDirectory(URL pURL) throws AdalapiException {
        LOGGER.info("IsDirectory check not supported by SimpleHttp. Returning 'FALSE'");
        return false;
    }

    @Override
    public final boolean exists(URL pURL) throws AdalapiException {
        LOGGER.info("Existence check not supported by SimpleHttp. Returning 'TRUE'");
        return true;
    }

    @Override
    public final long getSize(URL pURL) throws AdalapiException {
        return 0;
    }

    @Override
    public final long getLastModified(URL pURL) throws AdalapiException {
        return 0;
    }

    @Override
    public final boolean isWriteable(URL pURL) throws AdalapiException {
        LOGGER.info("Writeable check not supported by SimpleHttp. Returning 'FALSE'");
        return false;
    }

    @Override
    public final boolean isReadable(URL pURL) throws AdalapiException {
        LOGGER.info("Readable check not supported by SimpleHttp. Returning 'TRUE'");
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" View and manage operations ">
    @Override
    public final Collection<URL> list(URL pDirectory) throws AdalapiException {
        LOGGER.info("Listing check not supported by SimpleHttp. Returning empty list.");
        return new ArrayList();
    }

    @Override
    public final Collection<AbstractFile> list(AbstractFile pDirectory) throws AdalapiException {
        LOGGER.info("Listing check not supported by SimpleHttp. Returning empty list.");
        return new ArrayList();
    }

    @Override
    public final AbstractFile createDirectory(URL pDirectory) throws AdalapiException {
        throw new UnsupportedOperationException("createDirectory is not supported for this protocol implementation.");
    }

    @Override
    public final void deleteFile(URL pUrl) throws AdalapiException {
        //not supported
    }

    @Override
    public final void deleteDirectory(URL pUrl) throws AdalapiException {
        //not supported
    }

    //</editor-fold>
}
