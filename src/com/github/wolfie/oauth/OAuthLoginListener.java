package com.github.wolfie.oauth;

public interface OAuthLoginListener {
  public void loginFailed();

  public void loginSucceeded();
}
