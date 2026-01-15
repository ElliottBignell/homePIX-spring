package org.springframework.samples.homepix.sales;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
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

	@Column(name = "user_id")
	@NotBlank(message = "User ID is mandatory")
	private User user;

	@Column(name = "stripe_session_id")
	@NotBlank(message = "Session ID is mandatory")
	@NotEmpty
	private String stripeSessionId;

	@Column(name = "status")
	@NotBlank(message = "Status ID is mandatory")
	@NotEmpty
	private OrderStatus status;

	@Column(name = "total_amount")
	@NotBlank(message = "Amount ID is mandatory")
	@NotEmpty
	private BigDecimal amount;

	@Column(name = "created_at")
	@NotBlank(message = "Createed-at date is mandatory")
	@NotEmpty
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
