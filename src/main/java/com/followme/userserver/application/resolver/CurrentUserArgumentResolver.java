package com.followme.userserver.application.resolver;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followme.userserver.application.annotation.CurrentUser;
import com.followme.userserver.exception.UserNotFoundException;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    // Resolver가 언제 동작할지 결정
    // 파라미터에 @CurrentUser 가 붙어있고, 타입이 String(username)일 때 동작
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentUser.class);
        boolean isStringType = String.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && isStringType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나, 익명 사용자면 Exception 반환
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        // Keycloak Oauth2 리소스 서버를 쓰면 Principal이 Jwt 객체로 들어옵니다.
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            // Keycloak이 발급한 토큰의 "preferred_username"을 꺼냅니다.
            return jwt.getClaimAsString("preferred_username"); 
        }
        

        return authentication.getName();
    }
}