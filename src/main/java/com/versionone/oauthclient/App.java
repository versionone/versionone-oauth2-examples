package com.versionone.oauthclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.MemoryCredentialStore;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class App {
	
	// Currently VersionOne only has a single OAuth security scope.
	private static final String SCOPE = "apiv1";
	//private static final String SCOPE = "test:grant_15s";

	// Global instance of the HTTP transport.
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	// Global instance of the JSON factory.
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	// Client secrets from the VersionOne application instance
	private static final IClientSecrets secrets = new HardCodedClientSecrets();

	// User ID is a key for getting storing and retrieving credentials
	private static final String USER_ID = "self";

	public static void main(String[] args) {
		
        System.out.println("\n[STEP] Initialize Authorization Flow");
		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
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
		.setCredentialStore(new MemoryCredentialStore())
		// There is currently only 1 valid scope for VersionOne.
		.setScopes(SCOPE)
		.build();

        System.out.println("\n[STEP] Check to see if credentials can be loaded.");
		Credential credential = null;
		try {
			credential = codeFlow.loadCredential(USER_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null!=credential) {
			System.out.println("Using stored credentials.");
	        System.out.printf("Access Token: %s\n" + credential.getAccessToken());
	        System.out.printf("Expires In: %s\n" + credential.getExpiresInSeconds());
		} else {
			System.out.println("No stored credentials were found.");
		}

        System.out.println("\n[STEP] Request Authorization");

		
		/*
		// If credentials can be loaded, we're done.
		try {
			credential = codeFlow.loadCredential("self");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		AuthorizationCodeRequestUrl codeUrl = codeFlow.newAuthorizationUrl()
				.setRedirectUri(secrets.getRedirectUris().get(0))
				.setResponseTypes("code");
		String url = codeUrl.build();

		System.out.println("\nNavigate to:");
        System.out.println(url);
        try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

        System.out.println("\n[STEP] Get Authorization Code");
        System.out.println("Paste authorization code:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = null;
        try {
			code = br.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

        System.out.println("\n[STEP] Request Access Token");
        TokenRequest tokeRequest = null;
        TokenResponse tokenResponse = null;
		try {
			tokeRequest = codeFlow.newTokenRequest(code)
					.setRedirectUri(secrets.getRedirectUris().get(0))
					.setScopes(SCOPE);
			tokenResponse = tokeRequest.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			credential = codeFlow.createAndStoreCredential(tokenResponse, USER_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
        System.out.println("Access Token: " + tokenResponse.getAccessToken());
        System.out.println("Expires In: " + tokenResponse.getExpiresInSeconds());

        System.out.println("\n[STEP] Request Data");
        
		
		HttpRequestFactory requestFactory = null;
		
		try {
			final Credential v1credential = codeFlow.loadCredential(USER_ID);
	        requestFactory =
	                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
	                  public void initialize(HttpRequest request) throws IOException {
	                	  v1credential.initialize(request);
	                  }
	                });

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

        GenericUrl v1url = new GenericUrl("https://www14.v1host.com/v1sdktesting/rest-1.oauth.v1/data/Scope/0");

        HttpRequest v1request = null;
        try {
			v1request = requestFactory.buildGetRequest(v1url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
        HttpResponse v1response = null;
        try {
        	System.out.printf("Headers: %s\n", v1request.getHeaders().toString());
			v1response = v1request.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
        try {
        	BufferedReader in = new BufferedReader(new InputStreamReader(v1response.getContent()));
        	String inputLine;
			System.out.println("Response: ");
			while ((inputLine = in.readLine()) != null)
			    System.out.println(inputLine);
			in.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
	}

}
