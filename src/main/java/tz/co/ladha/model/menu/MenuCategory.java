package tz.co.ladha.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @Column(name = "category_description", nullable = false, unique = true)
    private String categoryDescription;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("subCategoryName ASC")
    private List<MenuSubCategory> subCategories = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "CategoryMenu",
            joinColumns = @JoinColumn(name = "MenuCategoryId"),
            inverseJoinColumns = @JoinColumn(name = "menuId")
    )
    private List<Menu> menus = new ArrayList<>();
}
