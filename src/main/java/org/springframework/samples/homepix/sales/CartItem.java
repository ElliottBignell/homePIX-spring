package org.springframework.samples.homepix.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.SizeForSale;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picture_id", nullable = false)
    private PictureFile picture;

    @Column(name = "size", nullable = false)
    private SizeForSale size = SizeForSale.MEDIUM;

    @Column(name = "price_at_addition", precision = 10, scale = 2)
    private BigDecimal priceAtAddition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status = CartStatus.IN_CART;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
