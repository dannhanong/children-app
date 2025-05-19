package com.team.child_be.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class AgoraConfig {
    @Value("${agora.app.id}")
    private String appId;

    @Value("${agora.app.certificate}")
    private String appCertificate;
    
    @Value("${agora.token.expire.seconds:3600}")
    private int tokenExpireInSeconds;
}
