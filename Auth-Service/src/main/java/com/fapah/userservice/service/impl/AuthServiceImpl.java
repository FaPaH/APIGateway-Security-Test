package com.fapah.userservice.service.impl;

import com.fapah.userservice.DTO.AuthResponse;
import com.fapah.userservice.entity.Role;
import com.fapah.userservice.entity.User;
import com.fapah.userservice.repository.UserRepository;
import com.fapah.userservice.service.RedisTokenService;
import com.fapah.userservice.util.JwtService;
import com.fapah.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Override
    public void register(String email, String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role).build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(String email, String rawPassword) {
        if(redisTokenService.isValidRefreshToken(email)) {
            redisTokenService.removeRefreshToken(email);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        redisTokenService.saveRefreshToken(refreshToken, user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if(redisTokenService.isValidRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtService.extractClaims(refreshToken).getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        redisTokenService.removeRefreshToken(email);

        String newRefreshToken = jwtService.generateRefreshToken(user);
        String newAccessToken = jwtService.generateToken(user);

        redisTokenService.saveRefreshToken(newRefreshToken, email);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        String email = jwtService.extractClaims(refreshToken).getSubject();

        if(!redisTokenService.isValidRefreshToken(email)) {
            throw new RuntimeException("Invalid refresh token");
        }

        redisTokenService.removeRefreshToken(email);
    }
}
