package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PictureRangeService {

	@Autowired
	private PictureFileRepository pictureFileRepository;

	public List<PictureFile> getPictureRangeByIds(Map<String, Object> updates, String ids) {

		final Pattern series = Pattern.compile("[0-9]+-[0-9]+");

		String sStart = ids;
		String sEnd = ids;

		Matcher hideMatcher = series.matcher(ids);

		if (hideMatcher.find()) {

			String[] idArray = ids.split( "-" );

			sStart = idArray[0];
			sEnd = idArray[1];
		}

		Long start = Long.parseLong(sStart);
		Long end = Long.parseLong(sEnd);

		List<String> response = new ArrayList<>();

		List<PictureFile> pictureFiles = pictureFileRepository.findAllIdRange(start, end);

		// Retrieve the `criteria` object
		Map<String, Object> criteria = (Map<String, Object>) updates.get("criteria");
		if (criteria != null) {

			String title = (String) criteria.get("title");
			if (title != null) {
				pictureFiles = pictureFiles.stream()
					.filter(file -> file.getTitle().equals(title))
					.collect(Collectors.toList());
			}
		}

		return pictureFiles;
	}
}
