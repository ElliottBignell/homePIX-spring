package org.springframework.samples.homepix;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Simple JavaBean domain object representing an Authority
 *
 * @author Elliott Bignell
 */
@Entity
@Table(name = "authoritories")
public class Authority {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "authority_id")
	private long authority_id;

	@Column(name = "user_id")
	@NotBlank(message = "User id is mandatory")
	@NotEmpty
	private int user_id;

	@Column(name = "authority")
	@NotBlank(message = "Authority name is mandatory")
	@NotEmpty
	private String authority;

}
