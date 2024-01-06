package org.springframework.samples.homepix.portfolio.keywords;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "keyword")
public class Keyword extends BaseEntity {

	@Column(name = "word")
	@NotEmpty
	private String word = "";
}
