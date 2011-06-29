package com.github.wolfie.oauth;

public interface OAuthAccessListener {
  public void accessDenied();

  public void accessGranted();
}
