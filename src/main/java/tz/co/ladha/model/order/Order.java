package tz.co.ladha.model.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.user.client.Client;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customerId")
    private Client client;

    @Column(nullable = false)
    private double orderAmount;
}
