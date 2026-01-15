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
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final UserDetailsService userDetailsService;

	UserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final RedirectLogoutSuccessHandler logoutSuccessHandler;
	private final CustomLoginSuccessHandler customLoginSuccessHandler;

	@Autowired
	CsrfAccessDeniedHandler csrfAccessDeniedHandler;

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

    CookieCsrfTokenRepository repo =
        CookieCsrfTokenRepository.withHttpOnlyFalse();

    repo.setCookieCustomizer(cookie ->
        cookie.sameSite("None").secure(true)
    );

    http
        // HTTPS behind proxy
        .requiresChannel(channel -> channel
            .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            .requiresSecure()
        )

        // CSRF
        .csrf(csrf -> csrf
            .csrfTokenRepository(repo)
            .ignoringRequestMatchers("/webhooks/**")
        )

        // AUTHORIZATION — ONE BLOCK ONLY
        .authorizeHttpRequests(auth -> auth
            // Stripe
            .requestMatchers("/webhooks/**").permitAll()

            // Public resources
            .requestMatchers(
                "/",
                "/error",
                "/error/**",
                "/index.xml",
                "/licence.html",
                "/about/",
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
                "/robots.txt",
                "/api/**",
                "/maps/**",
                "/chart/**",
                "/actuator/**",
                "/prelogin",
                "/cart",
                "/cart/choose/*",
                "/payment/success/*",
                "/payment/failure/*",
                "/payments/stripe/**"
            ).permitAll()

            // EVERYTHING ELSE
            .anyRequest().authenticated()
        )

        // LOGIN
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(customLoginSuccessHandler)
            .failureUrl("/prelogin?error")
            .permitAll()
        )

        // LOGOUT — ONCE
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler(logoutSuccessHandler)
            .permitAll()
        )

        // REMEMBER ME
        .rememberMe(remember -> remember
            .key("aSecureAndPrivateKey")
            .tokenValiditySeconds(7 * 24 * 60 * 60)
            .userDetailsService(userDetailsService)
        )

        // EXCEPTIONS
        .exceptionHandling(ex -> ex
            .accessDeniedHandler(new CsrfAccessDeniedHandler())
            .authenticationEntryPoint((req, res, e) ->
                res.sendRedirect("/prelogin?redirectTo=" +
                    UriUtils.encode(req.getRequestURI(), StandardCharsets.UTF_8))
            )
        )

        // HEADERS
        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin())
        );

    return http.build();
}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**", "/dist/**", "/static/**");
	}

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setSameSite("None");
		serializer.setUseSecureCookie(true);
		serializer.setCookiePath("/");
		return serializer;
	}
}
