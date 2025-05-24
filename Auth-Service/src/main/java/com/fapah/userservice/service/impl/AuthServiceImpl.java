package com.fapah.userservice.service.impl;

import com.fapah.userservice.DTO.AuthResponse;
import com.fapah.userservice.entity.Role;
import com.fapah.userservice.entity.User;
import com.fapah.userservice.exception.AuthenticationException;
import com.fapah.userservice.exception.InvalidTokenException;
import com.fapah.userservice.exception.UserAlreadyExistsException;
import com.fapah.userservice.repository.UserRepository;
import com.fapah.userservice.service.RedisTokenService;
import com.fapah.userservice.util.JwtService;
import com.fapah.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Override
    public void register(String email, String password, Role role) {
        logger.info("Attempting to register new user with email: {}", email);
        
        if (userRepository.findByEmail(email).isPresent()) {
            logger.error("Registration failed: User with email {} already exists", email);
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role).build();

        userRepository.save(user);
        logger.info("Successfully registered new user with email: {}", email);
    }

    @Override
    public AuthResponse login(String email, String rawPassword) {
        logger.info("Attempting login for user: {}", email);

        if(redisTokenService.isValidRefreshToken(email)) {
            logger.info("Found existing refresh token for user: {}. Removing it.", email);
            redisTokenService.removeRefreshToken(email);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Login failed: User not found with email: {}", email);
                    return new AuthenticationException("User not found with email: " + email);
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.error("Login failed: Invalid password for user: {}", email);
            throw new AuthenticationException("Invalid password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        redisTokenService.saveRefreshToken(refreshToken, user.getEmail());
        logger.info("Successfully logged in user: {}", email);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        logger.info("Attempting to refresh token");

        if(!redisTokenService.isValidRefreshToken(refreshToken)) {
            logger.error("Token refresh failed: Invalid refresh token");
            throw new InvalidTokenException("Invalid refresh token");
        }

        String email = jwtService.extractClaims(refreshToken).getSubject();
        logger.info("Refreshing token for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Token refresh failed: User not found with email: {}", email);
                    return new AuthenticationException("User not found with email: " + email);
                });

        redisTokenService.removeRefreshToken(email);

        String newRefreshToken = jwtService.generateRefreshToken(user);
        String newAccessToken = jwtService.generateToken(user);

        redisTokenService.saveRefreshToken(newRefreshToken, email);
        logger.info("Successfully refreshed token for user: {}", email);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        logger.info("Attempting to logout user");

        String email = jwtService.extractClaims(refreshToken).getSubject();

        if(!redisTokenService.isValidRefreshToken(email)) {
            logger.error("Logout failed: Invalid refresh token for user: {}", email);
            throw new InvalidTokenException("Invalid refresh token");
        }

        redisTokenService.removeRefreshToken(email);
        logger.info("Successfully logged out user: {}", email);
    }
}
