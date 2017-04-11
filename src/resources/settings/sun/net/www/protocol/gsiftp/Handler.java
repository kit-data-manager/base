package sun.net.www.protocol.gsiftp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * The class for handling the stream protocol handlers. A stream protocol
 * handler knows how to make a connection for a particular protocol type,
 * which is gsiftp in this case.
 * @author hartmann-v
 */
public class Handler extends URLStreamHandler {

    /**This method overrides openConnection in class java.net.URLStreamHandler
     * 
     * @param u The URL
     * 
     * @return The URL connection
     * 
     * @throws IOException If a IOException occurs opening the connection
     */
    @Override
    protected final URLConnection openConnection(URL u) throws IOException {
        return new GsiFtpConnection(u);
    }
}
