package org.springframework.samples.homepix.portfolio.locations;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "location")
public class Location extends BaseEntity {

	@Column(name = "location")
	@NotEmpty
	private String location;
}
