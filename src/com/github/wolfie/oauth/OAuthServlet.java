package com.github.wolfie.oauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.wolfie.oauth.exception.OAuthInfoNotFoundException;

public class OAuthServlet extends HttpServlet {
  private static final long serialVersionUID = 3695701637457598082L;
  private static final int YEAR_IN_SECONDS = 31556926;

  @Override
  protected void doGet(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {

    if (isLogoutQuery(request)) {
      final Cookie oauthCookie = getOAuthCookie(request);
      oauthCookie.setMaxAge(0);
      // is this needed?
      response.addCookie(oauthCookie);

      OAuthUtil.destroy(new OAuthIdentifier(oauthCookie.getValue()));
    }

    else if (isOAuthResponse(request)) {
      try {

        final OAuthIdentifier id = new OAuthIdentifier(
            request.getParameter(OAuthUtil.PARAM_ID));

        if (oauthIsDenied(request)) {
          OAuthUtil.setDenied(id);
          response.addCookie(removeCookie(id));
          OAuthUtil.accessDenied(id);
        } else {

          final String verifier = request
              .getParameter(OAuthUtil.PARAM_VERIFIER);
          OAuthUtil.setVerifier(id, verifier);
          response.addCookie(createCookie(id));
          OAuthUtil.accessGranted(id);
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

  private static Cookie getOAuthCookie(final HttpServletRequest request) {
    for (final Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(OAuthUtil.COOKIE_NAME)) {
        return cookie;
      }
    }
    return null;
  }

  private static boolean isLogoutQuery(final HttpServletRequest request) {
    final int length = request.getContextPath().length();
    final String substring = request.getRequestURI().substring(length);
    return substring.contains("logout");
  }

  private static Cookie createCookie(final OAuthIdentifier id) {
    final Cookie cookie = _newCookie(id);
    cookie.setMaxAge(YEAR_IN_SECONDS);
    return cookie;
  }

  private static Cookie removeCookie(final OAuthIdentifier id) {
    final Cookie cookie = _newCookie(id);
    cookie.setMaxAge(0);
    return cookie;
  }

  private static Cookie _newCookie(final OAuthIdentifier id) {
    final Cookie cookie = new Cookie(OAuthUtil.COOKIE_NAME, id.toString());
    cookie.setDomain("/");
    return cookie;
  }

  private static boolean oauthIsDenied(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.PARAM_ID) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.PARAM_DENIED) != null);
    return hasUuid && isDenied;
  }

  private static boolean isOAuthResponse(final ServletRequest request) {
    final boolean hasUuid = (request.getParameter(OAuthUtil.PARAM_ID) != null);
    final boolean isDenied = (request.getParameter(OAuthUtil.PARAM_DENIED) != null);
    final boolean hasToken = (request.getParameter(OAuthUtil.PARAM_TOKEN) != null);
    final boolean hasVerifier = (request.getParameter(OAuthUtil.PARAM_VERIFIER) != null);
    return hasUuid && (isDenied || (hasToken && hasVerifier));
  }
}
