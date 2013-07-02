package com.versionone.oauthclient.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.versionone.oauthclient.webapp.jsonfilesecrets.JsonFileRepository;

public class OAuth2Callback extends AbstractAuthorizationCodeCallbackServlet {
	/** Client secrets from the VersionOne application instance */
	private static IClientSecrets secrets;

	private static final long serialVersionUID = 1L;

	@Override
	protected void onSuccess(HttpServletRequest request, HttpServletResponse response, Credential credential) 
			throws ServletException, IOException {
		response.sendRedirect("/");
	}

	@Override
	protected void onError(HttpServletRequest request, HttpServletResponse response, AuthorizationCodeResponseUrl errorResponse)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
		PrintWriter writer = response.getWriter();
		writer.println("<!doctype html><html><head>");
		writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		writer.println("<title>" + "Authorization Cancelled" + "</title>");
		writer.println("</head><body>");
		writer.println("<h3>Authorization Cancelled</h3>");
		writer.println("<p>" + errorResponse.toString() + "</p>");
		writer.println("</body></html>");
	}

	@Override
	protected String getRedirectUri(HttpServletRequest request)	throws ServletException, IOException {
		return Utils.getRedirectUri(request);
	}

	@Override
	protected String getUserId(HttpServletRequest request) throws ServletException, IOException {
		return Utils.USER_ID;
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		secrets = new JsonFileRepository(Utils.JSON_FACTORY).loadClientSecrets(AuthorizedWebApp.class);
		return Utils.newFlow(secrets);
	}
}
