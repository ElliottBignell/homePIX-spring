package org.springframework.samples.homepix.portfolio.locations;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRelationshipId implements Serializable {

	private Long picture;
	private Long location;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LocationRelationshipId that = (LocationRelationshipId) o;
		return Objects.equals(picture, that.picture) && Objects.equals(location, that.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(picture, location);
	}
}
