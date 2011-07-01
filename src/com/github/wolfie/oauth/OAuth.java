package com.github.wolfie.oauth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class OAuth {

  // FIXME: move the method bodies from OAuthUtil here

  public static void addListener(final OAuthIdentifier id,
      final OAuthAccessListener listener) {
    OAuthUtil.addListener(id, listener);
  }

  public static OAuthLoginInfo createTwitterLogin(final String twitterApiKey,
      final String twitterSecretKey, final String callbackUri) {
    return OAuthUtil.createTwitterLogin(twitterApiKey, twitterSecretKey,
        callbackUri);
  }

  public static String getBasicInfoRaw(final OAuthIdentifier id) {
    return OAuthUtil.getBasicInfoRaw(id);
  }

  public static boolean tryAutoLogin(
      final HttpServletRequest httpServletRequest, final String apiKey,
      final String apiSecret) {
    for (final Cookie cookie : httpServletRequest.getCookies()) {
      if (cookie.getName().equals(OAuthUtil.COOKIE_NAME)
          && !cookie.getValue().isEmpty()) {
        return OAuthUtil.login(cookie.getValue(), apiKey, apiSecret);
      }
    }
    return false;
  }

  public static void setAutoLoginHandler(final OAuthAutoLoginHandler handler) {
    OAuthUtil.setAutolLoginHandler(handler);
  }
}
