package nl.siegmann.epublib.epub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.Constants;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * Various low-level support methods for reading/writing epubs.
 * 
 * @author paul.siegmann
 *
 */
public class EpubProcessorSupport {
	
	private static final Logger log = Logger.getLogger(EpubProcessorSupport.class.getName());
	
	protected static DocumentBuilderFactory documentBuilderFactory;
	
	static {
		init();
	}

    static class EntityResolverImpl implements EntityResolver {
		private String previousLocation;
		
		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			String resourcePath;
			if (systemId.startsWith("http:")) {
				URL url = new URL(systemId);
				resourcePath = "resources/dtd/" + url.getHost() + url.getPath();
				previousLocation = resourcePath.substring(0, resourcePath.lastIndexOf('/'));
			} else {
				resourcePath = previousLocation + systemId.substring(systemId.lastIndexOf('/'));
			}
			
			if (this.getClass().getClassLoader().getResource(resourcePath) == null) {
				throw new RuntimeException("remote resource is not cached : [" + systemId + "] cannot continue");
			}

			InputStream in = EpubProcessorSupport.class.getClassLoader().getResourceAsStream(resourcePath);
			return new InputSource(in);
		}
	}
	
	
	private static void init() {
		EpubProcessorSupport.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
	}
	
	public static XmlSerializer createXmlSerializer(OutputStream out) throws UnsupportedEncodingException {
		return createXmlSerializer(new OutputStreamWriter(out, Constants.ENCODING));
	}
	
	public static XmlSerializer createXmlSerializer(Writer out) {
		XmlSerializer result = null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setValidating(true);
			result = factory.newSerializer();
			result.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			result.setOutput(out);
		} catch (Exception e) {
			log.log(Level.WARNING, " When creating XmlSerializer: " + e.getClass().getName() + ": " + e.getMessage(), e);
		}
		return result;
	}
	
	public DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactory;
	}

	/**
	 * Creates a DocumentBuilder that looks up dtd's and schema's from epublib's classpath.
	 * 
	 * @return
	 */
	public static DocumentBuilder createDocumentBuilder() {
		DocumentBuilder result = null;
		try {
			result = documentBuilderFactory.newDocumentBuilder();
			result.setEntityResolver(new EntityResolverImpl());
		} catch (ParserConfigurationException e) {
			log.warning(e.getMessage());
		}
		return result;
	}
}
