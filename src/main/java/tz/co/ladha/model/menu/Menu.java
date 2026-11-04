package tz.co.ladha.model.menu;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.vendor.Branch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID menuId;

    @Column(nullable = false)
    private String menuName;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branchId")
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subCategoryId")
    private MenuSubCategory subCategory;

    @ManyToMany
    @JoinTable(
            name = "MenuDay",
            joinColumns = @JoinColumn(name = "menuId"),
            inverseJoinColumns = @JoinColumn(name = "dayId")
    )
    private List<Day> daysAvailable = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MenuSize> sizes = new ArrayList<>();

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean available = true;
}
