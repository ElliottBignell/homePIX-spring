package org.springframework.samples.homepix.sales;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CartDownload {

    private final Long orderNo;
    private final String filename;
	private final String downloadUrl;
	private final LocalDateTime downloadedAt;

    public CartDownload(Long orderNo, String downloadUrl, LocalDateTime downloadedAt) {
        this.orderNo = orderNo;
        this.filename = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
		this.downloadedAt = downloadedAt;
		this.downloadUrl = downloadUrl;
    }
}
