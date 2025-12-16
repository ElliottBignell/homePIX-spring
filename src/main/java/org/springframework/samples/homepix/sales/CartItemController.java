package org.springframework.samples.homepix.sales;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.SizeForSale;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.sales.EmailService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
	{
		return "redirect:/cart";
	}

	@GetMapping("/cart")
	@Secured("ROLE_ADMIN")
	public String showCart( Map<String, Object> model,
							Principal principal)
	{
		Optional<User> user = userRepository.findByUsername(principal.getName());
		List<CartItem> items = new ArrayList<>();

		if (user.isPresent()) {

			long id = user.get().getUser_id();

			items = cartItemRepository.findAll().stream()
				.filter(item -> item.getUser().getUser_id() == id)
				.collect(Collectors.toList());
		}

		model.put("items", items);
		return "cart/cart.html";
	}

    @GetMapping("/test-mail")
    public String testMail() {
        emailService.sendEmail("elliott.bignell@gmail.com",
                "Test from HomePIX",
                "If you see this, email sending works.");
		return "cart/cart.html";
    }
}
