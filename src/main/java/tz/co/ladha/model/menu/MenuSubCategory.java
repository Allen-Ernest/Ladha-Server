package tz.co.ladha.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Discrete front-of-house category under a broader platform {@link MenuCategory}
 * (e.g. parent "Bites, Juices & Coffee" → Bites / Juices / Coffee).
 */
@Entity
@Getter
@Setter
@Table(name = "MenuSubCategory")
public class MenuSubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subCategoryId;

    @Column(nullable = false)
    private String subCategoryName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SizingMode sizingMode = SizingMode.CATEGORICAL;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId", nullable = false)
    private MenuCategory parentCategory;

    @JsonIgnore
    @OneToMany(mappedBy = "subCategory")
    private List<Menu> menus = new ArrayList<>();
}
