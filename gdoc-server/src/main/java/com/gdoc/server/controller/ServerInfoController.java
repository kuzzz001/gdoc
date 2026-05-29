package com.gdoc.server.controller;

import com.gdoc.common.result.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServerInfoController {

    @Value("${gdoc.version:1.0.0}")
    private String version;

    @Value("${gdoc.build-time:unknown}")
    private String buildTime;

    @GetMapping("/version")
    public ApiResponse<Map<String, String>> version() {
        Map<String, String> info = Map.of(
                "version", version,
                "buildTime", buildTime,
                "serverTime", LocalDateTime.now().toString(),
                "java", System.getProperty("java.version")
        );
        return ApiResponse.success(info);
    }
}