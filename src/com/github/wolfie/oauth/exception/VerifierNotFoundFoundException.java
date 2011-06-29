package com.github.wolfie.oauth.exception;

/**
 * This exception is thrown when an access token is requested before a Verifier
 * has been created. If this is passed on to the client software, it's most
 * probably a bug in my OAuth project.
 * 
 * @author henrikpaul
 */
public class VerifierNotFoundFoundException extends RuntimeException {
  private static final long serialVersionUID = 7223411711991590487L;
}