package com.projectmanager.dto.auth;

import lombok.Data;

@Data
public class TokenResponseDto {

    private String accessToken;
    private String refreshToken;

}
