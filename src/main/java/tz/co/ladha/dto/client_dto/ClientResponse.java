package tz.co.ladha.dto.client_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientResponse {
    private String email;
    private ClientResponseDTO clientResponseDTO;
}
