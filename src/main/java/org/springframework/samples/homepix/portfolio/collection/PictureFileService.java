package org.springframework.samples.homepix.portfolio.collection;

import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.maps.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.samples.homepix.portfolio.filtering.DateRangeSpecification;
import org.springframework.samples.homepix.portfolio.filtering.PictureFileSpecification;
import org.springframework.samples.homepix.portfolio.filtering.SearchTextSpecification;
import org.springframework.samples.homepix.portfolio.filtering.SortDirection;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.stereotype.Service;

// Service Layer
@Service
public class PictureFileService {

	protected final PictureFileRepository pictureFileRepository;

	@Autowired
	protected final FolderRepository folderRepository;

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipRepository;

	@Autowired
    public PictureFileService(PictureFileRepository pictureFiles, FolderRepository folderRepository) {
        this.pictureFileRepository = pictureFiles;
		this.folderRepository = folderRepository;
	}

    public List<PictureFile> getPhotos(Date startDate, Date endDate, String searchText, SortDirection sortDirection) {

		// Create specifications based on input parameters
		PictureFileSpecification dateRangeSpec = new DateRangeSpecification(startDate, endDate);
		PictureFileSpecification searchTextSpec = new SearchTextSpecification(searchText);

		// Create a composite specification
		Specification<PictureFile> compositeSpec = Specification.where(dateRangeSpec).and(searchTextSpec);

		// Create a sort strategy based on input parameter
		Sort sort = Sort.by(sortDirection == SortDirection.ASC ? Sort.Order.asc("fieldName") : Sort.Order.desc("fieldName"));

		// Apply specifications and sorting strategy to retrieve data
		List<PictureFile> result = pictureFileRepository.findAll(compositeSpec, sort);

		return result;
	}

	public List<double[]> getNearbyPositions(double refLatitude, double refLongitude, int radius) {

		// Call the repository to find nearby pictures
		List<Object[]> results = pictureFileRepository.findPositionsWithinRadiusWithoutCrowding(refLatitude, refLongitude, radius);

		// Transform the result into a list of coordinates (latitude, longitude)
		List<double[]> nearbyCoordinates = new ArrayList<>();
		for (Object[] result : results) {
			double latitude = ((Number) result[1]).doubleValue();
			double longitude = ((Number) result[2]).doubleValue();
			nearbyCoordinates.add(new double[]{latitude, longitude});
		}

		return nearbyCoordinates;
	}

	public List<PictureFile> getNearbyPictures(double refLatitude, double refLongitude, int radius) {

		// Call the repository to find nearby pictures
		List<PictureFile> results = pictureFileRepository.findPicturesWithinRadiusWithoutCrowding(refLatitude, refLongitude, radius);

		return results;
	}

	public void addMapDetails(PictureFile file, Map<String, Object> model) {

		int radius = 1000;

		if (null != file.getLatitude() && null != file.getLongitude()) {

			// Get the reference image's latitude and longitude
			double refLatitude = file.getLatitude();
			double refLongitude = file.getLongitude();

			List<double[]> nearbyCoordinates = getNearbyPositions(refLatitude, refLongitude, radius);
			String key = System.getenv("GOOGLE_MAPS_KEY");
			String staticMapUrl = generateGoogleStaticMapWithMarkers(refLatitude, refLongitude, nearbyCoordinates, key);
			String liveMapUrl = generateGoogleMapsUrlWithMarkers(nearbyCoordinates, refLatitude, refLongitude);

			model.put("mapUrl", staticMapUrl);
			model.put("gpslink", liveMapUrl);
		}
	}

	public void addMapDetails(Iterable<PictureFile> files, Map<String, Object> model) {

		String key = System.getenv("GOOGLE_MAPS_KEY");
		Optional<String> staticMapUrl = generateGoogleStaticMapWithMarkers(files, key);
		Optional<String> liveMapUrl = generateGoogleMapsUrlWithMarkers(files);

		if (staticMapUrl.isPresent()) {

			model.put("mapUrl", staticMapUrl.get());
			model.put("gpslink", liveMapUrl);
		}
	}

