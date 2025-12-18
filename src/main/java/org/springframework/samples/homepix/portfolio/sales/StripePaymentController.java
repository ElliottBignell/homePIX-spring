package org.springframework.samples.homepix.portfolio.sales;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.sales.CartItem;
import org.springframework.samples.homepix.sales.CartItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Value("${homepix.url}")
	String baseUrl;

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public ResponseEntity<String> createCheckoutSession(Principal principal) throws StripeException, IOException {

		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<PictureFile> items = new ArrayList<>();

		if (user.isEmpty()) {
			// If no user, redirect to /cart
			return ResponseEntity
				.status(303)
				.header(HttpHeaders.LOCATION, "/cart")
				.build();
		}

		long id = user.get().getUserId();

		items = cartItemRepository.findAll().stream()
			.filter(item -> item.getUser().getUserId() == id)
			.map(CartItem::getPicture)
			.collect(Collectors.toList());

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
								.setUnitAmount(1000L) // CHF 10.00
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

