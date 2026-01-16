package org.springframework.samples.homepix.sales;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StripePaymentService implements PaymentService {

	@Value("${stripe.secret-key}")
	private String stripeSecretKey;

	@Value("${homepix.url}")
	String baseUrl;

	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ArchiveService archiveService;

	@Override
    public String createCheckoutSession(String username, String userEmail) throws StripeException {

		BigDecimal price = BigDecimal.valueOf(10);
		List<CartItem> items = new ArrayList<>();

		Optional<User> user = userRepository.findByUsername(username);

		if (user.isEmpty()) {
			return "redirect:/error-403";
		}

		List<CartItem> cartItems = cartItemRepository.findByUserAndStatus(user.get(), CartStatus.IN_CART);

		price = cartItems.stream()
			.map(CartItem::getTotalPrice)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		long amountInCents = price
			.movePointRight(2)                // CHF → cents
			.setScale(0, RoundingMode.HALF_UP)
			.longValueExact();

		Stripe.apiKey = stripeSecretKey;

		SessionCreateParams params =
			SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(baseUrl + "cart")
				.setCancelUrl(baseUrl + "payment/failure")
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.addLineItem(
					SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPriceData(
							SessionCreateParams.LineItem.PriceData.builder()
								.setCurrency("chf")
								.setUnitAmount(amountInCents) // CHF 10.00
								.setProductData(
									SessionCreateParams.LineItem.PriceData.ProductData.builder()
										.setName("HomePIX Photo Package")
										.build()
								)
								.build()
						)
						.build()
				)
				.build();

		List<PictureFile> pictureFiles =
			items.stream()
				.filter(item -> item.getUser().getUserId() == user.get().getUserId())
				.map(CartItem::getPicture)
				.toList();

		String archiveUrl = archiveService.getArchiveName(username);

		// Clear cart (optional but recommended)
		cartItemRepository.deleteByUser_UserId(user.get().getUserId());

		String[] parts = archiveUrl.split("/");

		String filename = parts[2];

		Session session = Session.create(params);

		Order order = new Order();

		order.setUser(user.get());
		order.setStripeSessionId(session.getId());
		order.setStatus(OrderStatus.PENDING);
		order.setAmount(BigDecimal.valueOf(amountInCents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
		order.setCreatedAt(Instant.now());
		order.setDownloadLink(archiveUrl);

		cartItems.stream()
			.filter(item -> item.getUser().getUserId() == user.get().getUserId())
			.forEach(cartItem -> {
				if (cartItem.getPicture() == null) {
					throw new IllegalStateException(
						"CartItem " + cartItem.getId() + " has no picture"
					);
				}
				OrderItem orderItem = new OrderItem();
				orderItem.setOrder(order);                 // owning side
				orderItem.setPicture(cartItem.getPicture());
				orderItem.setPrice(cartItem.getTotalPrice());

				order.getItems().add(orderItem);           // 🔴 THIS WAS MISSING
			});
		orderRepository.save(order);

        return session.getUrl();
    }
}
