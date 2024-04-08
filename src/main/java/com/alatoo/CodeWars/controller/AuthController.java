package com.alatoo.CodeWars.controller;
import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;
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

}
