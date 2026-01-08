package org.springframework.samples.homepix.sales;

import com.stripe.exception.StripeException;
import org.springframework.samples.homepix.User;

public interface PaymentService {
	public String createCheckoutSession(String username, String userEmail) throws StripeException;
}
