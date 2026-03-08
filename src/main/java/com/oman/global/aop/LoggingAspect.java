package com.oman.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.oman.domain..controller..*(..))")
    public void controllerPointcut() {}

    @Pointcut("execution(* com.oman.domain..service..*(..))")
    public void servicePointcut() {}

    @Around("controllerPointcut() || servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.debug(">>> [Entry] {}.{}() with args: {}", className, methodName, joinPoint.getArgs());
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            log.debug("<<< [Exit]  {}.{}() in {}ms with result: {}", className, methodName, elapsedTime, result);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("!!! [Error] Illegal argument in {}.{}()", className, methodName, e);
            throw e;
        }
    }
}