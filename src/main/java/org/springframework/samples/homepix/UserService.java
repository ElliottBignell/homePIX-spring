package org.springframework.samples.homepix;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void createUser(String username, String password) {

		String hashedPassword = passwordEncoder.encode(password);

		User user = new User();
		user.setUsername(username);
		user.setPassword(hashedPassword);
		user.setEnabled(true);

		userRepository.save(user);
	}

	public boolean changeUserPassword(String username, String newPassword) {
		// Retrieve the user (this part depends on your user management implementation)
		Optional<User> users = userRepository.findByUsername(username);

		if (!users.isEmpty()) {

			// Encode the new password
			String encodedPassword = passwordEncoder.encode(newPassword);

			User user = users.get();

			// Update the user's password
			user.setPassword(encodedPassword);

			// Save the user with the new password
			userRepository.save(user);

			return true;
		}

		return false;
	}
}
