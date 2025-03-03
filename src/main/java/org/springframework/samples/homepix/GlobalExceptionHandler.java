package org.springframework.samples.homepix;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFoundException(NoHandlerFoundException ex) {

		Map<String, String> response = new HashMap<>();
		response.put("error", "Not Found");
		response.put("message", "The requested resource was not found.");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
}
