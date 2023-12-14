package org.springframework.samples.homepix;

import org.springframework.security.crypto.password.PasswordEncoder;

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

}
