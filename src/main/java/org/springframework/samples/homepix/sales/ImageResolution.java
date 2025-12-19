package org.springframework.samples.homepix.sales;

public enum ImageResolution {

    THUMBNAIL(200),
    MEDIUM(1000),
    ORIGINAL(-1); // original size

    private final int maxWidth;

    ImageResolution(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }
}
