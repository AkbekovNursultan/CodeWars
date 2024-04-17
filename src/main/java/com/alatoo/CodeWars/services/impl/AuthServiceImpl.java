package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.auth.LoginRequest;
import com.alatoo.CodeWars.dto.auth.LoginResponse;
import com.alatoo.CodeWars.dto.auth.RecoveryRequest;
import com.alatoo.CodeWars.dto.auth.RegisterRequest;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BadCredentialsException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.JwtService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mail;
    @Override
    public String register(RegisterRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent())
            throw new BadCredentialsException("User with this username already exists.");
        if(userRepository.findByEmail(request.getEmail()).isPresent())
            throw new BadCredentialsException("This email is already connected.");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        if(!containsRole(request.getRole()))
            throw new BadRequestException("Unknown role.");
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        if(Role.valueOf(request.getRole()).equals(Role.USER)){
            user.setPoints(0);
            user.setRank(0);
        }
        String code = createCode();
        user.setVerificationCode(code);
        sendVerificationCode(request.getEmail(), code);
        userRepository.saveAndFlush(user);
        return "Verification code was sent to your email.";
    }
    private void sendVerificationCode(String email, String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mail);
        message.setTo(email);
        message.setSubject("Confirm registration");
        message.setText("\n\n" + code + "\n\n" + "This is code for verifying your account.\n\nDon't share it!!!");
        mailSender.send(message);

    }
    private String createCode(){
        String code = "";
        Random random = new Random();
        for(int k = 0; k < 6; k++) {
            if (random.nextInt(2) == 0)
                code += (char) (random.nextInt(26) + 65);
            else
                code += (char) (random.nextInt(10) + 48);
        }
        return code;
    }
    @Override
    public String confirm(String code) {
        Optional<User> user = userRepository.findByVerificationCode(code);
        if(user.isEmpty() || !user.get().getVerificationCode().equals(code))
            throw new BadRequestException("Incorrect verification code.");
        user.get().setEmailVerified(true);
        user.get().setVerificationCode(null);
        user.get().setImage(null);
        user.get().setBanned(false);
        userRepository.save(user.get());
        return "Email successfully connected.";
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if(user.isEmpty() || !user.get().getEmailVerified())
            throw new BadRequestException("User not found.");
        checkAccess(user.get());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
        }catch (org.springframework.security.authentication.BadCredentialsException e){
            throw new BadRequestException("Invalid password.");
        }
        return convertToResponse(user);
    }

    @Override
    public String recovery(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        String code = createCode();
        if(user.isEmpty())
            throw new NotFoundException("Account with this email doesn't exist!");
        checkAccess(user.get());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nursultan20052003@gmail.com");
        message.setTo(email);
        message.setText("This is link for recovery your password: http://localhost:8080/auth/password_recovery?code=" + code + "\n\nDon't share it!!!");
        message.setSubject("Password recovery.");
        mailSender.send(message);
        user.get().setRecoveryCode(code);
        userRepository.save(user.get());
        return "Message was sent to your email.";
    }
    @Override
    public String recoverPassword(String code, RecoveryRequest request) {
        Optional<User> user = userRepository.findByRecoveryCode(code);
        if(user.isEmpty())
            throw new BadRequestException("Invalid code");
        checkAccess(user.get());
        if(passwordEncoder.matches(request.getNewPassword(), (user.get().getPassword())))
            throw new BadRequestException("This password is already used.");
        if(!request.getNewPassword().equals(request.getConfirmPassword()) && user.get().getRecoveryCode() != null)
            throw new BadRequestException("The passwords doesn't match.");
        user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.get().setRecoveryCode(null);
        userRepository.save(user.get());

        return "Password successfully changed";
    }

    @Override
    public User getUserFromToken(String token){
        String[] chunks = token.substring(7).split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try{
            jsonObject = (JSONObject) jsonParser.parse(decoder.decode(chunks[1]));
        }catch(ParseException e){
            throw new RuntimeException(e);
        }
        return userRepository.findByUsername(String.valueOf(jsonObject.get("sub"))).orElseThrow(() -> new RuntimeException("User can be null"));
    }

    private LoginResponse convertToResponse(Optional<User> user){
        LoginResponse response = new LoginResponse();
        Map<String, Object> extraClaims = new HashMap<>();

        String jwtToken = jwtService.generateToken(extraClaims, user.get());
        response.setToken(jwtToken);
        return response;
    }
    private Boolean containsRole(String possibleRole){
        for(Role role : Role.values()){
            if(role.name().equalsIgnoreCase(possibleRole))
                return true;
        }
        return false;
    }

    public void checkAccess(User user){
        if(user.getBanned())
            throw new BlockedException("You were banned.");
    }
}
// Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// String currentPrincipalName = authentication.getName();