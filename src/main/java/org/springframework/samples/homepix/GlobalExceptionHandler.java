package org.springframework.samples.homepix;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BadCredentialsException.class)
	public String handleBadCredentialsException(BadCredentialsException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("error", "Bad credentials: " + ex.getMessage());
		return "redirect:/login";
	}

	// Add additional exception handlers if needed

}
