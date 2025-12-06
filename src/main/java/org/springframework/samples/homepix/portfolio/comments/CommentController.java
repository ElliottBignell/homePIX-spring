package org.springframework.samples.homepix.portfolio.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class CommentController {

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	PictureFileRepository pictureFileRepository;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/submit_comment")
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
		comment.setUser(user);

		commentRepository.save(comment);

		return "redirect:" + redirectTo;
	}
}
