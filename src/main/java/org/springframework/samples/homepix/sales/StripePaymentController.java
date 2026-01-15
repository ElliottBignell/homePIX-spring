package org.springframework.samples.homepix.sales;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.UserNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/payments/stripe")
@RequiredArgsConstructor
public class StripePaymentController {

	@Autowired
	StripePaymentService paymentService;

	@PostMapping("/session")
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
				.header(HttpHeaders.LOCATION, "/checkout")
				.build();
		}
	}
}