	public String generateGoogleStaticMapWithMarkers(double refLatitude, double refLongitude, List<double[]> coordinates, String key) {

		// Base URL for the Static Map
		StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");

		// Default parameters like zoom, size, etc.
		urlBuilder.append("size=300x300");

		int limit = 10;

		// Add markers for each set of coordinates
		for (double[] coord : coordinates) {

			double latitude = coord[0];
			double longitude = coord[1];
			if (latitude != refLatitude && longitude != refLongitude) {
				urlBuilder.append("&markers=color:yellow|").append(latitude).append(",").append(longitude);
			}

			if (--limit <= 0) {
				break;
			}
		}

		urlBuilder.append("&markers=scale:5|color:red|label:R|").append(refLatitude).append(",").append(refLongitude);

		// Append API key
		urlBuilder.append("&key=").append(key);

		return urlBuilder.toString();
	}

	public Optional<String> generateGoogleStaticMapWithMarkers(Iterable<PictureFile> files, String key) {

		// Base URL for the Static Map
		StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");

		// Default parameters like zoom, size, etc.
		urlBuilder.append("size=300x200");

		int count = 0;

		// Add markers for each set of coordinates
		for (PictureFile pictureFile : files) {

			if (null != pictureFile.getLatitude() && null != pictureFile.getLongitude()) {

				urlBuilder.append("&markers=color:yellow|")
					.append(pictureFile.getLatitude())
					.append(",")
					.append(pictureFile.getLongitude());

				count++;

				if (count > 30) {
					break;
				}
			}
		}

		// Append API key
		urlBuilder.append("&key=").append(key);

		if (count > 0) {
			return Optional.ofNullable(urlBuilder.toString());
		}
		else {
			return Optional.ofNullable(null);
		}
	}

	public String generateGoogleMapsUrlWithMarkers(List<double[]> coordinates, double refLatitude, double refLongitude) {
		StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/");

		// Add the reference marker first with default text
		urlBuilder.append(refLatitude).append(",").append(refLongitude).append("/");

		// Add the rest of the nearby markers
		for (double[] coord : coordinates) {
			urlBuilder.append(coord[0]).append(",").append(coord[1]).append("/");
		}

		// Optionally add a default text for the reference marker (e.g., Picture by E.C. Bignell)
		urlBuilder.append("@").append(refLatitude).append(",").append(refLongitude)
			.append(",14z?hl=en");

		return urlBuilder.toString();
	}

	public Optional<String> generateGoogleMapsUrlWithMarkers(Iterable<PictureFile> files) {

		StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/");

		int count = 0;

		// Add the rest of the nearby markers
		for (PictureFile pictureFile : files) {

			if (null != pictureFile.getLatitude() && null != pictureFile.getLongitude()) {
				urlBuilder.append(pictureFile.getLatitude())
					.append(",")
					.append(pictureFile.getLongitude())
					.append("/");

				count++;
			}
		}

		// Optionally add a default text for the reference marker (e.g., Picture by E.C. Bignell)
		urlBuilder.append(",14z?hl=en");

		if (count > 0) {
			return Optional.ofNullable(urlBuilder.toString());
		}
		else {
			return Optional.ofNullable(null);
		}
	}

