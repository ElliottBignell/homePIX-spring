package org.springframework.samples.homepix;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@WebFilter("/*")
public class CanonicalRedirectFilter implements Filter {
	private static final String PREFERRED_DOMAIN = "www.homepix.ch";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String serverName = httpRequest.getServerName();

		// Allow localhost and 127.0.0.1
		if (serverName.equalsIgnoreCase("localhost") || serverName.equals("127.0.0.1")) {
			chain.doFilter(request, response);
			return;
		}

		// Redirect non-preferred domains
		if (!serverName.equalsIgnoreCase(PREFERRED_DOMAIN)) {
			String redirectUrl = "https://" + PREFERRED_DOMAIN + httpRequest.getRequestURI();
			httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			httpResponse.setHeader("Location", redirectUrl);
			return;
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}
}
