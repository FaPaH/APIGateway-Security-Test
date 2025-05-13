package com.fapah.userservice.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {

    private String secret;
    private long expiration;
    private long refreshExpiration;
    private String prefix;
}
