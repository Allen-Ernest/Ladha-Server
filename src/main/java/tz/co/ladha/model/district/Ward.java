package tz.co.ladha.model.district;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.vendor.Vendor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_ward_name_per_district", columnNames = {"districtId", "ward_name"})
})
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID wardId;

    @Column(name = "ward_name", nullable = false)
    private String wardName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "districtId", nullable = false)
    private District district;

    @JsonIgnore
    @OneToMany(mappedBy = "HQ")
    private List<Vendor> vendors = new ArrayList<>();
}
