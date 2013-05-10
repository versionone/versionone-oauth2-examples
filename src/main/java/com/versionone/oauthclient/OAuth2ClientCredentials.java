package com.versionone.oauthclient;

public class OAuth2ClientCredentials {
	  /** Value of the "API Key". */
	  public static final String CLIENT_ID =
	      "Enter API Key from http://www.dailymotion.com/profile/developer into CLIENT_ID in "
	      + OAuth2ClientCredentials.class;

	  /** Value of the "API Secret". */
	  public static final String CLIENT_SECRET =
	      "Enter API Secret from http://www.dailymotion.com/profile/developer into CLIENT_SECRET in "
	      + OAuth2ClientCredentials.class;

	  public static void errorIfNotSpecified() {
	    if (CLIENT_ID.startsWith("Enter ")) {
	      System.err.println(CLIENT_ID);
	      System.exit(1);
	    }
	    if (CLIENT_SECRET.startsWith("Enter ")) {
	      System.err.println(CLIENT_SECRET);
	      System.exit(1);
	    }
	  }
}
