package com.gdoc.document.service;

import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.security.annotation.DocPermission;
import com.gdoc.security.annotation.RequirePermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {

    private final PermissionService permissionService;

    public PermissionAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(com.gdoc.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        DocPermission requiredPermission = annotation.value();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Long userId = (Long) authentication.getPrincipal();

        Object[] args = joinPoint.getArgs();
        Long docId = null;
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if ("id".equals(paramNames[i]) && args[i] instanceof Long) {
                docId = (Long) args[i];
                break;
            }
        }

        if (docId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无法确定文档ID");
        }

        DocPermission userPermission = permissionService.getPermission(docId, userId);
        if (userPermission == null) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }
        if (!userPermission.covers(requiredPermission)) {
            throw new BusinessException(ResultCode.DOC_PERMISSION_DENIED);
        }

        return joinPoint.proceed();
    }
}
