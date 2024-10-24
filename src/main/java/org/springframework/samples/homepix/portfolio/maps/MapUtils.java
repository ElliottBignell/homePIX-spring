package org.springframework.samples.homepix.portfolio.maps;

import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtils {

	public static class LatLng {
		private final double latitude;
		private final double longitude;

		public LatLng(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}
	}

	// Method to calculate the center and zoom level based on PictureFile collection
	public static Map.Entry<LatLng, Integer> calculateCenterAndZoom(Collection<PictureFile> pictureFiles) {
		// Get statistics for latitude and longitude
		DoubleSummaryStatistics latStats = pictureFiles.stream()
			.mapToDouble(PictureFile::getLatitude)
			.summaryStatistics();

		DoubleSummaryStatistics lngStats = pictureFiles.stream()
			.mapToDouble(PictureFile::getLongitude)
			.summaryStatistics();

		// Calculate center of the bounding box
		double centerLat = (latStats.getMin() + latStats.getMax()) / 2;
		double centerLng = (lngStats.getMin() + lngStats.getMax()) / 2;

		// Calculate distance in degrees for both latitude and longitude
		double latDiff = latStats.getMax() - latStats.getMin();
		double lngDiff = lngStats.getMax() - lngStats.getMin();

		// Calculate maximum diff and determine zoom level
		double maxDiff = Math.max(latDiff, lngDiff);
		int zoomLevel = calculateZoomLevel(maxDiff);

		// Return center and zoom level
		return Map.entry(new LatLng(centerLat, centerLng), zoomLevel);
	}

	// Helper method to estimate zoom level based on max distance in degrees
	private static int calculateZoomLevel(double maxDiff) {
		// Example scale; you might want to adjust this for your application
		if (maxDiff < 0.002) return 15; // High zoom for close-up
		else if (maxDiff < 0.01) return 14;
		else if (maxDiff < 0.05) return 13;
		else if (maxDiff < 0.2) return 12;
		else if (maxDiff < 1) return 9;
		else if (maxDiff < 2) return 8;
		else if (maxDiff < 3) return 7;
		else if (maxDiff < 4) return 7;
		else if (maxDiff < 5) return 6;
		else return 4; // Low zoom for large areas
	}
}
