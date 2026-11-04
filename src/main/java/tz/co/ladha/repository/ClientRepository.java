package tz.co.ladha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.user.client.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByEmail(String email);
    Optional<Client> findByPhone(String phone);
    Optional<Client> findByEmailAndPhoneIgnoreCase(String email, String phone);
    List<Client> findByFirstNameAndLastNameIgnoreCase(String firstName, String lastName);
}
