package org.springframework.samples.homepix.portfolio.comments;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "picture_id")
	private PictureFile picture_id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user")
	private User user;

	@Column(name = "text")
	@Size(max=1023)
	@NotBlank(message = "Comment text is mandatory")
	@NotEmpty
	private String text;

	@Column(name = "date")
	@NotNull
	private LocalDateTime date;
}
