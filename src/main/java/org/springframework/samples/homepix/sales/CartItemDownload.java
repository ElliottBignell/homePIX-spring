package org.springframework.samples.homepix.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item_download")
@Getter
@Setter
public class CartItemDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private LocalDateTime createdAt;

	@Column(nullable = true)
	private LocalDateTime downloadedAt;

	public CartItemDownload() {

		this.filename = "";
		this.username = "";
	}

    public CartItemDownload(String filename, String username) {

        this.filename = filename;
		this.username = username;
        this.createdAt = LocalDateTime.now();
		this.downloadedAt = null;
    }

    public String getDownloadUrl(String username) {
        return "/downloads/" + username + "/" + filename;
    }
}
