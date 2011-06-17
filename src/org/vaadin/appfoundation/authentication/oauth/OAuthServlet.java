package org.vaadin.appfoundation.authentication.oauth;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.model.Token;
import org.vaadin.appfoundation.authentication.oauth.OAuthUtil.OAuthInfoNotFoundException;
import org.vaadin.appfoundation.authentication.oauth.OAuthUtil.VerifierNotFoundFoundException;

public class OAuthServlet extends HttpServlet {
  private static final long serialVersionUID = 3695701637457598082L;

  @Override
  protected void doGet(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {

    if (isOAuthResponse(request)) {
      try {

        final UUID uuid = UUID.fromString(request
            .getParameter(OAuthUtil.UUID_PARAM_NAME));

        if (oauthIsDenied(request)) {
          OAuthUtil.setDenied(uuid);
        } else {

          final String verifier = request
              .getParameter(OAuthUtil.VERIFIER_PARAM_NAME);
          OAuthUtil.setVerifier(uuid, verifier);
          final Token accessToken = OAuthUtil.getAccessToken(uuid);
          System.out.println(accessToken);
          OAuthUtil.destroy(uuid);
        }

      } catch (final OAuthInfoNotFoundException e) {
        e.printStackTrace();
      } catch (final VerifierNotFoundFoundException e) {
        e.printStackTrace();
      }
    }

    response.setStatus(HttpServletResponse.SC_FOUND);
    response.setHeader("Location", request.getContextPath());
  }

  private static boolean oauthIsDenied(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.UUID_PARAM_NAME) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.DENIED_PARAM_NAME) != null);
    return hasUuid && isDenied;
  }

  private static boolean isOAuthResponse(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.UUID_PARAM_NAME) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.DENIED_PARAM_NAME) != null);
    final boolean hasToken = (request.getParameter(OAuthUtil.TOKEN_PARAM_NAME) != null);
    final boolean hasVerifier = (request
        .getParameter(OAuthUtil.VERIFIER_PARAM_NAME) != null);
    return hasUuid && (isDenied || (hasToken && hasVerifier));
  }
}
