package com.versionone.oauthclient.hardcodedsecrets;

import com.versionone.oauthclient.IClientSecretRepository;
import com.versionone.oauthclient.IClientSecrets;

public class HardCodedRepository implements IClientSecretRepository{
	public IClientSecrets loadClientSecrets() {
		return new HardCodedClientSecrets();
	}

}
