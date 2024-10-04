package org.springframework.samples.homepix.portfolio.keywords;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@Entity
@Getter
@Setter
@Table(name = "keyword_relationships_new")
public class KeywordRelationships extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "keyword_id")
	private Keyword keyword;

	@ManyToOne
	@JoinColumn(name = "entry_id")
	private PictureFile pictureFile;

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).toString();
	}

	public Integer getPictureId() {
		return (pictureFile != null) ? pictureFile.getId() : null;
	}
	public Keyword getKeyword() { return keyword; }
}

