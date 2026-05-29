package com.gdoc.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class SsoFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SsoFilter.class);

    @Value("${gdoc.sso.enabled:false}")
    private boolean enabled;

    @Value("${gdoc.sso.header:X-SSO-Token}")
    private String ssoHeader;

    @Value("${gdoc.sso.secret:}")
    private String ssoSecret;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (enabled) {
            if (!StringUtils.hasText(ssoSecret)) {
                log.error("SSO is enabled but gdoc.sso.secret is not configured");
                filterChain.doFilter(request, response);
                return;
            }

            String ssoToken = request.getHeader(ssoHeader);
            if (ssoToken == null) {
                ssoToken = request.getParameter("sso_token");
            }

            if (ssoToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    SSOUserInfo userInfo = verifyAndParseToken(ssoToken);
                    if (userInfo != null) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userInfo, null,
                                        List.of(new SimpleGrantedAuthority(userInfo.getRole()))
                                );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("SSO authenticated user: {}", userInfo.getUsername());
                    }
                } catch (Exception e) {
                    log.warn("SSO token verification failed: {}", e.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private SSOUserInfo verifyAndParseToken(String token) {
        String[] parts = token.split("\\.", 3);
        if (parts.length != 3) {
            return null;
        }

        String payloadBase64 = parts[1];
        String signatureProvided = parts[2];

        String dataToSign = parts[0] + "." + parts[1];
        String computedSignature = computeHmac(dataToSign);

        if (!computedSignature.equals(signatureProvided)) {
            log.warn("SSO token signature mismatch");
            return null;
        }

        String payload = new String(Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
        return parsePayload(payload);
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    ssoSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private SSOUserInfo parsePayload(String payload) {
        String[] fields = payload.split(":");
        if (fields.length < 3) {
            return null;
        }
        String username = fields[0];
        long userId = Long.parseLong(fields[1]);
        String role = fields.length > 2 ? fields[2] : "ROLE_SSO_USER";
        return new SSOUserInfo(username, userId, role);
    }

    public static class SSOUserInfo {
        private final String username;
        private final long userId;
        private final String role;

        public SSOUserInfo(String username, long userId, String role) {
            this.username = username;
            this.userId = userId;
            this.role = role;
        }

        public String getUsername() { return username; }
        public long getUserId() { return userId; }
        public String getRole() { return role; }
    }
}