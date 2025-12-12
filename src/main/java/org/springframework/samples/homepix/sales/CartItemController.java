package org.springframework.samples.homepix.sales;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.SizeForSale;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

	protected CartItemController(AlbumRepository albums, KeywordRepository keyword, KeywordRelationshipsRepository keywordsRelationships, FolderService folderService) {
		super(albums, keyword, keywordsRelationships, folderService);
	}

	@PostMapping("/submit_purchase")
	@Secured("ROLE_ADMIN")
	public String submitComment(@RequestParam("pictureID") Integer pictureID,
								@RequestParam("redirectTo") String redirectTo,
								Principal principal,
								RedirectAttributes redirectAttributes,
								Model model
	)
	{
		if (!redirectTo.startsWith("/")) {
			redirectTo = "/"; // fallback to home
		}

		Optional<User> user = userRepository.findByUsername(principal.getName());
		Optional<PictureFile> pictureFile =  pictureFileRepository.findById(pictureID);

		if (user.isPresent() && pictureFile.isPresent()) {

			CartItem cartItem = new CartItem();

			cartItem.setSize(SizeForSale.MEDIUM);
			cartItem.setPicture(pictureFile.get());
			cartItem.setUser(user.get());

			cartItemRepository.save(cartItem);
		}

		return "redirect:" + redirectTo;
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
}
