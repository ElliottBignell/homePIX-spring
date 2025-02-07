package org.springframework.samples.homepix.portfolio.locations;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "location_relationships")
@IdClass(LocationRelationshipId.class)
public class LocationRelationship extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "picture_id", nullable = false)
	private PictureFile picture; // ✅ Correct reference

	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location; // ✅ Correct reference
}
