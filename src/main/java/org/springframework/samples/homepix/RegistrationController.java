package org.springframework.samples.homepix;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class RegistrationController {

	private RegistrationService registrationService;

	@Autowired
	public RegistrationController(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	@GetMapping("/register")
	public String showRegistrationForm(@RequestParam(required = false) String redirectTo,
									   HttpSession session,
									   Model model) {

		Object currentUrl = session.getAttribute("currentUrl");
		if (currentUrl != null) {
			model.addAttribute("currentUrl", currentUrl);
		} else if (redirectTo != null) {
			session.setAttribute("currentUrl", redirectTo);
		}

		return "register";
	}

	@PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
			@RequestParam String email,
			@RequestParam(required = false) String redirectTo,
            Model model
	) {

	   if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        if (registrationService.userExists(username)) {
            model.addAttribute("error", "Username already exists.");
            return "register";
        }

		registrationService.registerUser(username, email, password);

		if (redirectTo != null) {
			return "redirect:/prelogin?redirectTo=" + redirectTo + "&principal=" + username; // Optional: show a "registration successful" message
		}
		else {
			return "redirect:/login"; // Optional: show a "registration successful" message
		}
	}
}
