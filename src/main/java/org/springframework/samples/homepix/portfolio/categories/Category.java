package org.springframework.samples.homepix.portfolio.categories;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category extends BaseEntity {

	@Column(name = "category")
	@NotEmpty
	private String category;
}
