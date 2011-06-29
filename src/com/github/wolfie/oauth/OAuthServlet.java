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
            .getParameter(OAuthUtil.PARAM_UUID));

        if (oauthIsDenied(request)) {
          OAuthUtil.setDenied(uuid);
          OAuthUtil.loginUnsuccessful(uuid);
        } else {

          final String verifier = request
              .getParameter(OAuthUtil.PARAM_VERIFIER);
          OAuthUtil.setVerifier(uuid, verifier);
          OAuthUtil.loginSuccessful(uuid);
        }
      } catch (final OAuthInfoNotFoundException e) {
        throw new ServletException(e);
      }
    }

    final String redirect;
    if (request.getParameter(OAuthUtil.PARAM_REDIRECT) != null) {
      redirect = request.getParameter(OAuthUtil.PARAM_REDIRECT);
    } else {
      redirect = request.getContextPath();
    }
    response.sendRedirect(redirect);
  }

  private static boolean oauthIsDenied(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.PARAM_UUID) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.PARAM_DENIED) != null);
    return hasUuid && isDenied;
  }

  private static boolean isOAuthResponse(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.PARAM_UUID) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.PARAM_DENIED) != null);
    final boolean hasToken = (request.getParameter(OAuthUtil.PARAM_TOKEN) != null);
    final boolean hasVerifier = (request
        .getParameter(OAuthUtil.PARAM_VERIFIER) != null);
    return hasUuid && (isDenied || (hasToken && hasVerifier));
  }
}
