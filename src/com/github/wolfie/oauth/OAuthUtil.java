package com.github.wolfie.oauth;

import java.util.HashMap;
import java.util.Map;

import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.github.wolfie.oauth.OAuthAccessListener.AccessEvent;
import com.github.wolfie.oauth.exception.OAuthInfoNotFoundException;
import com.github.wolfie.oauth.exception.VerifierNotFoundFoundException;

class OAuthUtil {

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

  private static OAuthAutoLoginHandler autologinHandler = null;

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
    try {
      final String serializedForm = getOauthInfo(id).serialize();

      final OAuthAccessListener listener = loginListeners.get(id);
      if (listener != null) {
        listener.accessGranted(new AccessEvent() {
          @Override
          public String getStorableString() {
            return serializedForm;
          }
        });
      }
    } catch (final OAuthInfoNotFoundException e) {
      // bug, shoudlnt' happen!
      e.printStackTrace();
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

  /**
   * Try to log in with a cookie value
   * 
   * @param cookieValue
   * @param apiKey
   * @param apiSecret
   * 
   * @return <code>true</code> iff login is successful.
   */
  public static boolean login(final String cookieValue, final String apiKey,
      final String apiSecret) {
    try {
      final OAuthIdentifier oAuthIdentifier = new OAuthIdentifier(cookieValue);
      if (autologinHandler == null) {
        throw new IllegalStateException(
            "autologin handler needs to be set before trying to autologin!");
      }

      final String rawOAuthInfo = autologinHandler
          .retrieveOAuthInfo(cookieValue);

      final OAuthInfo deserialize = OAuthInfo.deserialize(oAuthIdentifier,
          rawOAuthInfo, apiKey, apiSecret);

      oauthMap.put(oAuthIdentifier, deserialize);
      // TODO: make sure that the access is still valid
      return true;

    } catch (final IllegalArgumentException e) {
      System.err.println("This is a bug in this library. Oops!");
      e.printStackTrace();
      return false;
    }
  }

  public static void setAutolLoginHandler(final OAuthAutoLoginHandler handler) {
    autologinHandler = handler;
  }
}