	public String loadMapPreview(Map<String, Object> model, String name) {

		Collection<Folder> folder = this.folderRepository.findByName(name);

		if (!folder.isEmpty()) {

			final String imagePath = System.getProperty("user.dir") + "/images/";

			List<PictureFile> content = pictureFileRepository.findByFolderName(name);

			if (!content.isEmpty()) {

				StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/");

				int limit = 10;

				// Add the rest of the nearby markers
				for (PictureFile pictureFile : content) {

					if (null != pictureFile.getLatitude() && null != pictureFile.getLongitude()) {
						urlBuilder.append(pictureFile.getLatitude())
							.append(",")
							.append(pictureFile.getLongitude())
							.append("/");

						if (--limit <= 0) {
							break;
						}
					}
				}

				List<List<Float>> nearbyCoordinates = content
					.stream()
					.filter(element -> element.getLatitude() != null && element.getLongitude() != null)
					.map(element -> Arrays.asList(element.getLatitude(), element.getLongitude()))
					.collect(Collectors.toList());
				List<PictureFile> nearbyPictures = content
					.stream()
					.filter(element -> element.getLatitude() != null && element.getLongitude() != null)
					.collect(Collectors.toList());

				if (!nearbyPictures.isEmpty()) {

					Map.Entry<MapUtils.LatLng, Integer> result = MapUtils.calculateCenterAndZoom(nearbyPictures);

					MapUtils.LatLng center = result.getKey();
					int zoom = result.getValue();

					model.put("zoom", zoom);
					model.put("refLatitude", nearbyPictures.iterator().next().getLatitude());
					model.put("refLongitude", nearbyPictures.iterator().next().getLongitude());
					model.put("picture", content.iterator().next());
					model.put("nearbyCoordinates", nearbyCoordinates);
					model.put("nearbyPictures", nearbyPictures);

					return "picture/pictureMap.html";			}
			}
		}

		return "redirect:/buckets/" + name;
	}

	public Page<PictureFile> getComplexSearchPageByLocation(String searchText,
												  LocalDate first,
												  LocalDateTime last,
												  boolean isAdmin,
												  String userRoles,
												  Pageable pageable
	) {
		return this.pictureFileRepository.findByLocationOnly(
			searchText,
			pageable
		);
	}

	public Page<PictureFile> getComplexSearchPage(String searchText,
										   LocalDate first,
										   LocalDateTime last,
										   boolean isAdmin,
										   String userRoles,
										   Pageable pageable
	) {

		Pattern keywordPattern = Pattern.compile("[:](.*)");
		Matcher keywordMmatcher = keywordPattern.matcher(searchText);

		if (keywordMmatcher.find()) {

			if (searchText.equals(":!*")) {

				return this.pictureFileRepository.findByNoKeywordsAndDateRangeAndValidityAndAuthorization(
					first,
					last,
					isAdmin,
					userRoles,
					pageable
				);
			} else {

				return this.pictureFileRepository.findByWordInKeyword(
					keywordMmatcher.group(1),
					first,
					last,
					isAdmin,
					userRoles,
					pageable
				);
			}
		} else {

			Pattern pattern = Pattern.compile("[\"'](.*)[\"']");
			Matcher matcher = pattern.matcher(searchText);

			if (matcher.find()) {

				return this.pictureFileRepository.findByWholeWordInTitleOrFolderOrKeywordAndDateRangeAndValidityAndAuthorization(
					matcher.group(1),
					first,
					last,
					isAdmin,
					userRoles,
					pageable
				);
			} else {

				return this.pictureFileRepository.findByWordInTitleOrFolderOrKeywordAndDateRangeAndValidityAndAuthorization(
					searchText,
					first,
					last,
					isAdmin,
					userRoles,
					pageable
				);
			}
		}
	}

	public void applyArguments(Map<String, Object> model, CollectionRequestDTO requestDTO)
	{
		model.put("startDate", requestDTO.getFromDate());
		model.put("endDate", requestDTO.getToDate());
		model.put("sort", requestDTO.getSort());
		model.put("search", requestDTO.getSearch());

		String arguments = "";

		if (!requestDTO.getSearch().equals("")) {
			arguments += "search=" + requestDTO.getSearch();
		}
		if (arguments.length() > 0) {
			arguments = '?' + arguments + "#";
		}

		model.put("arguments", arguments);
	}

	@Cacheable("folderByName")
	public List<PictureFile> findByFolderName(String name) {
		return pictureFileRepository.findByFolderName(name);
	}

	@Cacheable("folderByNameAndParameters")
	public List<PictureFile> folderByNameAndParameters( String folder, String searchText, LocalDate startDate, LocalDate endDate ) {
		return pictureFileRepository.findByFolderName( folder, searchText, startDate, endDate );
	}

