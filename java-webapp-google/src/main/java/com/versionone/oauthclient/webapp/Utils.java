package com.versionone.oauthclient.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.MemoryCredentialStore;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class Utils {
    /** User ID is a key for getting storing and retrieving credentials */
    public static final String USER_ID = "self";    
	
	/** The JSON Factory is an expensive resource so make it a global instance. */
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	/** The HTTP Transport is an expensive resource so make it a global instance. */
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	/** The same Credential Store should be used by all Servlets so make it a global instance. */
	// The Memory Credential Store will drop credentials whenever the application is reloaded 
	public static final CredentialStore CREDENTIAL_STORE = new MemoryCredentialStore();
	/*
	// A JDO Credential Store is appropriate for a durable web application
	private static final CredentialStore CREDENTIAL_STORE = new JdoCredentialStore(
			JDOHelper.getPersistenceManagerFactory("transactions-optional"));
	*/

	/** Currently VersionOne only has a single OAuth security scope. */
    private static final String SCOPE = "apiv1";
    
	public static AuthorizationCodeFlow newFlow(IClientSecrets secrets) throws IOException {
		return new AuthorizationCodeFlow.Builder(
				// VersionOne accepts OAuth tokens in the header.
				BearerToken.authorizationHeaderAccessMethod(),
				// Communication will be over HTTP.
				HTTP_TRANSPORT,
				// OAuth end-points require JSON parsing.
				JSON_FACTORY,
				// The token URI is found in the VersionOne client_secrets
				new GenericUrl(secrets.getTokenUri()),
				// The client authentication parameters are found in the VersionOne client_secrets
				new ClientParametersAuthentication(secrets.getClientId(), secrets.getClientSecret()),
				// The client id is found in the VersionOne client_secrets
				secrets.getClientId(),
				// The authorization URI is found in the VersionOne client_secrets
				secrets.getAuthUri())
				// Set up storage for credentials once they are granted.
				.setCredentialStore(CREDENTIAL_STORE)
				// There is currently only 1 valid scope for VersionOne.
				.setScopes(SCOPE).build();
	};
	
	public static String getRedirectUri(HttpServletRequest request) {
		GenericUrl url = new GenericUrl(request.getRequestURL().toString());
	    url.setRawPath("/oauth2callback");
	    return url.build();
    }

}
