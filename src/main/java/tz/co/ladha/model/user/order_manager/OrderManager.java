package tz.co.ladha.model.user.order_manager;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.user.User;
import tz.co.ladha.model.vendor.Branch;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrderManager extends User {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchId", nullable = false)
    private Branch branch;

    @Column(nullable = false)
    private LocalDateTime dateCreated;
}
