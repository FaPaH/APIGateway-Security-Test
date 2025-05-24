package com.fapah.apigateway.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;;

@Data
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {

    private String secret;
    private long expiration;
}
