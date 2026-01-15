package org.springframework.samples.homepix.sales;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.samples.homepix.sales.Order;

@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ArchiveService archiveService;

	@Autowired
	EmailService mailService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {

        Event event;

        try {
            event = Webhook.constructEvent(
                payload,
                sigHeader,
                webhookSecret
            );
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        switch (event.getType()) {

            case "checkout.session.completed" -> handleCheckoutCompleted(event);

            case "checkout.session.expired" -> handleCheckoutExpired(event);

            case "checkout.session.async_payment_failed" -> handlePaymentFailed(event);

            default -> {
                // ignore
            }
        }

        return ResponseEntity.ok().build();
    }

	private void handleCheckoutCompleted(Event event) throws IOException {

		Session session =
			(Session) event.getDataObjectDeserializer()
				.getObject()
				.orElseThrow();

		String sessionId = session.getId();

		Optional<Order> orders = orderRepository.findFirstByStripeSessionId(sessionId);

		if (orders.isEmpty()) {
			throw new RuntimeException("Order not found");
		}

		Order order = orders.get();

		if (order.getStatus() == OrderStatus.PAID) {
			return; // idempotency
		}

		order.setStatus(OrderStatus.PAID);
		order.setPaidAt(Instant.now());

		orderRepository.save(order);

		// 🔔 trigger fulfilment
		fulfilOrder(order);
	}

	private void fulfilOrder(Order order) throws IOException {

		String username = order.getUser().getUsername();

		List<PictureFile> pictures =
			order.getItems().stream()
				.map(OrderItem::getPictureFile)
				.toList();

		String s3Key = archiveService.createAndUploadArchive(
			username,
			pictures
		);

		order.setDownloadLink(s3Key);
		orderRepository.save(order);

		mailService.sendBuyerDownloadLink(
			order.getUser().getEmail(),
			"Your HomePIX photo package",
			s3Key
		);
	}

	private void handlePaymentFailed(Event event) {
	}

	private void handleCheckoutExpired(Event event) {

		Session session = (Session) event.getDataObjectDeserializer()
			.getObject()
			.orElseThrow(() -> new IllegalStateException("No session object"));

		orderRepository.findFirstByStripeSessionId(session.getId())
			.ifPresent(order -> {
				order.setStatus(OrderStatus.EXPIRED);
				orderRepository.save(order);
			});
	}
}
