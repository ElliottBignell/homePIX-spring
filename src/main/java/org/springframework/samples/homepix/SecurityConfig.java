package org.springframework.samples.homepix;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final UserDetailsService userDetailsService;

	UserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final RedirectLogoutSuccessHandler logoutSuccessHandler;
	private final CustomLoginSuccessHandler customLoginSuccessHandler;

	@Autowired
	SecurityConfig(UserRepository userRepository,
				   UserDetailsService userDetailsService,
				   CustomLoginSuccessHandler customLoginSuccessHandler,
				   RedirectLogoutSuccessHandler logoutSuccessHandler
				   ) {
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
		this.customLoginSuccessHandler = customLoginSuccessHandler;
		this.logoutSuccessHandler = logoutSuccessHandler;
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
	public CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
		CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
		handler.setCsrfRequestAttributeName("_csrf");
		return handler;
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
					"/downloads/*/*.tar.gz",
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
					"/prelogin",
					"/cart/buy",
					"/cart",
					"/cart/delete",
					"/cart/choose/*",
					"/cart/add/*",
					"/payment/success/*",
					"/webhooks/stripe",
					"/webhooks/paypal",
					"/test-mail",
					"/create-checkout-session",
					"/debug/mappings"
				)
				.permitAll()
				.anyRequest().authenticated()
			.and()
			.formLogin(form -> form
				.loginPage("/login")  // Ensure login page is registered
				.loginProcessingUrl("/login")
				.successHandler(customLoginSuccessHandler)
				.permitAll()
			)
            .logout(logout -> logout
                .logoutUrl("/logout")   // must match your form POST
                .logoutSuccessHandler(logoutSuccessHandler)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("aSecureAndPrivateKey")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .userDetailsService(userDetailsService) // ✅ CRUCIAL
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
				});

		logger.info("Form login configured with custom login page at /login");
		logger.info("Default success URL after login is set to /api/keywords");
		logger.info("Logout URL configured at /logout");

		http.csrf(csrf -> csrf
			.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler())
			.ignoringRequestMatchers("/prelogin", "/logout")
		);

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
