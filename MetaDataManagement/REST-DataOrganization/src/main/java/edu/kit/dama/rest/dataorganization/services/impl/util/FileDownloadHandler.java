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

import com.sun.jersey.core.util.ReaderWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author jejkal
 */
public class FileDownloadHandler implements IDownloadHandler<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadHandler.class);

    @Override
    public Response prepareStream(File pFile) throws IOException {
        try {
            String contentType;
            try (FileInputStream is = new FileInputStream(pFile)) {
                LOGGER.debug("Trying to determine content type of file {}", pFile);
                Metadata metadata = new Metadata();
                ParseContext context = new ParseContext();
                AutoDetectParser parser = new AutoDetectParser();
                context.set(Parser.class, parser);
                ContentHandler handler = new BodyContentHandler(-1);
                // actually extract the metadata via Tika
                parser.parse(is, handler, metadata, context);
                contentType = metadata.get("Content-Type");
            }
            return Response.ok(new FileInputStream(pFile), contentType).build();
        } catch (SAXException | TikaException ex) {
            throw new IOException("Failed to prepare download stream for file " + pFile, ex);
        }
    }
}
