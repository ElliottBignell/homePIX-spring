package org.springframework.samples.homepix.sales;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentPageController {

    @Value("${stripe.publishable-key}")
    private String stripePk;

    @Value("${paypal.client-id}")
    private String paypalClientId;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("stripePublishableKey", stripePk);
        model.addAttribute("paypalClientId", paypalClientId);
        return "payment";
    }
}
