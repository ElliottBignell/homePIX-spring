package org.springframework.samples.homepix.sales;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.samples.homepix.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@NotNull(message = "User is mandatory")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "stripe_session_id")
	@NotBlank(message = "Session ID is mandatory")
	@NotEmpty
	private String stripeSessionId;

	@NotNull
	@Enumerated(EnumType.STRING) // recommended, so DB stores "PENDING" not 0/1/2
	private OrderStatus status;

	@NotNull
	@Positive
	@Digits(integer = 10, fraction = 2) // optional but recommended
	@Column(name="total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "paid_at")
	private Instant paidAt;

	@Column(name = "download_link")
	@NotBlank(message = "Download-link is mandatory")
	@NotEmpty
	private String downloadLink;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();
}
