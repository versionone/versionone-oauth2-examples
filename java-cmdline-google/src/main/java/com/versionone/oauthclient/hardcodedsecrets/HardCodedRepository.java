package com.versionone.oauthclient.hardcodedsecrets;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.versionone.oauthclient.IClientSecretRepository;
import com.versionone.oauthclient.IClientSecrets;

public class HardCodedRepository implements IClientSecretRepository{
	public IClientSecrets loadClientSecrets() {
		return new HardCodedClientSecrets();
	}

}
