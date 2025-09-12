package com.freedom.common.exception.custom;

public class CharacterAlreadyCreatedException extends RuntimeException {
    public CharacterAlreadyCreatedException(String message) {
        super(message);
    }
}
