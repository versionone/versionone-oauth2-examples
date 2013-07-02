package com.versionone.oauthclient.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.versionone.oauthclient.webapp.jsonfilesecrets.JsonFileRepository;

public class AuthorizedWebApp extends AbstractAuthorizationCodeServlet {

	/** Client secrets from the VersionOne application instance */
    private static IClientSecrets secrets;
    
	private static final String APP_NAME = "VersionOne OAuth2 Sample Web Client";

	private static final long serialVersionUID = 1L;
	
	/** Expected XML elements */
	private static final String ASSETS = "Assets";
	private static final String ASSET = "Asset";
	private static final String ATTRIBUTE = "Attribute";
	private static final String RELATION = "Relation";
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.println("<!doctype html><html><head>");
		writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		writer.println("<title>" + APP_NAME + "</title>");
		writer.println("</head><body>");
		
		final Credential v1credential = this.getCredential();
		HttpRequestFactory requestFactory = Utils.HTTP_TRANSPORT
				.createRequestFactory(new HttpRequestInitializer() {
					public void initialize(HttpRequest request)
							throws IOException {
						v1credential.initialize(request);
					}
				});
		GenericUrl v1url = new GenericUrl(secrets.getServerBaseUri());
		// Add the OAuth API end-point
        v1url.getPathParts().add("rest-1.oauth.v1");
        // Add a simple data query for the currently logged in member
        v1url.getPathParts().add("Data");
        v1url.getPathParts().add("Member");
        v1url.set("where", "IsSelf=\'true\'");

        // Send request to VersionOne and print the results.
        HttpRequest v1request = requestFactory.buildGetRequest(v1url);
        HttpResponse v1response = v1request.execute();
        printMemberDetails(writer, v1response);
		writer.println("</body></html>");
	}

	@Override
	protected String getRedirectUri(HttpServletRequest request) throws ServletException, IOException {
	    return Utils.getRedirectUri(request);
    }

	@Override
	protected String getUserId(HttpServletRequest arg0) throws ServletException, IOException {
		return Utils.USER_ID;
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		secrets = new JsonFileRepository(Utils.JSON_FACTORY).loadClientSecrets(AuthorizedWebApp.class);
		return Utils.newFlow(secrets);
	}

	private BufferedReader printMemberDetails(PrintWriter writer, HttpResponse v1response) throws IOException,
			FactoryConfigurationError {
		BufferedReader in = new BufferedReader(new InputStreamReader(v1response.getContent()));
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					if (startElement.getName().getLocalPart()==ASSETS) {
						writer.println("<h2>" + startElement.getName().getLocalPart() + "</h2>");
						printAttributes(writer, startElement);
					} else if (startElement.getName().getLocalPart()==ASSET) {
						writer.println("<p>" + startElement.getName().getLocalPart() + ":</p>");
						printAttributes(writer, startElement);						
					} else if (startElement.getName().getLocalPart()==ATTRIBUTE) {
						Attribute aName = (Attribute) startElement.getAttributes().next();
						event = eventReader.nextEvent();
						if (event.isEndElement()) {
							writer.println("<p>" + aName.getValue().toString() + "</p>");
						} else {
							writer.println("<p>" + aName.getValue().toString() + "=" + event.asCharacters().getData() + "</p>");
						}
					} else if (startElement.getName().getLocalPart()==RELATION) {
						Attribute aName = (Attribute) startElement.getAttributes().next();
						event = eventReader.nextEvent();
						writer.println("<p>" + aName.getValue().toString() + ":</p>");
					}
				}
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return in;
	}

	private void printAttributes(PrintWriter writer, StartElement startElement) {
		writer.print("<ul>");
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute a = attributes.next();
			writer.print("<li>" + a.getName().toString() + "=" + a.getValue() + "</li>");						
		}						
		writer.print("</ul>");
	}

}
