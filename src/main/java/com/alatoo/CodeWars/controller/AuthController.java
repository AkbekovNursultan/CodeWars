package com.alatoo.CodeWars.controller;
import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RecoveryRequest;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request,
                           @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return authService.register(request, locale);
    }
    @PostMapping("/confirm")
    public String confirmRegistration(@RequestParam String code,
                                      @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return authService.confirm(code, locale);
    }
    @GetMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request,
                               @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return authService.login(request, locale);
    }
    @PostMapping("/recovery")
    public String recovery(@RequestParam String email,
                           @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return authService.recovery(email, locale);
    }
    @PostMapping("/password_recovery")
    public String recoverPassword(@RequestParam String code,
                                  @RequestBody RecoveryRequest request,
                                  @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return authService.recoverPassword(code, request, locale);
    }
    //+
}
