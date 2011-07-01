package com.github.wolfie.oauth;

import java.util.Arrays;
import java.util.List;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.github.wolfie.oauth.exception.VerifierNotFoundFoundException;

class OAuthInfo {

  private final OAuthService service;

  private String authorizationUrl = null;
  private Token requestToken = null;
  private Verifier verifier = null;
  private Token accessToken = null;
  private boolean denied = false;
  private final Class<? extends Api> oauthApiClass;

  public OAuthInfo(final OAuthIdentifier id,
      final Class<? extends Api> oauthApiClass, final String apiKey,
      final String secretKey, final String callbackUri) {

    this.oauthApiClass = oauthApiClass;
    service = getTwitterService(id, apiKey, secretKey, callbackUri);
    requestToken = service.getRequestToken();
    authorizationUrl = service.getAuthorizationUrl(requestToken);
  }

  public OAuthInfo(final OAuthIdentifier id,
      final Class<? extends Api> oauthApiClass, final String apiKey,
      final String secretKey, final Token accessToken) {
    this.oauthApiClass = oauthApiClass;
    service = getTwitterService(id, apiKey, secretKey);
    this.accessToken = accessToken;
  }

  public String getLoginUrl() {
    return authorizationUrl;
  }

  private static OAuthService getTwitterService(final OAuthIdentifier id,
      final String apiKey, final String secretKey) {
    return new ServiceBuilder().provider(TwitterApi.class).apiKey(apiKey)
        .apiSecret(secretKey).build();
  }

  private static OAuthService getTwitterService(final OAuthIdentifier id,
      final String twitterApiKey, final String twitterSecretKey,
      final String callbackUri) {

    return new ServiceBuilder().provider(TwitterApi.class)
        .apiKey(twitterApiKey).callback(generateCallbackUri(callbackUri, id))
        .apiSecret(twitterSecretKey).build();
  }

  private static String generateCallbackUri(final String callbackUri,
      final OAuthIdentifier id) {
    final char combinationChar;
    if (callbackUri.contains("?")) {
      combinationChar = '&';
    } else {
      combinationChar = '?';
    }
    return callbackUri + combinationChar + OAuthUtil.PARAM_ID + "=" + id;
  }

  public OAuthService getService() {
    return service;
  }

  public Token getRequestToken() {
    return requestToken;
  }

  public Token getAccessToken() throws VerifierNotFoundFoundException {
    if (accessToken == null) {
      if (verifier == null) {
        throw new VerifierNotFoundFoundException();
      }
      accessToken = service.getAccessToken(requestToken, verifier);
    }

    return accessToken;
  }

  public void setVerifier(final String verifier) {
    this.verifier = new Verifier(verifier);
  }

  public void setDenied() {
    denied = true;
  }

  public boolean isDenied() {
    return denied;
  }

  public boolean isTwitter() {
    return !isDenied() && oauthApiClass.equals(TwitterApi.class);
  }

  public String serialize() {
    try {
      final String apiName = oauthApiClass.getSimpleName();
      final String token = accessToken.getToken();
      final String secret = accessToken.getSecret();
      return apiName + "," + token + "," + secret;
    } catch (final NullPointerException e) {
      return null;
    }
  }

  public static OAuthInfo deserialize(final OAuthIdentifier id,
      final String string, final String apiKey, final String apiSecret) {
    final String[] split = string.split(",");
    if (split.length != 3) {
      return null;
    }

    Class<? extends Api> api = null;

    @SuppressWarnings("unchecked")
    final List<Class<? extends Api>> apiList = Arrays
        .<Class<? extends Api>> asList(TwitterApi.class);
    for (final Class<? extends Api> apiClass : apiList) {
      if (split[0].equals(apiClass.getSimpleName())) {
        api = apiClass;
        break;
      }
    }

    if (api == null) {
      throw new RuntimeException("BUG! can't handle type " + split[0]);
    }

    return new OAuthInfo(id, api, apiKey, apiSecret, new Token(split[1],
        split[2]));
  }
}