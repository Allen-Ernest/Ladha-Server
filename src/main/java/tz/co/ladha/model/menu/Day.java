package tz.co.ladha.model.menu;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID dayId;

    private String dayName;
}