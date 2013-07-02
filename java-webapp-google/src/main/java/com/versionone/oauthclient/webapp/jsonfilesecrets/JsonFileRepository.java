package com.versionone.oauthclient.webapp.jsonfilesecrets;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Preconditions;
import com.versionone.oauthclient.webapp.IClientSecretRepository;
import com.versionone.oauthclient.webapp.IClientSecrets;

public class JsonFileRepository implements IClientSecretRepository {
	private static final String CLIENT_SECRETS_FILENAME = "client_secrets.json";
	private JsonFactory jsonFactory;

	public JsonFileRepository(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	public IClientSecrets loadClientSecrets(Class context) {
		InputStreamReader clientSecrets = new InputStreamReader(context.getResourceAsStream("/" + CLIENT_SECRETS_FILENAME));
		WebApplication app = null;
		try {
			app = jsonFactory.createJsonParser(clientSecrets).parseAndClose(WebApplication.class, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Preconditions.checkArgument(!app.getSecret().getClientId().startsWith("Enter ")
		          && !app.getSecret().getClientSecret().startsWith("Enter "), 
		          "The sample code does not contain valid client id and secret values.\n"
		          + "Please add a Permitted App in your Member Profile, \n"
		          + "download the %s file from the VersionOne permitted applications page, \n"
		          + "and save it in the src/main/resources directory.\n", 
		          CLIENT_SECRETS_FILENAME);
		return app.getSecret();
	}

}