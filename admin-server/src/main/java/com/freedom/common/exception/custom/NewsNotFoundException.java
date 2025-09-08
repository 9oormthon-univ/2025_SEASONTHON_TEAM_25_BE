package com.freedom.common.exception.custom;

public class NewsNotFoundException extends RuntimeException {
  public NewsNotFoundException(String message) {
    super(message);
  }
}
