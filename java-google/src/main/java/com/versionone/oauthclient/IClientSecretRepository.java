package com.versionone.oauthclient;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface IClientSecretRepository {
	public IClientSecrets loadClientSecrets();
}
