package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.categories.Category;
import org.springframework.samples.homepix.portfolio.categories.CategoryRepository;
import org.springframework.samples.homepix.portfolio.keywords.*;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Collection;

@Service
public class PictureService {

	@Autowired
	private PictureFileRepository pictureFileRepository;

	@Autowired
	private LocationRepository locationRepository; // To fetch Location objects

	@Autowired
	private LocationRelationshipsRepository locationRelationshipsRepository; // To fetch Location objects

	@Autowired
	private CategoryRepository categoryRepository; // To fetch Category objects

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipRepository;

	@Autowired
	private KeywordRepository keywordRepository;

	@Autowired
	private KeywordService keywordService;

	public List<PictureFile> findAll() {
		// CrudRepository's findAll() returns an Iterable, convert to List
		return StreamSupport.stream(pictureFileRepository.findByFolderName("Brescia").spliterator(), false)
			.collect(Collectors.toList());
	}

	public List<PictureFile> findByBucket(String bucket) {
		// CrudRepository's findAll() returns an Iterable, convert to List
		return StreamSupport.stream(pictureFileRepository.findByFolderName(bucket).spliterator(), false)
			.collect(Collectors.toList());
	}

	public List<PictureFile> findByLocation(String name) {

		return locationRelationshipsRepository.findByLocation(name).stream()
			.map(LocationRelationship::getPicture)
			.collect(Collectors.toList());
	}

	private void updatePicture(Map<String, Object> update, String filename) throws Exception {

		Collection<PictureFile> pictures = pictureFileRepository.findByFilename(filename);

		if (pictures != null) {

			for (PictureFile picture : pictures) {

				// Iterate over the map and update fields dynamically
				for (Map.Entry<String, Object> entry : update.entrySet()) {

					String fieldName = entry.getKey();
					Object fieldValue = entry.getValue();

					if (!fieldName.equals("filename") && !fieldName.equals("filenames")) {  // Skip filename, it's for lookup

						if (fieldName.equals("location")) {

							Integer locationId = (Integer) update.get("location"); // Assuming location is passed as ID

							// Fetch the Location object by ID
							Location location = locationRepository.findById(locationId)
								.orElseThrow(() -> new IllegalArgumentException("Invalid location ID"));

							System.out.println("Location: " + location.getId() + ", " + location.getLocation());

							// Set the Location object in the PictureFile
							//picture.setLocation(location);
						} else if (fieldName.equals("plain_location")) {

							String location_text = (String) update.get("plain_location"); // Assuming location is passed as ID

							// Fetch the Location object by ID
							Collection<Location> existing = locationRepository.findByPlaceName(location_text);

							Location newLocation;

							if (existing.isEmpty()) {

								newLocation = new Location();
								newLocation.setLocation(location_text);
								this.locationRepository.save(newLocation);
							} else
								newLocation = existing.iterator().next();

							System.out.println("Location: " + newLocation.getId() + ", " + newLocation.getLocation());

							// Set the Location object in the PictureFile
							//picture.setLocation(newLocation);
							this.pictureFileRepository.save(picture);
						} else if (fieldName.equals("GPSLocation")) {

							String locationGPS = (String) update.get("GPSLocation"); // Assuming location is passed as ID

							System.out.println("Location: " + locationGPS);
						} else if (fieldName.equals("category1") || fieldName.equals("category2")) {

							Integer categoryId = 0;

							if (fieldName.equals("category1")) {
								categoryId = (Integer) update.get("category1");
							} else {
								categoryId = (Integer) update.get("category2");
							}

							// Fetch the Location object by ID
							Category category = categoryRepository.findById(categoryId)
								.orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

							System.out.println("Location: " + category.getId() + ", " + category.getCategory());

							if (fieldName.equals("category1")) {
								picture.setPrimaryCategory(category);
							} else if (fieldName.equals("category2")) {
								picture.setSecondaryCategory(category);
							}
						} else if (fieldName.equals("keywords")) {

							ArrayList<String> keywords = (ArrayList<String>) update.get("keywords");
							System.out.println(keywords);

							for (String word : keywords) {
								keywordService.addKeywordToPicture(picture, word);
							}
						} else {
							updateField(picture, fieldName, fieldValue);
						}

						// Save the updated picture
						pictureFileRepository.save(picture);
					}
				}
				// Save the updated picture
				pictureFileRepository.save(picture);
			}
		}
	}

