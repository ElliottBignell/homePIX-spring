package org.springframework.samples.homepix;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	UserRepository userRepository;

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
					.requestMatchers("/login").permitAll()
					.requestMatchers(
						"/",
						"/buckets/**",
						"/albums/**",
						"/album/**",
						"/collection/**",
						"/calendar/**",
						"/web-images/**",
						"/resources/**",
						"/css/**",
						"/js/**",
						"/static/**",
						"/register",
						"/sitemap.xml",
						"/album*.xml",
						"/folder*.xml",
						"/index.xml"
					)
				.permitAll()
				.anyRequest().authenticated()
			.and()
				.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.permitAll()
			.and()
				.logout()
				.permitAll()
			.and()
				.csrf()
				.disable(); // For simplicity; handle CSRF properly in a production environment

		http.headers().frameOptions().sameOrigin();

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**");
	}
}
