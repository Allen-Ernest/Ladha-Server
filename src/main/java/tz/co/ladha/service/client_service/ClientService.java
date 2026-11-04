package tz.co.ladha.service.client_service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tz.co.ladha.dto.AuthResponse;
import tz.co.ladha.dto.client_dto.ClientResponse;
import tz.co.ladha.dto.client_dto.ClientResponseDTO;
import tz.co.ladha.model.user.Gender;
import tz.co.ladha.model.user.client.Client;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.repository.ClientRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    public Optional<ClientResponse> registerClient(String email, String firstName, String lastName, String phone, String gender) {
        if (clientRepository.findByEmail(email.trim()).isPresent()) {
            throw new Exception("Email is already in use.");
        }

        if (clientRepository.findByPhone(phone.trim()).isPresent()) {
            throw new Exception("Phone is already in use.");
        }

        Client client = new Client();
        client.setEmail(email.trim());
        client.setFirstName(firstName.trim());
        client.setLastName(lastName.trim());
        client.setPhone(phone);

        if (gender.trim().equalsIgnoreCase("male")) {
            client.setGender(Gender.MALE);
        } else {
            client.setGender(Gender.FEMALE);
        }
        clientRepository.save(client);
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setEmail(email.trim());
        clientResponseDTO.setFirstName(firstName.trim());
        clientResponseDTO.setLastName(lastName.trim());
        clientResponseDTO.setPhone(phone.trim());
        clientResponseDTO.setGender(gender.trim());

        return Optional.of(new ClientResponse(email.trim(), clientResponseDTO));
    }
}
