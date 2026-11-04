package tz.co.ladha.controller.client_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.co.ladha.dto.client_dto.ClientResponse;
import tz.co.ladha.dto.client_dto.ClientDTO;
import tz.co.ladha.service.client_service.ClientService;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/client/auth")
public class ClientAuthController {
    private ClientService clientService;

    @PostMapping("/register-client")
    public ResponseEntity<?> registerClient(@Valid @RequestBody ClientDTO clientDTO) {
        Optional<ClientResponse> response = clientService.registerClient(clientDTO.getEmail(), clientDTO.getFirstName(), clientDTO.getLastName(), clientDTO.getPhone(), clientDTO.getGender());
        if (response.isPresent()) {
            return ResponseEntity.ok().body(response.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
