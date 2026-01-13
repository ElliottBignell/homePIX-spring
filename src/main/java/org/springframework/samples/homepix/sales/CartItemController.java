package org.springframework.samples.homepix.sales;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CartStatus;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CartItemController extends PaginationController
{
	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	CartItemDownloadRepository cartItemDownloadRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PictureFileRepository pictureFileRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	ArchiveService archiveService;

	protected CartItemController(AlbumRepository albums, KeywordRepository keyword, KeywordRelationshipsRepository keywordsRelationships, FolderService folderService) {
		super(albums, keyword, keywordsRelationships, folderService);
	}

	@GetMapping("/cart/choose/{pictureId}")
	@PermitAll
	public String choosePicture(@PathVariable("pictureId") int pictureId,
								Map<String, Object> model,
								HttpServletRequest request,
								Principal principal
	)
	{
		String userAgent = request.getHeader("User-Agent");

		logger.info("Request from User-Agent to choosePicture(/cart/choose/" + String.valueOf(pictureId) + "): " + userAgent);

		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/choose/" + pictureId;
		}

		Optional<PictureFile> file =  pictureFileRepository.findById(pictureId);

		if (file.isEmpty()) {
			return "redirect:/error-404";
		}

		String filename = "jpegs/" + file.get().getFolderName() + "/" + file.get().getFilename();
		boolean available = archiveService.s3ObjectExists(filename);

		if (!available) {
			return "redirect:/error-404";
		}

		model.put("picture", file.get());
		model.put("resolutions", PricingTier.values());

		CartItem cartItem = new CartItem();
		Optional<User> user = userRepository.findByUsername(principal.getName());

		user.ifPresent(cartItem::setUser);
		cartItem.setPricingTier(PricingTier.ORIGINAL); // TODO Get appropriate tier
		cartItem.setPicture(file.get());

		cartItemRepository.save(cartItem);

		model.put("item", cartItem);

		return "/cart/addToCart.html";
	}

	@PostMapping("/cart/choose/{pictureId}")
	@PermitAll
	public String choosePicture(@PathVariable("pictureId") int pictureId,
								@RequestParam("redirectTo") String redirectTo,
								Map<String, Object> model,
								Principal principal
	)
	{
		Optional<PictureFile> file =  pictureFileRepository.findById(pictureId);

		if (file.isEmpty()) {
			return "redirect:/error-404";
		}

		String filename = "jpegs/" + file.get().getFolderName() + "/" + file.get().getFilename();
		boolean available = archiveService.s3ObjectExists(filename);

		if (!available) {
			return "redirect:/error-404";
		}

		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/choose/" + pictureId;
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());

		if (user.isPresent()) {

			model.put("picture", file.get());
			model.put("currentUrl", redirectTo);
			model.put("resolutions", PricingTier.values());
		}
		else {
			return "redirect:/login";
		}

		return "/cart/addToCart.html";
	}

	@PostMapping("/cart/delete")
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
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

	@GetMapping("/cart/addToCart/{pictureId}")
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public String showAddToCart(
		@PathVariable int pictureId,
		@RequestParam(required = false) String redirectTo,
		Map<String, Object> model,
		Principal principal
	) {
		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/choose/" + pictureId;
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());
		Optional<PictureFile> pictureFile =  pictureFileRepository.findById(pictureId);

		if (user.isPresent() && pictureFile.isPresent()) {

			CartItem cartItem = new CartItem();

			cartItem.setPricingTier(PricingTier.ORIGINAL);
			cartItem.setPicture(pictureFile.get());
			cartItem.setUser(user.get());

			cartItemRepository.save(cartItem);

			model.put("item", cartItem);
			model.put("picture", pictureFile.get());
			model.put("resolutions", PricingTier.values());
		}

		model.put("currentUrl", redirectTo);
		return "cart/addToCart";
	}


	@PostMapping("/cart/addToCart/{pictureId}")
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public String showAddToCart(@PathVariable("pictureId") int pictureId,
								@RequestParam("redirectTo") String redirectTo,
								@RequestParam("tier") PricingTier tier,
								Map<String, Object> model,
								Principal principal)
	{
		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/addToCart/" + pictureId;
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());
		Optional<PictureFile> pictureFile =  pictureFileRepository.findById(pictureId);

		if (user.isPresent() && pictureFile.isPresent()) {

			CartItem cartItem = new CartItem();

			cartItem.setPricingTier(tier);
			cartItem.setPicture(pictureFile.get());
			cartItem.setUser(user.get());

			cartItemRepository.save(cartItem);

			model.put("item", cartItem);
		}

		model.put("currentUrl", redirectTo);

		return "redirect:/cart";
	}

	@PostMapping("/cart/buy")
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public String buyFromCart(ImageResolution resolution,
							  Map<String, Object> model,
							  Principal principal)
		throws IOException
	{
		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/choose/" + -1;
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();
		BigDecimal price = BigDecimal.valueOf(10);

		if (user.isPresent()) {

			long id = user.get().getUserId();

			items = cartItemRepository.findAll().stream()
				.filter(item -> item.getUser().getUserId() == id)
				.collect(Collectors.toList());

			List<CartItem> order = cartItemRepository.findByUserAndStatus(principal.getName(), CartStatus.IN_CART);

			price = order.stream()
				.map(CartItem::getTotalPrice)
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
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public String showCart(Map<String, Object> model,
						   @NonNull Principal principal)
	{
		if (principal == null) {
			return "redirect:/prelogin?redirectTo=/cart/choose/" + -1;
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();
		BigDecimal price = BigDecimal.valueOf(10);

		if (user.isPresent()) {

			long id = user.get().getUserId();

			List<CartItem> order = cartItemRepository.findByUserAndStatus(principal.getName(), CartStatus.IN_CART);

			items = order.stream()
				.filter(item -> item.getUser().getUserId() == id)
				.filter(item -> {
					return archiveService.s3ObjectExists(
						"jpegs/" +
							item.getPicture().getFolderName() +
							"/" +
							item.getPicture().getFilename()
					);
				})
				.collect(Collectors.toList());

			price = order.stream()
				.map(CartItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

			Map<Long, CartItem> itemsById = items.stream()
				.collect(Collectors.toMap(CartItem::getId, Function.identity()));

			Comparator<CartDownload> orderBy = (item1, item2 ) -> { return Math.toIntExact(item2.getOrderNo() - item1.getOrderNo()); };

			List<CartDownload> downloads = cartItemDownloadRepository.findByUsername(principal.getName()).stream()
				.map(url -> {
					return new CartDownload(url.getId(), url.getFilename(), url.getDownloadedAt());
				})
				.sorted(orderBy)
				.collect(Collectors.toList());

			model.put("downloads", downloads);
		}

		// TODO Add dates created for use in a tool-tip
		model.put("items", items);
		model.put("price", price);
		model.put("resolutions", ImageResolution.values());

		return "cart/cart.html";
	}

    @GetMapping("/test-mail")
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
    public String testMail() {
        emailService.sendEmail("elliott.bignell@gmail.com",
                "Test from HomePIX",
                "If you see this, email sending works.");
		return "cart/cart.html";
    }

	@PostMapping("/webhooks/stripe")
	@PermitAll
	public ResponseEntity<String> stripeWebhook(@RequestBody String payload,
												@RequestHeader("Stripe-Signature") String sigHeader) {
		// verify signature
		// update order status
		return ResponseEntity.ok("success");
	}

	@PostMapping("/webhooks/paypal")
	@PermitAll
	public ResponseEntity<String> paypalWebhook(@RequestBody String payload) {
		// validate, update order
		return ResponseEntity.ok("OK");
	}
}
