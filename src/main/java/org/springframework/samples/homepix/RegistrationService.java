package org.springframework.samples.homepix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	public RegistrationService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void registerUser(String username, String email, String password) {

		User user = new User();

		user.setUsername(username);
		user.setEnabled(true);
		user.setPassword(passwordEncoder.encode(password));

		userRepository.save(user);
	}
}
