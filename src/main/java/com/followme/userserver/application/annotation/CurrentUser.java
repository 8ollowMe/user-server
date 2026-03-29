package com.followme.userserver.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 컨트롤러 파라미터에만 붙일 수 있도록 설정
@Retention(RetentionPolicy.RUNTIME) // 실행 중에도 유지되도록 설정
public @interface CurrentUser {
}