package com.fapah.userservice.service;

public interface RedisTokenService {

    void saveRefreshToken(String refreshToken, String email);

    String getRefreshTokenByEmail(String refreshToken);

    boolean isValidRefreshToken(String refreshToken);

    void removeRefreshToken(String refreshToken);
}
