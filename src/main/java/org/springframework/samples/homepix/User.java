package org.springframework.samples.homepix;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails, CredentialsContainer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private long user_id;

	@Column(name = "username")
	@NotBlank(message = "User name is mandatory")
	@NotEmpty
	private String username;

	@Column(name = "password")
	@NotBlank(message = "Password name is mandatory")
	@NotEmpty
	private String password;

	@Column(name = "enabled")
	private boolean enabled;

	@Override
	public void eraseCredentials() {

	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		String roles = "ADMIN,USER";

		// Split roles into a list
		String[] rolesArray = roles.split(",");

		// Create SimpleGrantedAuthority for each role
		if (username.equals("elliottcb")) {
			return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		else {
			return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		}
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

}
