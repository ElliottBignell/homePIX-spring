package org.springframework.samples.homepix.sales;

public enum ImageResolution {

    THUMBNAIL(300,200),
    MEDIUM(1500,1000),
    ORIGINAL(-1, -1); // original size

    private final int width;
    private final int height;

    ImageResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
