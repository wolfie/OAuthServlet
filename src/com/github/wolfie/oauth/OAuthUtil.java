package com.github.wolfie.oauth;

import java.util.HashMap;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.github.wolfie.oauth.exception.OAuthInfoNotFoundException;
import com.github.wolfie.oauth.exception.VerifierNotFoundFoundException;

class OAuthUtil {

  static class OAuthInfo {

    private final OAuthService service;
    private final Token requestToken;
    private final String authorizationUrl;

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

    public String getLoginUrl() {
      return authorizationUrl;
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
      return callbackUri + combinationChar + PARAM_ID + "=" + id;
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
      return !isDenied() && oauthApiClass.equals(TwitterApi.class);
    }
  }

  public static final String PARAM_VERIFIER = "oauth_verifier";
  public static final String PARAM_TOKEN = "oauth_token";
  public static final String PARAM_REDIRECT = "post_redirect";
  public static final String PARAM_DENIED = "denied";
  public static final String PARAM_ID = "oauth_id";
  public static final String COOKIE_NAME = "oauth_id";

  private static final String TWITTER_BASIC_INFO = "http://api.twitter.com/1/account/verify_credentials.xml";

  private OAuthUtil() {
    // not instantiable
  }

  // TODO: timeout handling
  private static Map<OAuthIdentifier, OAuthInfo> oauthMap = new HashMap<OAuthIdentifier, OAuthInfo>();
  private static Map<OAuthIdentifier, OAuthAccessListener> loginListeners = new HashMap<OAuthIdentifier, OAuthAccessListener>();

  public static OAuthLoginInfo createTwitterLogin(final String twitterApiKey,
      final String twitterSecretKey, final String callbackUri) {

    final OAuthIdentifier id = new OAuthIdentifier();

    final OAuthInfo info = new OAuthInfo(id, TwitterApi.class, twitterApiKey,
        twitterSecretKey, callbackUri);
    oauthMap.put(id, info);

    final String loginUrl = info.getLoginUrl();

    return new OAuthLoginInfo() {
      public String getLoginUri() {
        return loginUrl;
      }

      public OAuthIdentifier getIdentifier() {
        return id;
      }
    };
  }

  public static Token getAccessToken(final OAuthIdentifier OAuthIdentifier)
      throws OAuthInfoNotFoundException, VerifierNotFoundFoundException {
    return getOauthInfo(OAuthIdentifier).getAccessToken();
  }

  private static OAuthInfo getOauthInfo(final OAuthIdentifier OAuthIdentifier)
      throws OAuthInfoNotFoundException {
    final OAuthInfo oAuthInfo = oauthMap.get(OAuthIdentifier);
    if (oAuthInfo != null) {
      return oAuthInfo;
    } else {
      throw new OAuthInfoNotFoundException();
    }
  }

  public static void setVerifier(final OAuthIdentifier id, final String verifier)
      throws OAuthInfoNotFoundException {
    getOauthInfo(id).setVerifier(verifier);
  }

  public static void destroy(final OAuthIdentifier OAuthIdentifier) {
    oauthMap.remove(OAuthIdentifier);
    loginListeners.remove(OAuthIdentifier);
  }

  public static void setDenied(final OAuthIdentifier id)
      throws OAuthInfoNotFoundException {
    getOauthInfo(id).setDenied();
  }

  public boolean isDenied(final OAuthIdentifier id)
      throws OAuthInfoNotFoundException {
    return getOauthInfo(id).isDenied();
  }

  public static void addListener(final OAuthIdentifier id,
      final OAuthAccessListener listener) {
    if (listener != null && id != null) {
      loginListeners.put(id, listener);
    } else {
      throw new IllegalArgumentException("nulls are not allowed");
    }
  }

  public static void accessDenied(final OAuthIdentifier id) {
    final OAuthAccessListener listener = loginListeners.get(id);
    if (listener != null) {
      listener.accessDenied();
    }
  }

  public static void accessGranted(final OAuthIdentifier id) {
    final OAuthAccessListener listener = loginListeners.get(id);
    if (listener != null) {
      listener.accessGranted();
    }
  }

  public static String getBasicInfoRaw(final OAuthIdentifier id) {
    try {
      final OAuthInfo oauthInfo = getOauthInfo(id);

      if (oauthInfo.isTwitter()) {
        final OAuthService service = oauthInfo.getService();
        final Token accessToken = oauthInfo.getAccessToken();
        final OAuthRequest basicInfoRequest = new OAuthRequest(Verb.GET,
            TWITTER_BASIC_INFO);
        // basicInfoRequest.addBodyParameter("skip_status", "true");
        service.signRequest(accessToken, basicInfoRequest);
        final Response response = basicInfoRequest.send();
        return response.getBody();
      } else {
        throw new RuntimeException(
            "There's some unfinished business going on...");
      }
    } catch (final OAuthInfoNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
