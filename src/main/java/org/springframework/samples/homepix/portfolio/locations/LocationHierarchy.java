package org.springframework.samples.homepix.portfolio.locations;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.samples.homepix.model.BaseEntity;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "location_hierarchy")
public class LocationHierarchy extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "child_location_id", nullable = false)
	private Location childLocation;

	@ManyToOne
	@JoinColumn(name = "parent_location_id", nullable = false)
	private Location parentLocation;
}
