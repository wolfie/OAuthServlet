package com.github.wolfie.oauth;

import java.util.UUID;

public class OAuthIdentifier {
  private final UUID uuid;

  public OAuthIdentifier() {
    uuid = UUID.randomUUID();
  }

  public OAuthIdentifier(final String idString) {
    try {
      uuid = UUID.fromString(idString);
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(idString + " is not a valid "
          + getClass().getSimpleName() + " string");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OAuthIdentifier other = (OAuthIdentifier) obj;
    if (uuid == null) {
      if (other.uuid != null) {
        return false;
      }
    } else if (!uuid.equals(other.uuid)) {
      return false;
    }
    return true;
  }
}
