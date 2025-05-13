package com.fapah.userservice.DTO;

import com.fapah.userservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
}
