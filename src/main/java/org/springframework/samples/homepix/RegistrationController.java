package org.springframework.samples.homepix;

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
	public String showRegistrationForm() {
		return "register";
	}

	@PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
			@RequestParam String email,
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

        return "redirect:/login?registered"; // Optional: show a "registration successful" message
	}
}
