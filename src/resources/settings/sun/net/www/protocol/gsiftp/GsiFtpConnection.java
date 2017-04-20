package sun.net.www.protocol.gsiftp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class represents a communication link via the GridFTP protocol.
 * Currently this implementation only provides the handling of the
 * gsiftp protocol when using a <a
 * href="http://java.sun.com/javase/6/docs/api/java/net/URL.html">URL</a>.
 * @author hartmann-v
 */
public class GsiFtpConnection extends URLConnection {

    /** The log4j logger*/
    private static final Logger LOGGER = LoggerFactory.getLogger(GsiFtpConnection.class);

    /**The constructor to instantiate the class.
     * 
     * @param pUrl The URL
     */
    public GsiFtpConnection(URL pUrl) {
        super(pUrl);
        LOGGER.debug("GsiFtpConnection successfully instantiated.");
    }

    /**This method overrides connect in class java.net.URLConnection
     * It is currently not implemented and throws only a UnsupportedOperationException.
     * 
     * @throws IOException If a IOException occurs by connecting
     */
    @Override
    public final void connect() throws IOException {
        //connect with server...
        throw new UnsupportedOperationException("Not yet implemented.");
        //GSIURLConnection connect = keine Ahnung wie hier ne Instanz erzeugt wird.
        //connect.connect();
    }
}
