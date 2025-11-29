package org.springframework.samples.homepix;

import lombok.RequiredArgsConstructor;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

	private boolean isAdmin(Authentication auth) {
		return auth != null &&
			auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") );
	}

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/error-403";

        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam String username, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/error-404";

        userRepository.findByUsername(username).ifPresent(userRepository::delete);
        return "redirect:/admin/users";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/error-404";

        //userRepository.findByUsername(username).ifPresent(user -> {
            //String tempPassword = "Temp1234"; // Temporary password
            //user.setPassword(passwordEncoder.encode(tempPassword));
            //userRepository.save(user);
        //});

        return "redirect:/admin/users?resetSuccess";
    }
}
