package com.versionone.oauthclient.jsonfilesecrets;

import java.util.Arrays;
import java.util.List;

import com.google.api.client.util.Key;
import com.versionone.oauthclient.IClientSecrets;

public class JsonClientSecrets implements IClientSecrets {

	@Key("client_id")
	private String clientId;
	@Key("client_name")
	private String clientName;
	@Key("client_secret")
	private String clientSecret;
	@Key("redirect_uris")
	private List<String> redirectUris;
	@Key("auth_uri")
	private String authUri;
	@Key("token_uri")
	private String tokenUri;
	@Key("server_base_uri")
	private String serverBaseUri;
	@Key("expires_on")
	private String expiresOn;
	
	public String getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getAuthUri() {
		return authUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public String getServerBaseUri() {
		return serverBaseUri;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public List<String> getRedirectUris() {
		return redirectUris;
	}

}
