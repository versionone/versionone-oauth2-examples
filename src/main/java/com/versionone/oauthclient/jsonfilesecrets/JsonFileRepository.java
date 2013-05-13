package com.versionone.oauthclient.jsonfilesecrets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.api.client.json.JsonFactory;
import com.versionone.oauthclient.IClientSecretRepository;
import com.versionone.oauthclient.IClientSecrets;

public class JsonFileRepository implements IClientSecretRepository{
	private JsonFactory jsonFactory;
	
	public JsonFileRepository(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}
	
	public IClientSecrets loadClientSecrets() {
		FileInputStream clientSecrets;
		try {
			clientSecrets = new FileInputStream("client_secrets.json");
		} catch (FileNotFoundException e) {
			System.out.printf("Please download the client_secrets.json file from the VersionOne permitted %napplications page and save it in the current directory.%n");
			return null;
		}
		InstalledApplication app = null;
		try {
			app = jsonFactory.createJsonParser(clientSecrets).parseAndClose(InstalledApplication.class, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        return app.getSecret();
	}

}
