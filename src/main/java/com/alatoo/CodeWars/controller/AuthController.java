package com.alatoo.CodeWars.controller;
import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RecoveryRequest;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request){
        return authService.register(request);
    }
    @PostMapping("/confirm")
    public String confirmRegistration(@RequestParam String code){
        return authService.confirm(code);
    }
    @GetMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }
    @PostMapping("/recovery")
    public String recovery(@RequestParam String email){
        return authService.recovery(email);
    }
    @PostMapping("/password_recovery")
    public String recoverPassword(@RequestParam String code, @RequestBody RecoveryRequest request){
        return authService.recoverPassword(code, request);
    }
    //+
}
