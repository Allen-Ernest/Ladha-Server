package tz.co.ladha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    List<User> findByFirstNameAndLastNameIgnoreCase(String firstName, String lastName);
}