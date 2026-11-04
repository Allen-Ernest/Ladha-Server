package tz.co.ladha.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "MenuSize")
public class MenuSize {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sizeId;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menuId", nullable = false)
    private Menu menu;

    /** Display label, e.g. "1 piece" or "S (1 person)". */
    @Column(nullable = false)
    private String label;

    /** COUNTABLE: number of pieces. */
    private Integer pieceCount;

    /** CATEGORICAL: S, M, L, XL, XXL. */
    private String sizeCode;

    /** CATEGORICAL: estimated number of people this size serves. */
    private Integer servesPeople;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
