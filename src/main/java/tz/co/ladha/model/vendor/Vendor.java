package tz.co.ladha.model.vendor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.district.Ward;
import tz.co.ladha.model.menu.MenuCategory;
import tz.co.ladha.model.user.admin.Administrator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID vendorId;

    @Column(nullable = false, unique = true)
    private int registrationNumber;

    @OneToOne
    @JoinColumn(name = "administratorId")
    private Administrator administrator;

    @Column(nullable = false)
    private String vendorName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "categoryId")
    private MenuCategory primaryCategory;

    private double rating;

    @Column(nullable = false)
    private String vendorPhone;

    @Column(nullable = false)
    private String vendorEmail;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String vendorAddress;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorType vendorType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hqID", nullable = false)
    private Ward HQ;
}
