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

        // cleanup
        if (session != null) {
            session.removeAttribute("currentUrl");
        }

        response.sendRedirect(redirectTo);
    }
}
