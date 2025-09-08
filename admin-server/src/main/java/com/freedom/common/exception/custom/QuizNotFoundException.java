package com.freedom.common.exception.custom;

public class QuizNotFoundException extends RuntimeException {
    
    public QuizNotFoundException(String message) {
        super(message);
    }
    
    public QuizNotFoundException(Long quizId) {
        super("퀴즈를 찾을 수 없습니다. id=" + quizId);
    }
}
