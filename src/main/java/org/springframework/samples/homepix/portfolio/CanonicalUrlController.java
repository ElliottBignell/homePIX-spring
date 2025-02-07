package org.springframework.samples.homepix.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CanonicalUrlController {

    @GetMapping
    public String redirectToCanonical(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        if (requestUrl.contains("homepix.ch") && !requestUrl.contains("www.homepix.ch")) {
            return "redirect:https://www.homepix.ch" + request.getRequestURI();
        }
        return null; // No redirection needed
    }
}
