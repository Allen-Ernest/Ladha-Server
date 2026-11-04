package tz.co.ladha.dto.user_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotBlank(message = "First Name is required")
    String firstName;

    @NotBlank(message = "Last Name is required")
    String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    String adminType;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    String password;

    @NotBlank(message = "Password confirmation is required")
    String confirmPassword;

    @NotBlank(message = "Phone is required")
    String phone;

    @NotBlank(message = "Role is required")
    String role;

    @NotBlank(message = "Gender is required")
    String gender;
}