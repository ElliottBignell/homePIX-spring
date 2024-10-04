package org.springframework.samples.homepix.portfolio.locations;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "locations")
public class Location extends BaseEntity {

	@Column(name = "location")
	@NotEmpty
	private String location;
}
