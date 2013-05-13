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
import com.versionone.oauthclient.jsonfilesecrets.JsonFileRepository;

public class App {
	
	// Currently VersionOne only has a single OAuth security scope.
	//private static final String SCOPE = "apiv1";
	private static final String SCOPE = "test:grant_15s apiv1";

	// Global instance of the HTTP transport.
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	// Global instance of the JSON factory.
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	// Client secrets from the VersionOne application instance
	private static IClientSecrets secrets;

	// User ID is a key for getting storing and retrieving credentials
	private static final String USER_ID = "self";

	public static void main(String[] args) {
		
        System.out.println("\n[STEP] Load Client Secrets");
		secrets = new JsonFileRepository(JSON_FACTORY).loadClientSecrets();
		// Quit if the secrets could not be loaded.
		if (null == secrets) System.exit(1);
        System.out.printf("  Client ID: %s%n", secrets.getClientId());
        System.out.printf("  Client Name: %s%n", secrets.getClientName());
        System.out.printf("  Client Secret: %s%n", secrets.getClientSecret());
        System.out.printf("  Redirect URIs: %s%n", secrets.getRedirectUris().toString());
        System.out.printf("  Auth URI: %s%n", secrets.getAuthUri());
        System.out.printf("  Token URI: %s%n", secrets.getTokenUri());
        System.out.printf("  Server Base URI: %s%n", secrets.getServerBaseUri());
        System.out.printf("  Expires On: %s%n", secrets.getExpiresOn());
        
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

		try {
			final Credential v1credential = obtainCredentials(codeFlow);
			long expirationTime = v1credential.getExpiresInSeconds() * 1000;
	        HttpRequestFactory requestFactory =
	                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
	                  public void initialize(HttpRequest request) throws IOException {
	                	  v1credential.initialize(request);
	                  }
	                });
			requestResource(requestFactory);
			
			//waitForTokenExpiration(expirationTime);
			waitForTokenExpiration(v1credential.getExpiresInSeconds() * 1000);
			final Credential v1credential_2 = obtainCredentials(codeFlow);
	        HttpRequestFactory requestFactory_2 =
	                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
	                  public void initialize(HttpRequest request) throws IOException {
	                	  v1credential_2.initialize(request);
	                  }
	                });
			requestResource(requestFactory_2);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			// TODO Auto-generated catch block
			ie.printStackTrace();
		}
	}

	private static Credential obtainCredentials(AuthorizationCodeFlow codeFlow) throws IOException {
		System.out.println("\n[STEP] Obtain Credentials.");
		Credential credential = null;
		credential = codeFlow.loadCredential(USER_ID);
		if (null!=credential) {
			System.out.println("  Using stored credentials.");
	        System.out.printf("  Access Token: %s%n", credential.getAccessToken());
	        System.out.printf("  Expires In: %s s%n", credential.getExpiresInSeconds());
		} else {
			System.out.println("  No stored credentials were found.");
	        requestAuthorization(codeFlow);
	        String code = receiveAuthorizationCode();
			credential = requestAccessToken(codeFlow, code);
		}
		return credential;
	}

	private static void waitForTokenExpiration(long millis) throws InterruptedException {
		final long buffer = 5 * 1000; // 5 sec
		System.out.println("\n[STEP] Wait for Token Expiration");
		System.out.printf("  Sleeping for %d seconds until token expires.%n", ((millis + buffer) / 1000));
		Thread.sleep(millis + buffer);
	}

	private static void requestAuthorization(AuthorizationCodeFlow codeFlow) {
		System.out.println("\n[STEP] Request Authorization");
		AuthorizationCodeRequestUrl codeUrl = codeFlow.newAuthorizationUrl()
				.setRedirectUri(secrets.getRedirectUris().get(0))
				.setResponseTypes("code");
		String url = codeUrl.build();
		System.out.println("  Navigate to:");
        System.out.println(url);
        try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException ioe) {
			// If browser doesn't open, the instructions still prompt user to follow the link.
		}
        return;
	}

	private static String receiveAuthorizationCode() throws IOException {
		System.out.println("\n[STEP] Receive Authorization Code");
        System.out.println("  Paste authorization code:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = null;
		code = br.readLine();
		return code;
	}

	private static Credential requestAccessToken(AuthorizationCodeFlow codeFlow, String code) throws IOException {
		System.out.println("\n[STEP] Request Access Token");
        TokenRequest tokeRequest = codeFlow.newTokenRequest(code)
        		.setRedirectUri(secrets.getRedirectUris().get(0))
        		.setScopes(SCOPE);
		TokenResponse tokenResponse = tokeRequest.execute();
		System.out.println("  Received new access token.");
        System.out.printf("  Access Token: %s%n", tokenResponse.getAccessToken());
        System.out.printf("  Expires In: %d s%n", tokenResponse.getExpiresInSeconds());
		return codeFlow.createAndStoreCredential(tokenResponse, USER_ID);
	}

	private static void requestResource(HttpRequestFactory requestFactory) throws IOException {
        System.out.println("\n[STEP] Request Resource");

		System.out.println("  [Request]");
		// Get the VersionOne instance from client_secrets
		GenericUrl v1url = new GenericUrl(secrets.getServerBaseUri());
		// Add the OAuth API end-point
		v1url.getPathParts().add("rest-1.oauth.v1");
		// Add a simple data query that should work for most instances
		v1url.appendRawPath("/data/Scope/0");
        HttpRequest v1request = requestFactory.buildGetRequest(v1url);
        System.out.printf("    %s %s\n", v1request.getRequestMethod(), v1request.getUrl().toString());
        System.out.printf("    Headers: %s\n", v1request.getHeaders().toString());
        
		System.out.println("  [Response]");
        HttpResponse v1response = v1request.execute();
    	BufferedReader in = new BufferedReader(new InputStreamReader(v1response.getContent()));
    	String inputLine;
        System.out.printf("    Headers: %s\n", v1response.getHeaders().toString());
        System.out.println("    Body:");
		while ((inputLine = in.readLine()) != null)
		    System.out.println(inputLine);
		in.close();			
	}

}
