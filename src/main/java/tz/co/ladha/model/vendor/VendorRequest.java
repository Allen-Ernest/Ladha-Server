package tz.co.ladha.model.vendor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class VendorRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID vendorRequestId;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false)
    private int registrationNumber;

    @Column(nullable = false)
    private String vendorEmail;

    @Column(nullable = false)
    private String vendorPhone;

    @Column(nullable = false)
    private String vendorAddress;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column(nullable = false)
    private LocalDateTime dateRequested;

    private LocalDateTime dateReviewed;
}
