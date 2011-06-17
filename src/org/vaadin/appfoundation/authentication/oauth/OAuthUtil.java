package org.vaadin.appfoundation.authentication.oauth;

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

  public static class OAuthInfo {

    private static final String TWITTER_API_KEY = "BGsv3VY1AlH12k0hVNd8Q";
    private static final String TWITTER_SECRET_KEY = "3LP8YO4xnZK535OQHPkAS9eQ0OOZPEuOWVHYQGZoUvw";

    private final OAuthService service;
    private final Token requestToken;
    private final String authorizationUrl;

    private Verifier verifier = null;
    private Token accessToken = null;
    private boolean denied = false;

    public OAuthInfo(final UUID uuid, final Class<? extends Api> oauthApiClass) {
      service = getTwitterService(uuid);
      requestToken = service.getRequestToken();
      authorizationUrl = service.getAuthorizationUrl(requestToken);
    }

    public String getLoginUrl() {
      return authorizationUrl;
    }

    private static OAuthService getTwitterService(final UUID uuid) {
      return new ServiceBuilder()
          .provider(TwitterApi.class)
          .apiKey(TWITTER_API_KEY)
          .callback(
              "http://127.0.0.1:8080/AppFoundationTest/oauth/?"
                  + UUID_PARAM_NAME + "=" + uuid).apiSecret(TWITTER_SECRET_KEY)
          .build();
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

  public static String getTwitterLoginUrl(final UUID randomUUID) {
    return getTwitterInfo(randomUUID).getLoginUrl();
  }

  private static OAuthInfo getTwitterInfo(final UUID uuid) {
    try {
      return getOauthInfo(uuid);

    } catch (final OAuthInfoNotFoundException e) {
      final OAuthInfo info = new OAuthInfo(uuid, TwitterApi.class);
      oauthMap.put(uuid, info);
      return info;
    }
  }

  public static Token getAccessToken(final UUID uuid)
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

  public static void setDenied(final UUID uuid)
      throws OAuthInfoNotFoundException {
    getOauthInfo(uuid).setDenied();
  }

  public static void addListener(final UUID uuid,
      final OAuthLoginListener listener) {
    if (listener != null && uuid != null) {
      loginListeners.put(uuid, listener);
    } else {
      throw new IllegalArgumentException("nulls are not allowed");
    }
  }
}
