package org.example.expert.aop;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.aop.log.LoginLog;
import org.example.expert.aop.log.LoginLogRepository;
import org.example.expert.domain.auth.security.UserDetailsImpl;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginLoggingAspect {

	private final LoginLogRepository loginLogRepository;

	@Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
	public Object logLoginRequest(ProceedingJoinPoint joinPoint) throws Throwable {

		Object[] args = joinPoint.getArgs(); 	// 로그인 메서드의 매개변수
		Long requesterId = 0L, registeringId = 0L;
		for (Object arg : args) {
			if (arg instanceof UserDetailsImpl) {
				requesterId = ((UserDetailsImpl) arg).getUser().getId();
			}
			if (arg instanceof ManagerSaveRequest) {
				registeringId = ((ManagerSaveRequest) arg).getManagerUserId();
			}
		}

		LoginLog log = LoginLog.createLogOf(requesterId, registeringId, LocalDateTime.now());

		try {
			Object result = joinPoint.proceed();
			log.setStatus("SUCCESS");
			return result;
		} catch (Exception ex) {
			log.setStatus("FAILURE");
			throw ex;
		} finally {
			loginLogRepository.save(log);
		}
	}
}
