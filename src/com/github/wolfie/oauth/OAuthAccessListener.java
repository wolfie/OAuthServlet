package com.github.wolfie.oauth;

public interface OAuthAccessListener {
  public interface AccessEvent {
    String getStorableString();
  }

  public void accessGranted(AccessEvent event);

  public void accessDenied();
}
