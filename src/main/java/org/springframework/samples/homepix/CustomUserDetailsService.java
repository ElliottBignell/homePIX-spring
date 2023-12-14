package org.springframework.samples.homepix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

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
		Collection<User> user = userRepository.findByName(username);

		// If user not found, throw UsernameNotFoundException
		if (user == null || user.isEmpty()) {
			throw new UsernameNotFoundException("User not found");
		}

		// Ensure that your User entity implements UserDetails or create a UserDetails
		// adapter
		// Example assuming User implements UserDetails
		return (UserDetails) user.iterator().next();
	}

}
