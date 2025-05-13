package com.fapah.userservice.service.impl;

import com.fapah.userservice.service.RedisTokenService;
import com.fapah.userservice.util.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    private final JwtProperties jwtProperties;

    @Override
    public void saveRefreshToken(String refreshToken, String email) {
        redisTemplate.opsForValue().set(jwtProperties.getPrefix() + email, refreshToken, Duration.ofSeconds(jwtProperties.getRefreshExpiration()));
    }

    @Override
    public String getRefreshTokenByEmail(String email) {
        return redisTemplate.opsForValue().get(jwtProperties.getPrefix() + email);
    }

    @Override
    public boolean isValidRefreshToken(String email) {
        return redisTemplate.hasKey(jwtProperties.getPrefix() + email);
    }

    @Override
    public void removeRefreshToken(String email) {
        redisTemplate.delete(jwtProperties.getPrefix() + email);
    }
}
