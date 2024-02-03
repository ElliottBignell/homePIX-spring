package org.springframework.samples.homepix.portfolio.seo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.FolderRepository;
import org.springframework.samples.homepix.portfolio.FolderService;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Spliterators;
import java.util.List;

@RestController
public class SEOController extends PaginationController {
	protected SEOController(
		AlbumRepository albums,
		FolderRepository folders,
		PictureFileRepository pictureFiles,
		KeywordRepository keyword,
		KeywordRelationshipsRepository keywordsRelationships,
		FolderService folderService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> sitemap() {

		// Example sitemap content. In a real application, generate this dynamically
		String sitemapContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
			"   <url>\n" +
			"       <loc>https://www.homepix.ch/</loc>\n" +
			"       <lastmod>2024-01-01</lastmod>\n" +
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

	List<String> getAlbumTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(albums.findAll().iterator(), 0), false)
			.map(album -> {
				// Here you generate your XML tag string based on the album properties
				// Adjust property names as necessary
				return "<url>" +
					"<loc>https://www.homepix.ch/album/" + album.getId() + "</loc>" +
					"<lastmod>" + album.getLastModifiedDate().toString() + "</lastmod>" +
					"<changefreq>weekly</changefreq>" +
					"<priority>0.8</priority>" +
					"</url>";
			})
			.collect(Collectors.toList());
	}

	List<String> getAnnualTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(calendar.getItems().iterator(), 0), false)
			.flatMap(yearGroup ->
				StreamSupport.stream(Spliterators.spliteratorUnknownSize(yearGroup.getYears().iterator(), 0), false)
					.map(year -> "<url>" +
						"<loc>https://www.homepix.ch/calendar/" + year.getYear() + "</loc>" +
						"<lastmod>2024-02-03</lastmod>" +
						"<changefreq>weekly</changefreq>" +
						"<priority>0.8</priority>" +
						"</url>")
			)
			.collect(Collectors.toList());
	}

	List<String> getFolderTags() {

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(folders.findAll().iterator(), 0), false)
			.map(folder -> {
				// Here you generate your XML tag string based on the album properties
				// Adjust property names as necessary
				return "<url>" +
					"<loc>https://www.homepix.ch/album/" + folder.getName() + "</loc>" +
					"<lastmod>" + folder.getLastModifiedDate().toString() + "</lastmod>" +
					"<changefreq>monthly</changefreq>" +
					"<priority>0.8</priority>" +
					"</url>";
			})
			.collect(Collectors.toList());
	}
}
