package com.ondoset.common;

import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodLogger {

	@Around("execution(* com.ondoset.service..*(..)) || execution(* com.ondoset.common.Ai.*(..)) || execution(* com.ondoset.common.Kma.*(..))")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

		ThreadContext.push(String.join(".", joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName()));
		System.out.println("joinPoint: " + joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName());

        Object result = joinPoint.proceed();

		ThreadContext.pop();
        return result;
	}
}
