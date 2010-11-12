package org.mobicents.server.xdm.appusage.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Test;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.XcapResponse;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.error.NotUTF8ConflictException;
import org.openxdm.xcap.common.error.NotWellFormedConflictException;
import org.openxdm.xcap.common.xml.TextWriter;
import org.openxdm.xcap.common.xml.XMLValidator;
import org.openxdm.xcap.server.slee.appusage.rlsservices.RLSServicesAppUsage;
import org.w3c.dom.Document;

public class ResourceInterdependenciesTest extends AbstractT {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ResourceInterdependenciesTest.class);
	}
	
	private String user1 = "sip:john@example.com";
	private String user2 = "sip:doe@example.com";
	
	private String documentName = "index";
	
	@Test
	public void test() throws HttpException, IOException, JAXBException, InterruptedException, TransformerException, NotWellFormedConflictException, NotUTF8ConflictException, InternalServerErrorException, InstanceNotFoundException, MBeanException, ReflectionException, URISyntaxException, MalformedObjectNameException, NullPointerException, NamingException {
		
		initRmiAdaptor();

		try {
			createUser(user1,"password");
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		try {
			createUser(user2,"password");
		}
		catch (RuntimeMBeanException e) {
			if (!(e.getCause() instanceof IllegalStateException)) {
				e.printStackTrace();
			}
		}
		
		XcapClient client = new XcapClientImpl();
		
		Credentials credentials1 = new UsernamePasswordCredentials(user1, "password");
		Credentials credentials2 = new UsernamePasswordCredentials(user2, "password");
		
		// create uri		
		String documentSelector1 = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(RLSServicesAppUsage.ID,user1,documentName).toPercentEncodedString();
		String documentSelector2 = DocumentSelectorBuilder.getUserDocumentSelectorBuilder(RLSServicesAppUsage.ID,user2,documentName).toPercentEncodedString();
		
		UriBuilder uriBuilder = new UriBuilder()
			.setSchemeAndAuthority("http://localhost:8080")
			.setXcapRoot("/mobicents")
			.setDocumentSelector(documentSelector1);
		URI documentURI1 = uriBuilder.toURI();
		uriBuilder.setDocumentSelector(documentSelector2);
		URI documentURI2 = uriBuilder.toURI();
		
				
		// INSERT USER1 DOC
		// read document xml
		InputStream is1 = this.getClass().getResourceAsStream("example_resource_interdependencies_1.xml");
		Document document1 = XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(is1));
        String content1 = TextWriter.toString(document1);
		is1.close();
		// send put request and get response
		XcapResponse putResponse1 = client.put(documentURI1,RLSServicesAppUsage.MIMETYPE,content1,null,credentials1);
		// check put response
		System.out.println("Response got:\n"+putResponse1);
		assertTrue("Put response must exists",putResponse1 != null);
		assertTrue("Put response code should be 201",putResponse1.getCode() == 201);
				
		// INSERT USER2 DOC
		// read document xml
		InputStream is2 = this.getClass().getResourceAsStream("example_resource_interdependencies_2.xml");
		Document document2 = XMLValidator.getWellFormedDocument(XMLValidator.getUTF8Reader(is2));
        String content2 = TextWriter.toString(document2);
		is2.close();
		// send put request and get response
		XcapResponse putResponse2 = client.put(documentURI2,RLSServicesAppUsage.MIMETYPE,content2,null,credentials2);
		// check put response
		System.out.println("Response got:\n"+putResponse2);
		assertTrue("Put response must exists",putResponse2 != null);
		assertTrue("Put response code should be 201",putResponse2.getCode() == 201);
		
		client.delete(documentURI1,null,credentials1);
		client.delete(documentURI2,null,credentials2);
		client.shutdown();
		
		removeUser(user1);
		removeUser(user2);
	}
		
}