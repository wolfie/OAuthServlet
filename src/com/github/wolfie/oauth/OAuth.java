package com.github.wolfie.oauth;

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
}
