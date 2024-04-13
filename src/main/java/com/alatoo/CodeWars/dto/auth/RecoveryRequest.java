package com.alatoo.CodeWars.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoveryRequest {
    private String newPassword;
    private String confirmPassword;
}
