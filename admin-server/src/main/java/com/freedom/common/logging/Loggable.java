package com.freedom.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드에 로깅을 적용하기 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    
    /**
     * 커스텀 로그 메시지 (비어있으면 기본 메시지 사용)
     */
    String value() default "";
}
