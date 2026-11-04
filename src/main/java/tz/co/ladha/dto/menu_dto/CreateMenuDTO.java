package tz.co.ladha.dto.menu_dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateMenuDTO {
    @NotBlank(message = "Menu name is required")
    private String menuName;

    private String description;

    @NotBlank(message = "Branch is required")
    private String branchId;

    @NotBlank(message = "Subcategory is required")
    private String subCategoryId;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private Boolean available = true;

    /** Day names, e.g. Monday, Tuesday. */
    @NotEmpty(message = "Select at least one day")
    private List<String> daysAvailable = new ArrayList<>();

    @Valid
    @NotEmpty(message = "Add at least one size / portion")
    private List<MenuSizeDTO> sizes = new ArrayList<>();

    @Getter
    @Setter
    public static class MenuSizeDTO {
        /** COUNTABLE */
        private Integer pieceCount;

        /** CATEGORICAL: S, M, L, XL, XXL */
        private String sizeCode;

        /** CATEGORICAL: people served */
        private Integer servesPeople;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        private BigDecimal price;
    }
}
