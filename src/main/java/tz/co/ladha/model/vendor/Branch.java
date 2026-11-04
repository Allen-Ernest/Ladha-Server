package tz.co.ladha.model.vendor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.user.admin.Administrator;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID branchId;

    @Column(nullable = false)
    private String branchName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "restaurantId")
    private Vendor vendor;

    @Column(nullable = false)
    private Boolean active;

    /** Street / area label shown in the UI (ward, landmark, etc.). */
    @Column(nullable = false)
    private String location;

    /** Physical address; defaults to the vendor's address for the main branch. */
    @Column(nullable = false)
    private String address;

    /**
     * Dedicated branch administrator. Null means the vendor admin operates this branch
     * (or the slot is vacant for appointment).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administratorId")
    private Administrator administrator;
}
