package org.springframework.samples.homepix.portfolio.calendar;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@Getter
@Setter
@Entity
@Table(name = "years")
public class YearThumbnail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "`year`")
	private int year;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "thumbnail_id")
	private PictureFile thumbnail;
}
