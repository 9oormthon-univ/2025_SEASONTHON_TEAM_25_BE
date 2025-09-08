package com.freedom.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 간단한 로깅 AOP 처리
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {
    
    @Around("@annotation(com.freedom.common.logging.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        String message = loggable.value().isEmpty()
            ? String.format("[%s.%s]", className, methodName)
            : String.format("[%s] - [%s.%s]", loggable.value(), className, methodName);

        long startTime = System.currentTimeMillis();
        log.info("{} 시작", message);
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("{} 완료 | 실행시간: {}ms | 결과: {}", 
                    message, executionTime, formatResult(result));
            
            return result;
            
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.warn("{} 실패 | 실행시간: {}ms | 예외타입: {} | 메시지: {}", 
                     message, executionTime, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 결과값을 로그에 적합하게 포맷팅
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        
        String resultString = result.toString();
        
        if (resultString.toLowerCase().contains("token") ||
            resultString.toLowerCase().contains("password")) {
            return "[민감정보]";
        }
        
        if (resultString.length() > 100) {
            return resultString.substring(0, 97) + "...";
        }
        
        return resultString;
    }
}
