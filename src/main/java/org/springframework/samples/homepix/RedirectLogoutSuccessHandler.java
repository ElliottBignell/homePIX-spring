package org.springframework.samples.homepix;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RedirectLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
								HttpServletResponse response,
								Authentication authentication)
            throws IOException {

        String redirectTo = request.getParameter("redirectTo");

        if (redirectTo == null || redirectTo.isBlank()) {
            redirectTo = "/";
        }

        response.sendRedirect(redirectTo);
    }
}
