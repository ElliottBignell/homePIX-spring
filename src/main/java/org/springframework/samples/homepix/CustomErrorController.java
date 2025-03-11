package org.springframework.samples.homepix;

import jakarta.servlet.RequestDispatcher;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

	@GetMapping("/error")
	public String handleError(HttpServletRequest request) {

		String requestURI = request.getRequestURI();

		// Bypass custom error page for Actuator endpoints
		if (requestURI.startsWith("/actuator")) {
			return null;
		}

		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				return "error-404"; // Returns the custom 404 page
			}
		}

		return "error"; // Generic error page for other errors
	}

	@GetMapping("/error-404")
	public String handleError404(HttpServletRequest request) {
		return "error-404"; // Generic error page for other errors
	}

	@GetMapping("/login")
	public String handleLogin(HttpServletRequest request) {
		return "login";
	}
}
