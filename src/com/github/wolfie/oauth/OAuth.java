package com.github.wolfie.oauth;

public class OAuth {

  // FIXME: move the method bodies from OAuthUtil here

  public static void destroy(final OAuthIdentifier identifier) {
    OAuthUtil.destroy(identifier);
  }

  public static void addListener(final OAuthIdentifier id,
      final OAuthLoginListener listener) {
    OAuthUtil.addListener(id, listener);
  }

  public static OAuthLoginInfo createTwitterLogin(final String twitterApiKey,
      final String twitterSecretKey, final String callbackUri) {
    return OAuthUtil.createTwitterLogin(twitterApiKey, twitterSecretKey,
        callbackUri);
  }
}
