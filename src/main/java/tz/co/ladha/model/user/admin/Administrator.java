package tz.co.ladha.model.user.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.user.User;

@Entity
@Getter
@Setter
public class Administrator extends User {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminType adminType;
}