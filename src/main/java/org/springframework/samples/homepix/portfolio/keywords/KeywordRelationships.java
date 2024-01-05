package org.springframework.samples.homepix.portfolio.keywords;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@Entity
@Getter
@Setter
@Table(name = "keyword_relationships")
public class KeywordRelationships extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "keyword_id")
	private Keywords keywords;

	@ManyToOne
	@JoinColumn(name = "entry_id")
	private PictureFile pictureFile;

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).toString();
	}
}
