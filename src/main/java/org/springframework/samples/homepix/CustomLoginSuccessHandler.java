package org.springframework.samples.homepix;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        HttpSession session = request.getSession(false);

        String redirectTo = (session != null)
                ? (String) session.getAttribute("currentUrl")
                : null;

        if (redirectTo == null || redirectTo.isBlank()) {
            redirectTo = "/";
        }

        // 🔒 CRITICAL: ensure redirect target is a GET page
        redirectTo = normalizeToGet(redirectTo);

        if (session != null) {
            session.removeAttribute("currentUrl");
        }

        response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303
        response.setHeader("Location", redirectTo);
    }

    private String normalizeToGet(String url) {
        // NEVER redirect to POST endpoints
        if (url.startsWith("/cart/addToCart/")) {
            return url; // this is the GET mapping
        }

        // add other mappings if needed
        return url;
    }
}
