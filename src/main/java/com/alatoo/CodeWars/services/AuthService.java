package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RecoveryRequest;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.entities.User;

import java.util.Locale;

public interface AuthService {
    String register(RegisterRequest request, Locale locale);

    String confirm(String code, Locale locale);

    LoginResponse login(LoginRequest request, Locale locale);

    User getUserFromToken(String token);

    String recovery(String email, Locale locale);

    String recoverPassword(String code, RecoveryRequest request, Locale locale);

    void checkAccess(User user);
}
