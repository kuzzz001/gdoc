package com.gdoc.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int DEFAULT_MAX_PER_MINUTE = 120;
    private static final int LOGIN_MAX_PER_MINUTE = 5;
    private static final long WINDOW_MS = 60_000;

    private static final Map<String, Integer> PATH_LIMITS = Map.of(
            "/api/auth/login", LOGIN_MAX_PER_MINUTE,
            "/api/auth/register", 3
    );

    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int maxRequests = PATH_LIMITS.getOrDefault(path, DEFAULT_MAX_PER_MINUTE);

        String clientId = getClientId(request) + ":" + path;
        RateLimitEntry entry = rateLimitMap.computeIfAbsent(clientId, k -> new RateLimitEntry());

        long now = System.currentTimeMillis();
        synchronized (entry) {
            if (now - entry.windowStart > WINDOW_MS) {
                entry.count.set(0);
                entry.windowStart = now;
            }

            if (entry.count.incrementAndGet() > maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientId(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }
}