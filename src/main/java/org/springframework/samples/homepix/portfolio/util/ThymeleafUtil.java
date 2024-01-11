package org.springframework.samples.homepix.portfolio.util;

import java.util.List;

public class ThymeleafUtil {
	public static List<?> chunkList(List<?> list, int chunkSize) {
		// Implement logic to chunk the list here
		// For simplicity, you can use your existing ChunkUtil or another method
		return ChunkUtil.chunkList(list, chunkSize);
	}
}
