package org.springframework.samples.homepix.sales;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.UserNotFoundException;
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
	StripePaymentService paymentService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PricingService pricingService;

	@Value("${homepix.url}")
	String baseUrl;

	@PostMapping("/create-checkout-session")
	@ResponseBody
	public ResponseEntity<Void> createCheckoutSession(Principal principal) {

		try {
			String redirectUrl = paymentService.createCheckoutSession(principal.getName(), "fred@fred.ch");

			return ResponseEntity
				.status(HttpStatus.SEE_OTHER)
				.header(HttpHeaders.LOCATION, redirectUrl)
				.build();

		} catch (UserNotFoundException ex) {

			return ResponseEntity
				.status(HttpStatus.SEE_OTHER)
				.header(HttpHeaders.LOCATION, "/cart")
				.build();

		} catch (StripeException ex) {

			return ResponseEntity
				.status(HttpStatus.SEE_OTHER)
				.header(HttpHeaders.LOCATION, "/cart")
				.build();
		}
	}
}

