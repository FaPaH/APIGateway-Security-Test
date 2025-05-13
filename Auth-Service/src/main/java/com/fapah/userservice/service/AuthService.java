package com.fapah.userservice.service;

import com.fapah.userservice.DTO.AuthResponse;
import com.fapah.userservice.entity.Role;

public interface AuthService {

    void register(String email, String password, Role role);

    AuthResponse login(String email, String password);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
