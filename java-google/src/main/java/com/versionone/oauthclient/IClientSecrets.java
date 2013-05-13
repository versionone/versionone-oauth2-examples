package com.versionone.oauthclient;

import java.util.List;

public interface IClientSecrets {
	public String getClientId();
	public String getClientName();
	public String getClientSecret();
	public String getAuthUri();
	public String getTokenUri();
	public String getServerBaseUri();
	public String getExpiresOn();
	public List<String> getRedirectUris();
}
