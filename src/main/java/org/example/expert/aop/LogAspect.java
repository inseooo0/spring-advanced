package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "LogAspect")
public class LogAspect {

    @Before("@annotation(org.example.expert.annotation.AdminLogMethod)")
    public void log(JoinPoint joinPoint) {
        // id, 요청 시간, 요청 url logging
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        // id
        Long userId = (Long) request.getAttribute("userId");

        // 요청 시간
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = formatter.format(LocalDateTime.now());

        // 요청 url
        String url = request.getRequestURL().toString();
        String httpMethod = request.getMethod();

        // 로그
        log.info("[log] 요청 시간: " + currentTime + ", 요청 사용자 id: " + userId + ", 요청 url: [" + httpMethod + "] " + url);
    }
}
