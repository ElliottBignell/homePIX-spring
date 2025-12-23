package org.springframework.samples.homepix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	UserRepository userRepository;

	@Autowired
	CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Retrieve user details from the database
		// Example using a UserRepository
		Optional<User> users = userRepository.findByUsername(username);

		// If user not found, throw UsernameNotFoundException
		if (users == null || users.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}

		// Ensure that your User entity implements UserDetails or create a UserDetails
		// adapter
		// Example assuming User implements UserDetails
		User user = users.get();

		if (user.getRole() == Role.ROLE_ADMIN) {
			return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
				.password(user.getPassword()).roles("ADMIN", "USER") // TODO: Replace with the actual roles
				.build();
		}
		else {
			return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
				.password(user.getPassword()).roles("USER") // TODO: Replace with the actual roles
				.build();
		}
	}

}
