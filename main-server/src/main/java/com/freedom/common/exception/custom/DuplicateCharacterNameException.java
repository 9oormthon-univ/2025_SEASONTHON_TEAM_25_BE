package com.freedom.common.exception.custom;

public class DuplicateCharacterNameException extends RuntimeException {
    public DuplicateCharacterNameException(String message) {
        super(message);
    }
}
