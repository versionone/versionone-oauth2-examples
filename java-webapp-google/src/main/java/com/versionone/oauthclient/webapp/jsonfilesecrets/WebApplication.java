package com.versionone.oauthclient.webapp.jsonfilesecrets;

import com.google.api.client.util.Key;

public class WebApplication {
	@Key("web")
	private JsonClientSecrets secret;

	public JsonClientSecrets getSecret() {
		return secret;
	}
}
