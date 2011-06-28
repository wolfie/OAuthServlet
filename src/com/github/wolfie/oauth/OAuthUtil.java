package com.github.wolfie.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OAuthUtil {

  public static class VerifierNotFoundFoundException extends Exception {
    private static final long serialVersionUID = 7223411711991590487L;
  }

  public static class OAuthInfoNotFoundException extends Exception {
    private static final long serialVersionUID = -7385579731681133274L;
  }

  static class OAuthInfo {

    private final OAuthService service;
    private final Token requestToken;
    private final String authorizationUrl;

    private Verifier verifier = null;
    private Token accessToken = null;
    private boolean denied = false;
    private final Class<? extends Api> oauthApiClass;

    public OAuthInfo(final UUID uuid, final Class<? extends Api> oauthApiClass,
        final String apiKey, final String secretKey, final String callbackUri) {

      this.oauthApiClass = oauthApiClass;
      service = getTwitterService(uuid, apiKey, secretKey, callbackUri);
      requestToken = service.getRequestToken();
      authorizationUrl = service.getAuthorizationUrl(requestToken);
    }

    public String getLoginUrl() {
      return authorizationUrl;
    }

    private static OAuthService getTwitterService(final UUID uuid,
        final String twitterApiKey, final String twitterSecretKey,
        final String callbackUri) {

      return new ServiceBuilder().provider(TwitterApi.class)
          .apiKey(twitterApiKey)
          .callback(generateCallbackUri(callbackUri, uuid))
          .apiSecret(twitterSecretKey).build();
    }

    private static String generateCallbackUri(final String callbackUri,
        final UUID uuid) {
      final char combinationChar;
      if (callbackUri.contains("?")) {
        combinationChar = '&';
      } else {
        combinationChar = '?';
      }
      return callbackUri + combinationChar + UUID_PARAM_NAME + "=" + uuid;
    }

    public OAuthService getService() {
      return service;
    }

    public Token getRequestToken() {
      return requestToken;
    }

    public Token getAccessToken() throws VerifierNotFoundFoundException {
      if (verifier == null) {
        throw new VerifierNotFoundFoundException();
      }

      if (accessToken == null) {
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
      return oauthApiClass.equals(TwitterApi.class);
    }
  }

  static final String VERIFIER_PARAM_NAME = "oauth_verifier";
  static final String TOKEN_PARAM_NAME = "oauth_token";
  static final String DENIED_PARAM_NAME = "denied";
  static final String UUID_PARAM_NAME = "oauth_uuid";

  private OAuthUtil() {
    // not instantiable
  }

  private static Map<UUID, OAuthInfo> oauthMap = new HashMap<UUID, OAuthInfo>();
  private static Map<UUID, OAuthLoginListener> loginListeners = new HashMap<UUID, OAuthLoginListener>();

  public static interface OAuthLoginInfo {
    UUID getIdentifier();

    String getLoginUri();
  }

  public static OAuthLoginInfo createTwitterLogin(final String twitterApiKey,
      final String twitterSecretKey, final String callbackUri) {

    final UUID randomUUID = UUID.randomUUID();

    final OAuthInfo info = new OAuthInfo(randomUUID, TwitterApi.class,
        twitterApiKey, twitterSecretKey, callbackUri);
    oauthMap.put(randomUUID, info);

    final String loginUrl = info.getLoginUrl();

    return new OAuthLoginInfo() {
      public String getLoginUri() {
        return loginUrl;
      }

      public UUID getIdentifier() {
        return randomUUID;
      }
    };
  }

  static Token getAccessToken(final UUID uuid)
      throws OAuthInfoNotFoundException, VerifierNotFoundFoundException {
    return getOauthInfo(uuid).getAccessToken();
  }

  private static OAuthInfo getOauthInfo(final UUID uuid)
      throws OAuthInfoNotFoundException {
    final OAuthInfo oAuthInfo = oauthMap.get(uuid);
    if (oAuthInfo != null) {
      return oAuthInfo;
    } else {
      throw new OAuthInfoNotFoundException();
    }
  }

  public static void setVerifier(final UUID uuid, final String verifier)
      throws OAuthInfoNotFoundException {
    getOauthInfo(uuid).setVerifier(verifier);
  }

  public static void destroy(final UUID uuid) {
    oauthMap.remove(uuid);
    loginListeners.remove(uuid);
  }

  static void setDenied(final UUID uuid) throws OAuthInfoNotFoundException {
    getOauthInfo(uuid).setDenied();
  }

  public boolean isDenied(final UUID uuid) throws OAuthInfoNotFoundException {
    return getOauthInfo(uuid).isDenied();
  }

  public static void addListener(final UUID uuid,
      final OAuthLoginListener listener) {
    if (listener != null && uuid != null) {
      loginListeners.put(uuid, listener);
    } else {
      throw new IllegalArgumentException("nulls are not allowed");
    }
  }

  public static void loginUnsuccessful(final UUID uuid) {
    final OAuthLoginListener listener = loginListeners.get(uuid);
    if (listener != null) {
      listener.loginFailed();
    }
  }

  public static void loginSuccessful(final UUID uuid) {
    final OAuthLoginListener listener = loginListeners.get(uuid);
    if (listener != null) {
      listener.loginSucceeded();
    }
  }
}
