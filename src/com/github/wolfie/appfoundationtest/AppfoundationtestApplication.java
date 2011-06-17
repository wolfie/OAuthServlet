package com.github.wolfie.appfoundationtest;

import java.util.UUID;

import org.vaadin.appfoundation.authentication.oauth.OAuthLoginListener;
import org.vaadin.appfoundation.authentication.oauth.OAuthUtil;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AppfoundationtestApplication extends Application implements
    OAuthLoginListener {

  public class TwitterSSOListener implements ClickListener {
    public void buttonClick(final ClickEvent event) {

      // TODO: requesttoken might fix the UUID requirement.

      OAuthUtil.addListener(randomUUID, AppfoundationtestApplication.this);
      final String authorizationUrl = OAuthUtil.getTwitterLoginUrl(randomUUID);
      System.out.println(authorizationUrl);
      getMainWindow().open(new ExternalResource(authorizationUrl));
    }
  }

  private final UUID randomUUID = UUID.randomUUID();

  @Override
  public void init() {
    final Window mainWindow = new Window("Appfoundationtest Application");
    setMainWindow(mainWindow);

    final Button twitter = new Button("Twitter");
    twitter.addListener(new TwitterSSOListener());
    mainWindow.addComponent(twitter);
  }

  public void loginFailed() {
    getMainWindow().getContent().removeAllComponents();
    getMainWindow().getContent().addComponent(new Label("FAILED!"));
    OAuthUtil.destroy(randomUUID);
  }

  public void loginSucceeded() {
    getMainWindow().getContent().removeAllComponents();
    getMainWindow().getContent().addComponent(new Label("SUCCEEDED!"));
    OAuthUtil.destroy(randomUUID);
  }
}
