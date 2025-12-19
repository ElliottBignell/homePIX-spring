package org.springframework.samples.homepix.sales;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class StripePaymentController {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	ArchiveService archiveService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PricingService pricingService;

	@Value("${homepix.url}")
	String baseUrl;

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public ResponseEntity<String> createCheckoutSession(Principal principal) throws StripeException, IOException {

		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();
		BigDecimal price = BigDecimal.valueOf(10);

		if (user.isEmpty()) {
			// If no user, redirect to /cart
			return ResponseEntity
				.status(303)
				.header(HttpHeaders.LOCATION, "/cart")
				.build();
		}

		long id = user.get().getUserId();

		PricingTier tier = PricingTier.THUMBNAIL;

		List<CartItem> order = cartItemRepository.findByUserAndStatus(user.get(), CartStatus.IN_CART);

		items = order.stream()
			.filter(item -> item.getUser().getUserId() == id)
			.toList();

		items.forEach(item -> item.setPricingTier(tier));

		price = order.stream()
			.map(item -> pricingService.calculatePrice(
				item.getPricingTier(),
				item.getPicture().getWidth(),
				item.getPicture().getHeight()
			))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		long amountInCents = price
			.movePointRight(2)                // CHF → cents
			.setScale(0, RoundingMode.HALF_UP)
			.longValueExact();

		Stripe.apiKey = stripeSecretKey;

		SessionCreateParams params =
			SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(baseUrl + "payment/success?session_id={CHECKOUT_SESSION_ID}")
				.setCancelUrl(baseUrl + "cart")
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

		Session session = Session.create(params);

		return ResponseEntity
			.status(303)
			.header(HttpHeaders.LOCATION, session.getUrl())
			.build();
	}
}

