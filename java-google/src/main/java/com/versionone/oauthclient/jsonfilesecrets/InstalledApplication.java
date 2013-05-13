package com.versionone.oauthclient.jsonfilesecrets;

import com.google.api.client.util.Key;

public class InstalledApplication {
	@Key("installed")
	private JsonClientSecrets secret;

	public JsonClientSecrets getSecret() {
		return secret;
	}
}
