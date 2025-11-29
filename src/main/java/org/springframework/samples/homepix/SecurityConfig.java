package org.springframework.samples.homepix;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	UserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	SecurityConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new CustomUserDetailsService(this.userRepository);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			// Redirect to HTTPS
			.requiresChannel()
			.requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
			.requiresSecure()
			.and()
				// Authorization and Authentication configuration
				.authorizeRequests()
					.requestMatchers(new RequestMatcher() {
						@Override
						public boolean matches(HttpServletRequest request) {
							// Check if the request URI ends with .xml
							return request.getRequestURI().endsWith(".xml");
						}
					}).permitAll()
			.requestMatchers(
				"/login",
					"/",
					"/error",
					"/error-404",
					"/error-403",
					"/error/**",
					"/index.xml",
					"/licence.html",
					"/about/",
					"/buckets/**",
					"/location/**",
					"/albums/**",
					"/album/**",
					"/collection/**",
					"/calendar/**",
					"/web-images/**",
					"/resources/**",
					"/fonts/**",
					"/css/**",
					"/js/**",
					"/dist/**",
					"/static/**",
					"/register",
					"/ads.txt",
					"/sitemap.xml",
					"/allPictures.txt",
					"/robots.txt",
					"/album*.xml",
					"/folder*.xml",
					"/api/pictures",
					"/api/pictures/**",
					"/api/keywords",
					"/api/keywords/**",
					"/api/albums",
					"/api/albums/**",
					"/api/folders",
					"/api/folders/**",
					"/api/chart-data/**",
					"/api/folders/**",
					"/api/locations/",
					"/api/locations/**",
					"/logs/docker/homepix",
					"/containers/**",
					"/actuator",
					"/actuator/**",
					"/maps/**",
					"/chart/**",
					"/admin/**",
					"/words",
					"/debug/mappings"
				)
				.permitAll()
				.anyRequest().authenticated()
			.and()
			.formLogin(form -> form
				.loginPage("/login")  // Ensure login page is registered
				.defaultSuccessUrl("/", true) // Redirect to home after login
				.permitAll()
			)
			.logout()
			.permitAll()
			.and()
			.exceptionHandling()
				// Handle 403 Forbidden errors
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.sendRedirect("/error-404");
				})
				// Handle 404 Not Found errors by directing to the error page
				.authenticationEntryPoint((request, response, authException) -> {
					response.sendRedirect("/error-403"); // Redirect all errors to your custom 404 page
				})
			.and()
			.csrf()
				.disable(); // For simplicity; handle CSRF properly in a production environment


		logger.info("Form login configured with custom login page at /login");
		logger.info("Default success URL after login is set to /api/keywords");
		logger.info("Logout URL configured at /logout");

		// Adding handlers to capture the request processing and debug further if necessary
		http.formLogin().successHandler((request, response, authentication) -> {
			logger.info("User {} successfully authenticated", authentication.getName());
			response.sendRedirect("/");  // Redirect after successful login
		});

		http.formLogin().failureHandler((request, response, exception) -> {
			logger.warn("Authentication failed: {}", exception.getMessage());
		});

		http.headers().frameOptions().sameOrigin();

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**", "/dist/**", "/static/**");
	}
}
