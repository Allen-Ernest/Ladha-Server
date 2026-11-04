package tz.co.ladha.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.dto.user_dto.AuthUserResponseDTO;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private AuthUserResponseDTO user;
}
