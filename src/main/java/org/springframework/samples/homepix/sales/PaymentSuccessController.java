package org.springframework.samples.homepix.sales;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.stripe.model.checkout.Session;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentSuccessController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

	@Autowired
	CartItemDownloadRepository cartItemDownloadRepository;

    private final CartItemRepository cartItemRepository;
    private final ArchiveService archiveService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public PaymentSuccessController(
            CartItemRepository cartItemRepository,
            ArchiveService archiveService,
            EmailService emailService,
            UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.archiveService = archiveService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

	@GetMapping("/success")
	@PermitAll
	public String paymentSuccess(
		@RequestParam("session_id") String sessionId,
		Principal principal,
		Model model
	) throws StripeException, IOException {

		// Ensure user is logged in
		if (principal == null) {
			return "redirect:/login";
		}

		Stripe.apiKey = stripeSecretKey;

		// ================================
		// 1) Retrieve the Checkout Session
		// ================================

		// Stripe Java v20.x requires the "null" argument:
		Session session = Session.retrieve(sessionId, null);

		// Debug check
		System.out.println("Stripe Session retrieved: " + session.getId());
		System.out.println("Payment status: " + session.getPaymentStatus());
		System.out.println("Amount total: " + session.getAmountTotal());

		// Ensure payment is actually successful
		if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
			model.addAttribute("error", "Payment not completed.");
			return "cart/cart";  // show cart with error message
		}

		// ====================================================
		// 2) Build and upload archive (AFTER payment success)
		// ====================================================
		Optional<User> userOpt = userRepository.findByUsername(principal.getName());

		if (userOpt.isEmpty()) {
			model.addAttribute("error", "User not found.");
			return "cart/cart";
		}

		User user = userOpt.get();

		// Get cart contents
		List<PictureFile> items =
			cartItemRepository.findAll().stream()
				.filter(item -> item.getUser().getUserId() == user.getUserId())
				.map(CartItem::getPicture)
				.toList();

		// Create + upload archive
		String archiveUrl = archiveService.createAndUploadArchive(user.getUsername(), items);

		// Clear cart (optional but recommended)
		cartItemRepository.deleteByUser_UserId(user.getUserId());

		String[] parts = archiveUrl.split("/");

		String username = user.getUsername();
		String filename = parts[2];

		CartItemDownload cartItemDownload = new CartItemDownload("downloads/" + username + "/" + filename, username);
		cartItemDownloadRepository.save(cartItemDownload);

		// ====================================
		// 3) Send emails (buyer + admin)
		// ====================================
		emailService.sendBuyerDownloadLink(
			user.getEmail(),
			"Your HomePIX photo package",
			archiveUrl
		);

		emailService.sendOwnerPurchaseNotification(
			user.getEmail(),
			"HomePIX photo package",
			archiveUrl
		);

		// ====================================
		// 4) Redirect the user back to /cart
		// ====================================
		return "redirect:/cart?success";
	}

	@GetMapping("/payment/failure")
	@PermitAll
	public String paymentFailure(Map<String, Object> model,
								 Principal principal
	) {
		return "/cart/paymentFailure.html";
	}
}
