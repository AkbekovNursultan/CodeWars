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
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.JwtService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    @Value("${spring.mail.username}")
    private String mail;
    @Override
    public String register(RegisterRequest request, Locale locale) {
        if(userRepository.findByUsername(request.getUsername()).isPresent())
            throw new BadCredentialsException(getMessage("register.error1"));
        if(userRepository.findByEmail(request.getEmail()).isPresent())
            throw new BadCredentialsException(getMessage("register.error2"));
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        if(!containsRole(request.getRole()))
            throw new BadRequestException(getMessage("register.error3"));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        if(Role.valueOf(request.getRole()).equals(Role.USER)){
            user.setPoints(0);
            user.setRank("Beginner");
        }
        String code = createCode();
        user.setVerificationCode(code);
        sendVerificationCode(request.getEmail(), code, locale);
        userRepository.saveAndFlush(user);
        return getMessage("register.success", new Object[]{request.getEmail()});
    }
    private void sendVerificationCode(String email, String code, Locale locale){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mail);
        message.setTo(email);
        message.setSubject(getMessage("verification.subject"));
        message.setText("\n\n" + code + "\n\n" + getMessage("verification.text"));
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
    public String confirm(String code, Locale locale) {
        Optional<User> user = userRepository.findByVerificationCode(code);
        if(user.isEmpty() || !user.get().getVerificationCode().equals(code))
            throw new BadRequestException(getMessage("confirm.error1"));
        user.get().setEmailVerified(true);
        user.get().setVerificationCode(null);
        user.get().setImage(null);
        user.get().setBanned(false);
        user.get().setFavorites(new ArrayList<>());
        userRepository.save(user.get());
        return getMessage("confirm.success");
    }

    @Override
    public LoginResponse login(LoginRequest request, Locale locale) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if(user.isEmpty() || !user.get().getEmailVerified())
            throw new BadRequestException(getMessage("login.error1"));
        checkAccess(user.get());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
        }catch (org.springframework.security.authentication.BadCredentialsException e){
            throw new BadRequestException(getMessage("login.error2"));
        }
        return convertToResponse(user);
    }

    @Override
    public String recovery(String email, Locale locale) {
        Optional<User> user = userRepository.findByEmail(email);
        String code = createCode();
        if(user.isEmpty())
            throw new NotFoundException(getMessage("recovery.error1"), HttpStatus.NOT_FOUND);
        checkAccess(user.get());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nursultan20052003@gmail.com");
        message.setTo(email);
        message.setText(getMessage("recovery.text", new Object[]{code}));
        message.setSubject("Password recovery.");
        mailSender.send(message);
        user.get().setRecoveryCode(code);
        userRepository.save(user.get());
        return getMessage("recovery.result");
    }
    @Override
    public String recoverPassword(String code, RecoveryRequest request, Locale locale) {
        Optional<User> user = userRepository.findByRecoveryCode(code);
        if(user.isEmpty())
            throw new BadRequestException(getMessage("recovery.error2"));
        checkAccess(user.get());
        if(passwordEncoder.matches(request.getNewPassword(), (user.get().getPassword())))
            throw new BadRequestException(getMessage("recovery.error3"));
        if(!request.getNewPassword().equals(request.getConfirmPassword()) && user.get().getRecoveryCode() != null)
            throw new BadRequestException(getMessage("recovery.error4"));
        user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.get().setRecoveryCode(null);
        userRepository.save(user.get());

        return getMessage("recovery.success");
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
            throw new BlockedException("BANNED");
    }
    private String getMessage(String code){
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    private String getMessage(String code, Object[] args){
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
// Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// String currentPrincipalName = authentication.getName();