package org.springframework.samples.homepix.portfolio.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class CommentController {

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	PictureFileRepository pictureFileRepository;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/submit_comment")
	@Secured("ROLE_USER")
	public String submitComment(@RequestParam("send_comment") String text,
								@RequestParam("description_id") Integer pictureId,
								@RequestParam("redirectTo") String redirectTo,
								Principal principal,
								RedirectAttributes redirectAttributes)
	{
		if (!redirectTo.startsWith("/")) {
			redirectTo = "/"; // fallback to home
		}

		if (text == null || text.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Comment must not be empty.");
			return "redirect:" + redirectTo;
		}

		String sanitizedText = text; // sanitizer.sanitize(text); TODO

		Comment comment = new Comment();
		comment.setText(sanitizedText);
		comment.setDate(LocalDateTime.now());

		PictureFile picture = pictureFileRepository.findById(pictureId)
			.orElseThrow(() -> new RuntimeException("Picture not found"));
		comment.setPicture_id(picture);

		User user = userRepository.findByUsername(principal.getName())
			.orElseThrow(() -> new RuntimeException("User not found"));
		comment.setUser_id(user);

		commentRepository.save(comment);

		return "redirect:" + redirectTo;
	}

	@PostMapping("/delete_comment")
	@Secured("ROLE_USER")
	public String deletePicture(
		@RequestParam("redirectTo") String redirectTo,
		@RequestParam("userId") String userId,
		@RequestParam("commentId") String commentId,
		Principal principal,
		RedirectAttributes redirectAttributes
	)
	{
		if (!redirectTo.startsWith("/")) {
			redirectTo = "/"; // fallback to home
		}

		if (principal.getName().equals(userId)) {

			Optional<Comment> comment = commentRepository.findById(Long.valueOf(commentId));

			comment.ifPresent(value -> commentRepository.delete(value));
		}

		return "redirect:" + redirectTo;
	}
}
