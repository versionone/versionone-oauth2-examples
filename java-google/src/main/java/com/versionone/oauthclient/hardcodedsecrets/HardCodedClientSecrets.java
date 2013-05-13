package com.versionone.oauthclient.hardcodedsecrets;

import java.util.Arrays;
import java.util.List;

import com.versionone.oauthclient.IClientSecrets;

public class HardCodedClientSecrets implements IClientSecrets {

	private static final String CLIENT_ID = "client_mzqhn239";
	private static final String CLIENT_NAME = "Java OAuth Example with Google lib";
	private static final String CLIENT_SECRET = "dnkegtru5eahhfgvwzqq";
	private static final List<String> REDIRECT_URIS = Arrays.asList("urn:ietf:wg:oauth:2.0:oob");
	private static final String AUTH_URI = "https://www14.v1host.com//v1sdktesting/oauth.mvc/auth";
	private static final String TOKEN_URI = "https://www14.v1host.com//v1sdktesting/oauth.mvc/token";
	private static final String SERVER_BASE_URI = "https://www14.v1host.com/v1sdktesting";
	private static final String EXPIRES_ON = "9999-12-31T23:59:59.9999999";
	
	public String getClientId() {
		return CLIENT_ID;
	}

	public String getClientName() {
		return CLIENT_NAME;
	}

	public String getClientSecret() {
		return CLIENT_SECRET;
	}

	public String getAuthUri() {
		return AUTH_URI;
	}

	public String getTokenUri() {
		return TOKEN_URI;
	}

	public String getServerBaseUri() {
		return SERVER_BASE_URI;
	}

	public String getExpiresOn() {
		return EXPIRES_ON;
	}

	public List<String> getRedirectUris() {
		return REDIRECT_URIS;
	}

}
