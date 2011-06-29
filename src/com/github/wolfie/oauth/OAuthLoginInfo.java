package com.github.wolfie.oauth;


public interface OAuthLoginInfo {
  OAuthIdentifier getIdentifier();

  String getLoginUri();
}