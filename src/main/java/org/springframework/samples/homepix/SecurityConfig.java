package org.springframework.samples.homepix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.samples.homepix.UserRepository;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
public class SecurityConfig {

	@Bean
	public CustomSecurityConfigurer customSecurityConfigurer(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder, UserRepository userRepository) {
		return new CustomSecurityConfigurer(userDetailsService, passwordEncoder, userRepository);
	}

	public static class CustomSecurityConfigurer
			extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

		private final UserDetailsService userDetailsService;

		private final PasswordEncoder passwordEncoder;

		private final UserRepository userRepository;

		@Autowired
		public CustomSecurityConfigurer(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
				UserRepository userRepository) {

			this.userDetailsService = userDetailsService;
			this.passwordEncoder = passwordEncoder;
			this.userRepository = userRepository;
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
				.requestMatchers("/", "/buckets/**", "/albums/**").permitAll() // Public pages
				.anyRequest().authenticated() // All other pages require authentication
				.and()
				.authorizeRequests()
				.requestMatchers("/static/**")
				.permitAll()
				.and()
				.formLogin()
				.loginPage("/login")
				.permitAll()
				.and()
				.logout()
				.permitAll();


			// Add additional configuration if needed
			http.addFilterBefore(new YourCustomAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

			// Log some information for debugging
			http.authorizeRequests().and().exceptionHandling().accessDeniedHandler((request, response, e) -> {
				// Log the exception or print some debug information
				e.printStackTrace();
			});
		}

		// Additional configuration methods can be added here

		// Custom authentication filter example
		private static class YourCustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

			// Implement your custom authentication logic here

		}

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SpringSecurityDialect springSecurityDialect() {
		return new SpringSecurityDialect();
	}
}
