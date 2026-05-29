package com.gdoc.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class WatermarkFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(WatermarkFilter.class);

    private boolean enabled = true;
    private String globalPrefix = "GDOC";

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGlobalPrefix(String prefix) {
        this.globalPrefix = (prefix != null && !prefix.isBlank()) ? prefix : "GDOC";
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (enabled && response instanceof HttpServletResponse httpResp) {
            String username = getCurrentUsername();
            String watermarkText = username != null
                    ? globalPrefix + " | " + username
                    : globalPrefix;

            String base64Watermark = Base64.getEncoder()
                    .encodeToString(watermarkText.getBytes(StandardCharsets.UTF_8));
            httpResp.setHeader("X-Watermark-Text", base64Watermark);
        }
        chain.doFilter(request, response);
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                if (auth.getPrincipal() instanceof Long) {
                    return auth.getName();
                }
            }
        } catch (Exception e) {
            log.debug("Cannot extract user for watermark", e);
        }
        return null;
    }
}