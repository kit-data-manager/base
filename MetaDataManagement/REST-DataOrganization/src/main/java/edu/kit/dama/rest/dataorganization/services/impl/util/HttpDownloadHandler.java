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
package edu.kit.dama.rest.dataorganization.services.impl.util;

import edu.kit.dama.util.DataManagerSettings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
public class HttpDownloadHandler implements IDownloadHandler<URL> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadHandler.class);

    @Override
    public Response prepareStream(URL pUrl) throws IOException {
        try {
            final CloseableHttpClient httpclient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods 
                    .build();

            HttpGet httpget = new HttpGet(pUrl.toURI());

            final CloseableHttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();
            String contentType = entity.getContentType().getValue();
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            LOGGER.debug("Using content type '{}' to stream URL content to client.", contentType);
            final InputStream is = entity.getContent();
            final int blockSize = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.DATA_ORGANIZATION_DOWNLOAD_BLOCK_SIZE, 10 * 1024);

            final StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    try {
                        byte[] data = new byte[blockSize];
                        int bytesRead;
                        while ((bytesRead = is.read(data)) != -1) {
                            os.write(data, 0, bytesRead);
                        }
                    } finally {
                        response.close();
                        httpclient.close();
                    }
                }
            };
            return Response.ok(stream, contentType).build();
        } catch (URISyntaxException ex) {
            throw new IOException("Failed to prepare download stream for URL " + pUrl, ex);
        }
    }
}
