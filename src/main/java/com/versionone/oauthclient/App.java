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
	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final String TOKEN_SERVER_URL = "https://www14.v1host.com/v1sdktesting/oauth.mvc/token";
	private static final String AUTHORIZATION_SERVER_URL = "https://www14.v1host.com/v1sdktesting/oauth.mvc/auth";
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	private static final String CLIENT_ID = "client_mzqhn239";
	private static final String CLIENT_SECRET = "dnkegtru5eahhfgvwzqq";

	public static void main(String[] args) {
		
		Credential credential = null;

        System.out.println("\n[STEP] Request Authorization");

		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
				BearerToken.authorizationHeaderAccessMethod(), 
				HTTP_TRANSPORT,
				JSON_FACTORY, 
				new GenericUrl(TOKEN_SERVER_URL), 
				new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
				CLIENT_ID, 
				AUTHORIZATION_SERVER_URL)
		.setCredentialStore(new MemoryCredentialStore())
		.setScopes(SCOPE)
		.build();
		
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
				.setRedirectUri(REDIRECT_URI)
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
					.setRedirectUri(REDIRECT_URI)
					.setScopes(SCOPE);
			tokenResponse = tokeRequest.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			credential = codeFlow.createAndStoreCredential(tokenResponse, "self");
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
			final Credential v1credential = codeFlow.loadCredential("self");
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
			System.out.printf("Response: \n%s", v1response.getContent().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
	}

}
