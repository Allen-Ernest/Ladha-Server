package tz.co.ladha.repository.menu_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.menu.Day;

import java.util.Optional;
import java.util.UUID;

public interface DayRepository extends JpaRepository<Day, UUID> {
    Optional<Day> findByDayNameIgnoreCase(String dayName);
}
