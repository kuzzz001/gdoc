package com.gdoc.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class WatermarkFilter implements Filter {

    private boolean enabled = true;
    private String watermarkText = "GDOC";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (enabled && response instanceof HttpServletResponse httpResp) {
            httpResp.setHeader("X-Watermark", watermarkText);
        }
        chain.doFilter(request, response);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWatermarkText(String text) {
        this.watermarkText = StringUtils.hasText(text) ? text : "GDOC";
    }
}