	// existing getExifEntries(...) and setDate(...) live here

	public void setDate(PictureFile picture, Map<String, String> properties) {

		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
			"yyyy:MM:dd HH:mm:ss",
			Locale.ENGLISH
		);

		try {

			String dateTimeString = properties.get("ProfileDateTime");

			if (dateTimeString.equals("0000:00:00 00:00:00")) {
				dateTimeString = properties.get("DateTimeOriginal");
			}

			if (null != dateTimeString) {

				final String plus = "[+]";

				if (dateTimeString.contains("+")) {

					String[] parts = dateTimeString.split(plus);
					dateTimeString = parts[0];
				}

				final String minus = "[-]";

				if (dateTimeString.contains("+")) {

					String[] parts = dateTimeString.split(minus);
					dateTimeString = parts[0];
				}

				if (null != dateTimeString && !dateTimeString.equals("0000:00:00 00:00:00")) {

					LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

					try {
						picture.setTaken_on(dateTime);
					}
					catch (Exception ex) {
						System.out.println(ex);
					}
				}
			}
		}
		catch (Exception ex) {

			// TODO: Duplicate date code
			System.out.println(ex);
			ex.printStackTrace();

			final String format = "yyyy:MM:dd HH:mm:ss";

			Supplier<String> today = () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
				LocalDateTime now = LocalDateTime.now();
				return dtf.format(now);
			};
			LocalDateTime dateTime = LocalDateTime.parse(today.get(), formatter);

			picture.setTaken_on(dateTime);
		}
	}

	@Transactional
	public DateUpdateResultDto updateDatesFromExif(PaginationController controller,List<String> pictureIds) throws IOException {

		int totalIdsProvided = pictureIds.size();
		List<String> updatedIds = new ArrayList<>();
		List<String> skippedIds = new ArrayList<>();
		List<String> notFoundIds = new ArrayList<>();

		for (String idString : pictureIds) {
			Integer id;
			try {
				id = Integer.valueOf(idString);
			} catch (NumberFormatException e) {
				// ID not parseable -> treat as "not found"
				notFoundIds.add(idString);
				continue;
			}

			Optional<PictureFile> optPicture = pictureFileRepository.findById(id);

			if (optPicture.isEmpty()) {
				notFoundIds.add(idString);
				continue;
			}

			PictureFile picture = optPicture.get();

			if (!needsDateUpdate(picture)) {
				skippedIds.add(idString);
				continue;
			}

			// Get EXIF key / name from the picture
			String exifName = getExifNameForPicture(picture);

			Map<String, String> properties = controller.getExifEntries(exifName);

			// This function already exists per your description
			setDate(picture, properties);

			pictureFileRepository.save(picture);

			updatedIds.add(idString);
		}

		int picturesFound = totalIdsProvided - notFoundIds.size();
		int updatedCount = updatedIds.size();
		int skippedCount = skippedIds.size();

		return new DateUpdateResultDto(
			totalIdsProvided,
			picturesFound,
			updatedCount,
			skippedCount,
			updatedIds,
			skippedIds,
			notFoundIds
		);
	}

	/**
	 * Decide whether the picture needs its date updated.
	 * Adjust this if dateTaken is not a String in your model.
	 */
	private boolean needsDateUpdate(PictureFile picture) {

		String dateTaken = picture.getTaken_on().toString(); // adjust getter as needed

		return dateTaken == null
			|| "0000:00:00 000:00:00".equals(dateTaken)
			|| "1970:01:01 00:00:00".equals(dateTaken);
	}

	/**
	 * How to derive the EXIF object key from a PictureFile.
	 * Adapt this to your actual model (s3Key, path, etc.).
	 */
	private String getExifNameForPicture(PictureFile picture) {
		// Example – change to match your entity fields:
		return picture.getFolderName();
		// or picture.getS3Key() + ".exif.xml"
		// or whatever you already use when calling getExifEntries(...)
	}
}
