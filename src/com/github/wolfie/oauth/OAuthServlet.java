package com.github.wolfie.oauth;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.wolfie.oauth.OAuthUtil.OAuthInfoNotFoundException;

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
          OAuthUtil.loginUnsuccessful(uuid);
        } else {

          final String verifier = request
              .getParameter(OAuthUtil.VERIFIER_PARAM_NAME);
          OAuthUtil.setVerifier(uuid, verifier);
          OAuthUtil.loginSuccessful(uuid);
        }
      } catch (final OAuthInfoNotFoundException e) {
        throw new ServletException(e);
      }
    }

    response.sendRedirect(request.getContextPath());
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
