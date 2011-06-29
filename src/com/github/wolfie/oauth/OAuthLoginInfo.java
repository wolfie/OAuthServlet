package com.github.wolfie.oauth;

public interface OAuthLoginInfo {

  // TODO: instead of using OAuth thingy, access this directy?

  OAuthIdentifier getIdentifier();

  String getLoginUri();
}