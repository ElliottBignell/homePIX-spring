package org.springframework.samples.homepix.sales;

import com.stripe.param.terminal.ReaderSetReaderDisplayParams;
import jakarta.servlet.http.HttpSession;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.SizeForSale;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.BucketController;
import org.springframework.samples.homepix.portfolio.folder.FolderController;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CartItemController extends PaginationController
{
	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PictureFileRepository pictureFileRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	BucketController bucketController;

	@Autowired
	FolderController folderController;

	@Autowired
	PricingService pricingService;

	protected CartItemController(AlbumRepository albums, KeywordRepository keyword, KeywordRelationshipsRepository keywordsRelationships, FolderService folderService) {
		super(albums, keyword, keywordsRelationships, folderService);
	}

	@PostMapping("/cart/choose/{pictureId}")
	@Secured("ROLE_ADMIN")
	public String submitComment(@PathVariable("pictureId") int pictureId,
								@RequestParam("redirectTo") String redirectTo,
								Map<String, Object> model,
								Principal principal
	)
	{
		Optional<User> user = userRepository.findByUsername(principal.getName());
		Optional<PictureFile> pictureFile =  pictureFileRepository.findById(pictureId);

		if (user.isPresent() && pictureFile.isPresent()) {
			model.put("picture", pictureFile.get());
			model.put("currentUrl", redirectTo);
		}

		return "/cart/addToCart.html";
	}

	@PostMapping("/cart/delete")
	@Secured("ROLE_ADMIN")
	public String showCart(@RequestParam(required = false) String redirectTo,
						   @RequestParam(required = true) Long orderNo,
						   Map<String, Object> model,
						   HttpSession session
	)
	{
		Optional<CartItem> cartItem = cartItemRepository.findById(orderNo);

		if (cartItem.isPresent()) {
			cartItemRepository.delete(cartItem.get());
		}

		return "redirect:/cart";
	}

	@PostMapping("/cart/addToCart/{pictureId}")
	@Secured("ROLE_ADMIN")
	public String showAddToCart(@PathVariable("pictureId") int pictureId,
								@RequestParam("redirectTo") String redirectTo,
								Map<String, Object> model,
								Principal principal)
	{
		Optional<User> user = userRepository.findByUsername(principal.getName());
		Optional<PictureFile> pictureFile =  pictureFileRepository.findById(pictureId);

		if (user.isPresent() && pictureFile.isPresent()) {

			CartItem cartItem = new CartItem();

			cartItem.setSize(SizeForSale.MEDIUM);
			cartItem.setPicture(pictureFile.get());
			cartItem.setUser(user.get());

			cartItemRepository.save(cartItem);
		}

		model.put("currentUrl", redirectTo);

		return "redirect:/cart";
	}

	@PostMapping("/cart/buy")
	@Secured("ROLE_ADMIN")
	public String buyFromCart( Map<String, Object> model,
							Principal principal)
		throws IOException
	{
		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();
		BigDecimal price = BigDecimal.valueOf(10);

		if (user.isPresent()) {

			PricingTier tier = PricingTier.THUMBNAIL;

			S3Client s3Client = folderController.getS3Clent();

			long id = user.get().getUserId();

			items = cartItemRepository.findAll().stream()
				.filter(item -> item.getUser().getUserId() == id)
				.collect(Collectors.toList());

			List<CartItem> order = cartItemRepository.findByUserAndStatus(user.get(), CartStatus.IN_CART);

			items.forEach(item -> item.setPricingTier(tier));

			price = order.stream()
				.map(item -> pricingService.calculatePrice(
					item.getPricingTier(),
					item.getPicture().getWidth(),
					item.getPicture().getHeight()
				))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

			model.put("files", items);
			model.put("price", price);

			return "cart/buy.html";
		}

		model.put("files", items);
		model.put("price", price);

		return "redirect:/cart";
	}

	@GetMapping("/cart")
	@Secured("ROLE_ADMIN")
	public String showCart(Map<String, Object> model,
						   @NonNull Principal principal)
	{
		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();
		List<String> files = new ArrayList<>();
		BigDecimal price = BigDecimal.valueOf(10);
		PricingTier tier = PricingTier.THUMBNAIL;

		if (user.isPresent()) {

			long id = user.get().getUserId();

			List<CartItem> order = cartItemRepository.findByUserAndStatus(user.get(), CartStatus.IN_CART);

			items = order.stream()
				.filter(item -> item.getUser().getUserId() == id)
				.collect(Collectors.toList());

			items.forEach(item -> item.setPricingTier(tier));

			price = order.stream()
				.map(item -> pricingService.calculatePrice(
					item.getPricingTier(),
					item.getPicture().getWidth(),
					item.getPicture().getHeight()
				))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

			folderController.initialiseS3Client();

			S3Client s3Client = folderController.getS3Clent();

			files = bucketController.listUserDownloads(user.get().getUsername());

			// Sort newest first (descending by filename)
			files.sort((a, b) -> {
				String fileA = a.substring(a.lastIndexOf('/') + 1);
				String fileB = b.substring(b.lastIndexOf('/') + 1);

				return fileB.compareTo(fileA);  // reverse order
			});

			// Convert into download URLs
			files = files.stream()
				.map(key -> "/downloads/" + user.get().getUsername() + "/" +
					key.substring(("downloads/" + user.get().getUsername() + "/").length()))
				.collect(Collectors.toList());
		}

		long amountInCents = price
			.multiply(BigDecimal.valueOf(100))
			.setScale(0, RoundingMode.HALF_UP)
			.longValueExact();

		// TODO Add dates created for use in a tool-tip
		model.put("downloads", files);
		model.put("items", items);
		model.put("price", price);

		return "cart/cart.html";
	}

    @GetMapping("/test-mail")
    public String testMail() {
        emailService.sendEmail("elliott.bignell@gmail.com",
                "Test from HomePIX",
                "If you see this, email sending works.");
		return "cart/cart.html";
    }

	@PostMapping("/webhooks/stripe")
	public ResponseEntity<String> stripeWebhook(@RequestBody String payload,
												@RequestHeader("Stripe-Signature") String sigHeader) {
		// verify signature
		// update order status
		return ResponseEntity.ok("success");
	}

	@PostMapping("/webhooks/paypal")
	public ResponseEntity<String> paypalWebhook(@RequestBody String payload) {
		// validate, update order
		return ResponseEntity.ok("OK");
	}
}