	@Transactional
	public void updatePictures(List<Map<String, Object>> updates) throws Exception {

		for (Map<String, Object> update : updates) {

			// Fetch the "filename" field as the key to identify the record
			String filename = (String) update.get("filename");

			if (filename != null) {
				updatePicture(update, filename);
			}
			else {

				// Look for multiple filenames
				ArrayList<String> filenames = (ArrayList<String>) update.get("filenames");

				for (String file : filenames) {
					System.out.println(file);
					updatePicture(update, file);
				}
			}
		}
	}

	@Transactional
	public void patchLocations(List<Map<String, Object>> locations) throws Exception {

		int count = 0;

		for (Map<String, Object> location : locations) {

			// Fetch the "filename" field as the key to identify the record
			String filename = (String) location.get("filename");

			if (filename != null) {

				Collection<PictureFile> pictures = pictureFileRepository.findByFilename(filename);

				if (pictures != null) {

					for (PictureFile picture : pictures) {

						try {

							double latitude = (double) location.get("latitude");
							double longitude = (double) location.get("longitude");
							picture.setLatitude((float) latitude);
							picture.setLongitude((float) longitude);
							count++;
							pictureFileRepository.save(picture);
						}
						catch (Exception e) {
							System.out.println(e.getStackTrace());
						}
					}
				}
			}
		}
		System.out.println(count);
	}

	@Transactional
	public void patchLocationsAsBatch(List<Map<String, Object>> locations) {

		// Extract all filenames in the list for a single fetch query
		List<String> filenames = locations.stream()
			.map(loc -> (String) loc.get("filename"))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		// Fetch all relevant PictureFiles in a single query
		List<PictureFile> pictures = pictureFileRepository.findByFilenames(filenames);

		// Create a map for fast lookup by filename
		Map<String, List<PictureFile>> pictureMap = pictures.stream()
			.collect(Collectors.groupingBy(PictureFile::getFilename));

		int count = 0;

		// Loop over the locations and update the corresponding PictureFile entries
		for (Map<String, Object> location : locations) {
			String filename = (String) location.get("filename");

			if (filename != null && pictureMap.containsKey(filename)) {
				Collection<PictureFile> pictureFiles = pictureMap.get(filename);

				double latitude = (double) location.get("latitude");
				double longitude = (double) location.get("longitude");

				for (PictureFile picture : pictureFiles) {
					picture.setLatitude((float) latitude);
					picture.setLongitude((float) longitude);
					count++;
				}
			}
		}

		// Save all updated PictureFiles in batch
		pictureFileRepository.saveAll(pictures);
		System.out.println("Total updated records: " + count);
	}

	private void updateField(PictureFile picture, String fieldName, Object fieldValue) throws Exception {
		Field field = PictureFile.class.getDeclaredField(fieldName);
		field.setAccessible(true);  // Allow access to private fields
		field.set(picture, fieldValue);  // Set the value of the field dynamically
	}

	@Transactional
	public List<Map<String, Object>> getAllPicturesWithKeywords(String bucket) throws Exception {

		List<PictureFile> pictureFiles = StreamSupport.stream(pictureFileRepository.findByFolderName(bucket).spliterator(), false)
			.collect(Collectors.toList());

		List<Map<String, Object>> result = new ArrayList<>();

		for (PictureFile picture : pictureFiles) {

			Map<String, Object> pictureProperties = getPictureFileProperties(picture);
			pictureProperties.remove("class"); // Optional: Remove the "class" property

			// Retrieve the keywords for this picture
			List<String> keywords = keywordRelationshipRepository.findByPictureId(picture.getId())
				.stream()
				.map(relationship -> relationship.getKeyword().getWord())
				.collect(Collectors.toList());

			// Add the keywords to the map
			pictureProperties.put("keywords", keywords);

			result.add(pictureProperties);
		}

		return result;
	}

	public Map<String, Object> getPictureFileProperties(PictureFile picture) throws IllegalAccessException {

		Map<String, Object> properties = new HashMap<>();
		for (Field field : PictureFile.class.getDeclaredFields()) {
			field.setAccessible(true); // Allow access to private fields
			properties.put(field.getName(), field.get(picture));
		}
		return properties;
	}
}
