package org.springframework.samples.homepix.portfolio.util;

import java.util.ArrayList;
import java.util.List;

public class ChunkUtil {
	public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {

		List<List<T>> result = new ArrayList<>();

		if (null != list) {

			for (int i = 0; i < list.size(); i += chunkSize) {
				int end = Math.min(i + chunkSize, list.size());
				result.add(list.subList(i, end));
			}
		}

		return result;
	}
}
