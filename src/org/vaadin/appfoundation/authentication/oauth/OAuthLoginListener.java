package org.vaadin.appfoundation.authentication.oauth;

public interface OAuthLoginListener {
  public void loginFailed();

  public void loginSucceeded();
}
