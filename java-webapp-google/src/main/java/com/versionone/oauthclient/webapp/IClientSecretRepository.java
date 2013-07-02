package com.versionone.oauthclient.webapp;

public interface IClientSecretRepository {
	public IClientSecrets loadClientSecrets(Class c);
}
