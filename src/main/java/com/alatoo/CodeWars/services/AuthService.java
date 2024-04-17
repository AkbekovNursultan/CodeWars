package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RecoveryRequest;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.entities.User;

public interface AuthService {
    String register(RegisterRequest request);

    String confirm(String code);

    LoginResponse login(LoginRequest request);

    User getUserFromToken(String token);

    String recovery(String email);

    String recoverPassword(String code, RecoveryRequest request);

    void checkAccess(User user);
}
