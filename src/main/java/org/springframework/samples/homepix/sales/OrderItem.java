package org.springframework.samples.homepix.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    private PictureFile pictureFile;

    @Enumerated(EnumType.STRING)
    private PricingTier tier;

    private BigDecimal price;
}
