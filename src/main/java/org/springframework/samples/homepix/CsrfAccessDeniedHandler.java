package org.springframework.samples.homepix;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        boolean csrf =
            ex instanceof MissingCsrfTokenException ||
            ex instanceof InvalidCsrfTokenException;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean anonymous =
            auth == null ||
            auth instanceof AnonymousAuthenticationToken;

        if (csrf && anonymous) {
            // IMPORTANT: do NOT try to replay the POST after login.
            // Redirect back to the GET page that shows the form again.
            String safeReturn = request.getRequestURI();
            String encoded = UriUtils.encode(safeReturn, StandardCharsets.UTF_8);

            response.sendRedirect("/prelogin?redirectTo=" + encoded);
            return;
        }

		if (csrf) {

			String safeReturn = request.getRequestURI(); // e.g. /cart/addToCart/9705 (GET endpoint)

			if (safeReturn.startsWith("/cart")) {

				System.out.println("CSRF token expired for authenticated user, redirecting to cart");
				response.sendRedirect("/cart");
				return;
			}
		}

        // Otherwise, this is a real 403
        response.sendRedirect("/error-403");
    }
}
