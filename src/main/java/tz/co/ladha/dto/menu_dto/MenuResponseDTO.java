package tz.co.ladha.dto.menu_dto;

import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.menu.SizingMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class MenuResponseDTO {
    private UUID menuId;
    private String menuName;
    private String description;
    private String imageUrl;
    private Boolean available;
    private UUID branchId;
    private String branchName;
    private UUID subCategoryId;
    private String subCategoryName;
    private SizingMode sizingMode;
    private String parentCategoryId;
    private String parentCategoryName;
    private List<String> daysAvailable = new ArrayList<>();
    private List<SizeResponse> sizes = new ArrayList<>();

    @Getter
    @Setter
    public static class SizeResponse {
        private UUID sizeId;
        private String label;
        private Integer pieceCount;
        private String sizeCode;
        private Integer servesPeople;
        private BigDecimal price;
        private Integer sortOrder;
    }
}
