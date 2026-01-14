package org.springframework.samples.homepix.portfolio.seo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumContent;
import org.springframework.samples.homepix.portfolio.album.AlbumContentRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class SEOController extends PaginationController {

	private final AlbumContentRepository albumContent;

	private static final String format = "yyyy-MM-dd";
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

	private Supplier<String> today = () -> {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	};

	protected SEOController(
		AlbumRepository albums,
		AlbumContentRepository albumContent,
		KeywordRepository keyword,
		KeywordRelationshipsRepository keywordsRelationships,
		FolderService folderService
	) {
		super(albums, keyword, keywordsRelationships, folderService);
		this.albumContent = albumContent;
	}

	@GetMapping(value = "/ads.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> adstext() {

		// Example sitemap content. In a real application, generate this dynamically
		String adsTxtContent = "google.com, pub-2242083496336489, DIRECT, f08c47fec0942fa0";

		return ResponseEntity.ok(adsTxtContent);
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> robots() {

		String text = """
			User-agent: *
			Allow: /
			Disallow: /collection/delete/
			Disallow: /collection/move/
			Disallow: /admin/
			Disallow: /login/
			Disallow: /actuator/
			Disallow: /api/
			Disallow: /logout/
			Sitemap: https://www.homepix.ch/sitemap.xml
			""";
		return ResponseEntity.ok(text);
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> sitemap() {

		// Example sitemap content. In a real application, generate this dynamically
		String sitemapContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
			"   <url>\n" +
			"       <loc>https://www.homepix.ch/</loc>\n" +
			"       <lastmod>2025-01-09</lastmod>\n" +
			"       <changefreq>monthly</changefreq>\n" +
			"       <priority>1.0</priority>\n" +
			"   </url>\n" +
			this.getAlbumTags().stream().collect(Collectors.joining("\n")) +
			this.getFolderTags().stream().collect(Collectors.joining("\n")) +
			//this.getAnnualTags().stream().collect(Collectors.joining("\n")) +
			"</urlset>";

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_XML);

		return new ResponseEntity<>(sitemapContent, httpHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/album{id}.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> album(@PathVariable("id") long id,
										Authentication authentication
	) {

		Optional<Album> album = this.albums.findById(id);

		List<String> contents;
		String pictures = "";

		if (album.isPresent()) {

			contents = getAlbumContentTags(album.get(),authentication);
			pictures = String.join("\n", contents);
		}

		String sitemapContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
			"   <url>\n" +
			"       <loc>https://www.homepix.ch/albums/1</loc>\n" +
			"       <lastmod>2025-01-09</lastmod>\n" +
			"       <changefreq>monthly</changefreq>\n" +
			"       <priority>1.0</priority>\n" +
			"   </url>\n" +
				pictures +
			"</urlset>";

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_XML);

		return new ResponseEntity<>(sitemapContent, httpHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/allPictures.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> allPictures(Authentication authentication)
	{
		Iterable<PictureFile> pictures = this.pictureFiles.findAll();

		return ResponseEntity.ok(StreamSupport.stream(pictures.spliterator(), false)
            .map(picture -> picture.getFolder().getName() + "/" +  picture.getFilename())
			.collect(Collectors.joining("\n"))
		);
	}

	@GetMapping(value = "/folder{name}.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> folder(@PathVariable("name") String name,
										 Authentication authentication
	) {

		Collection<Folder> folder = this.folders.findByName(name);

		List<String> contents;
		String pictures = "";

		if (!folder.isEmpty()) {

			contents = getFolderContentTags(folder.iterator().next(), authentication);
			pictures = String.join("\n", contents);
		}

		String sitemapContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
			"   <url>\n" +
			"       <loc>https://www.homepix.ch/buckets/" + name + "</loc>\n" +
			"       <lastmod>2025-01-09</lastmod>\n" +
			"       <changefreq>monthly</changefreq>\n" +
			"       <priority>1.0</priority>\n" +
			"   </url>\n" +
			pictures +
			"</urlset>";

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_XML);

		return new ResponseEntity<>(sitemapContent, httpHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/index.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> index() {

		// Example sitemap content. In a real application, generate this dynamically
		String sitemapContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
								"<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
								  "<sitemap>" +
									"<loc>https://www.homepix.ch/sitemap.xml</loc>" +
									"<lastmod>2024-11-04</lastmod>" +
									"</sitemap>" +
									this.getAlbumSiteTags().stream().collect(Collectors.joining("\n")) +
									this.getFolderSiteTags().stream().collect(Collectors.joining("\n")) +
								 "<!-- Additional sitemap entries here -->" +
								"</sitemapindex>";

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_XML);

		return new ResponseEntity<>(sitemapContent, httpHeaders, HttpStatus.OK);
	}

	List<String> getAlbumTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(albums.findAll().iterator(), 0), false)
			.map(album -> {

				String date = album.getLastModifiedDate() != null ? album.getLastModifiedDate().toString() : "2024-11-04";

				return "<url>\n" +
					"<loc>https://www.homepix.ch/album/" + album.getId() + "</loc>\n" +
					"<lastmod>" +
					date +
					"</lastmod>\n" +
					"<changefreq>weekly</changefreq>\n" +
					"<priority>0.8</priority>\n" +
					"</url>";
			})
			.collect(Collectors.toList());
	}

	List<String> getAlbumSiteTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(albums.findAll().iterator(), 0), false)
			.map(album -> {

				String date = album.getLastModifiedDate() != null ? album.getLastModifiedDate().toString() : "2024-11-04";

				return "<sitemap>\n" +
					   "<loc>https://www.homepix.ch/album" + album.getId() + ".xml</loc>\n" +
					   "<lastmod>" + date + "</lastmod>\n" +
					   "</sitemap>";
			})
			.collect(Collectors.toList());
	}

	List<String> getAlbumContentTags(Album album, Authentication authentication) {

		Collection<PictureFile> content = albumContent.findByAlbumId(album.getId()).stream()
			.map(AlbumContent::getPictureFile)
			.collect(Collectors.toList());

		return content.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.map( picture -> {

			return "<url>\n" +
				"<loc>https://www.homepix.ch/albums/" + album.getId() + "/item/" + picture.getId() + "</loc>\n" +
				"<lastmod>" + picture.getAdded_on() + "</lastmod>\n" +
				"<changefreq>monthly</changefreq>\n" +
				"<priority>0.8</priority>\n" +
				"</url>";
		})
		.collect(Collectors.toList());
	}

	List<String> getAnnualTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(calendar.getItems().iterator(), 0), false)
			.flatMap(yearGroup ->
				StreamSupport.stream(Spliterators.spliteratorUnknownSize(yearGroup.getYears().iterator(), 0), false)
					.map(year -> "<url>" +
						"<loc>https://www.homepix.ch/calendar/" + year.getYear() + "</loc>\n" +
						"<lastmod>2025-01-09</lastmod>\n" +
						"<changefreq>weekly</changefreq>\n" +
						"<priority>0.8</priority>\n" +
						"</url>")
			)
			.collect(Collectors.toList());
	}

	List<String> getFolderTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(folderService.getSortedFolders().iterator(), 0), false)
			.map(folder -> {
				// Here you generate your XML tag string based on the album properties
				// Adjust property names as necessary
				return "<url>\n" +
					"<loc>https://www.homepix.ch/buckets/" + folder.getName() + "</loc>\n" +
					"<lastmod>" + String.format(folder.getLastModifiedDate().toString(), format) + "</lastmod>\n" +
					"<changefreq>monthly</changefreq>\n" +
					"<priority>0.8</priority>\n" +
					"</url>";
			})
			.collect(Collectors.toList());
	}

	List<String> getFolderSiteTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(folderService.getSortedFolders().iterator(), 0), false)
			.map(folder -> {

				String date = folder.getLastModifiedDate() != null ? folder.getLastModifiedDate().toString() : "2024-11-04";

				return "<sitemap>\n" +
					"<loc>https://www.homepix.ch/folder" + folder.getName() + ".xml</loc>\n" +
					"<lastmod>" + date + "</lastmod>\n" +
					"</sitemap>";
			})
			.collect(Collectors.toList());
	}

	List<String> getFolderContentTags(Folder folder, Authentication authentication) {

		final String imagePath = System.getProperty("user.dir") + "/images/";

		List<PictureFile> pictureFiles = this.pictureFiles.findByFolderName(folder.getName());

		AtomicInteger index = new AtomicInteger();

		return pictureFiles.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.map( picture -> {

				return "<url>" +
					"<loc>https://www.homepix.ch/buckets/" + folder.getName() + "/item/" + index.getAndIncrement() + "</loc>\n" +
					"<lastmod>" + picture.getAdded_on() + "</lastmod>\n" +
					"<changefreq>monthly</changefreq>\n" +
					"<priority>0.8</priority>\n" +
					"</url>";
			})
			.collect(Collectors.toList());
	}}
