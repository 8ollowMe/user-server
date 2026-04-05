package com.followme.userserver.application.resolver;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followme.userserver.application.annotation.CurrentUser;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    // Resolver가 언제 동작할지 결정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentUser.class);

        boolean isUuidType = UUID.class.isAssignableFrom(parameter.getParameterType());
        
        return hasAnnotation && isUuidType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}