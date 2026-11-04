package tz.co.ladha.dto.client_dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponseDTO {
    private String clientId;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
}
