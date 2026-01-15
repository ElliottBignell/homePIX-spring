package org.springframework.samples.homepix;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log =
		Logger.getLogger(RequestLoggingFilter.class.getName());

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		long start = System.currentTimeMillis();

		filterChain.doFilter(request, response);

		long duration = System.currentTimeMillis() - start;

		String ip = extractClientIp(request);
		String ua = request.getHeader("User-Agent");

		Level level = isBotLike(request) ? Level.FINE : Level.INFO;
		boolean botLike = isBotLike(request);

		log.log(level, String.format(
			"event=http_request bot=%s method=%s path=%s status=%d ip=%s duration_ms=%d ua=\"%s\"",
			botLike,
			request.getMethod(),
			request.getRequestURI(),
			response.getStatus(),
			ip,
			duration,
			ua
		));
	}

	private String extractClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private boolean isBotLike(HttpServletRequest request) {

		String ua = request.getHeader("User-Agent");
		if (ua == null || ua.isBlank()) return true;

		String u = ua.toLowerCase();

		return u.contains("bot")
			|| u.contains("crawler")
			|| u.contains("spider")
			|| u.contains("scan")
			|| u.contains("curl")
			|| u.contains("wget")
			|| u.contains("python")
			|| u.contains("java")
			|| u.contains("go-http-client");
	}
}

