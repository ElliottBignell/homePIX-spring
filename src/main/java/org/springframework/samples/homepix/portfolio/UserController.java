package org.springframework.samples.homepix.portfolio;

import org.springframework.samples.homepix.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/change-password")
	public String changeUserPassword(
		//@RequestParam("username") String username,
		//@RequestParam("oldPassword") String oldPassword,
		//@RequestParam("newPassword") String newPassword
	) {

		// Call the method in UserService to change the password
		boolean isPasswordChanged = userService.changeUserPassword("", "");

		// Redirect to a confirmation page, or handle the response as needed
		return isPasswordChanged ? "password_change_success" : "password_change_failure";
	}


	@GetMapping("/change_password")
	public String changePassword(Album album, BindingResult result, Map<String, Object> model) {

		this.userService.changeUserPassword("", "");
		return "redirect:/welcome";
	}
}
