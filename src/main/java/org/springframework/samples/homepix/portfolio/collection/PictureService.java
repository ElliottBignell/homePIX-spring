package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.categories.Category;
import org.springframework.samples.homepix.portfolio.categories.CategoryRepository;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;

@Service
public class PictureService {

	@Autowired
	private PictureFileRepository pictureFileRepository;

	@Autowired
	private LocationRepository locationRepository; // To fetch Location objects

	@Autowired
	private CategoryRepository categoryRepository; // To fetch Category objects

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipRepository;

	@Autowired
	private KeywordRepository keywordRepository;

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
							picture.setLocation(location);
						} else if (fieldName.equals("plain_location")) {

							String location_text = (String) update.get("plain_location"); // Assuming location is passed as ID

							// Fetch the Location object by ID
							Collection<Location> existing = locationRepository.findByContent(location_text);

							Location newLocation;

							if (existing.isEmpty()) {

								newLocation = new Location();
								newLocation.setLocation(location_text);
								this.locationRepository.save(newLocation);
							} else
								newLocation = existing.iterator().next();

							System.out.println("Location: " + newLocation.getId() + ", " + newLocation.getLocation());

							// Set the Location object in the PictureFile
							picture.setLocation(newLocation);
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

								Collection<Keyword> existing = keywordRepository.findByContent(word.toLowerCase());
								Keyword newKeyword;

								if (existing.isEmpty()) {

									newKeyword = new Keyword();
									newKeyword.setWord(word);
									this.keywordRepository.save(newKeyword);
								} else
									newKeyword = existing.iterator().next();

								Collection<KeywordRelationships> relations = this.keywordRelationshipRepository.findByBothIds(picture.getId(), newKeyword.getId());

								if (relations.isEmpty()) {

									KeywordRelationships relation = new KeywordRelationships();
									relation.setPictureFile(picture);
									relation.setKeyword(newKeyword);
									this.keywordRelationshipRepository.save(relation);
								}
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
