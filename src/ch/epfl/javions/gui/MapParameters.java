package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Public: Represents the parameters of the visible portion of the map
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class MapParameters {
    private static final int MIN_ZOOM = 6;
    private static final int MAX_ZOOM = 19;
    private final IntegerProperty zoomProperty;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;

    /**
     * Default MapParameters constructor.
     * Defines the map parameters
     *
     * @param zoom (int): Zoom level
     * @param minX (int): X coordinate of the top-left corner
     * @param minY (int): Y coordinate of the top-left corner
     * @throws IllegalArgumentException If the given zoom is not between the zoom limits
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(Math2.clamp(MIN_ZOOM, zoom, MAX_ZOOM) == zoom);

        this.zoomProperty = new SimpleIntegerProperty(zoom);
        this.minXProperty = new SimpleDoubleProperty(minX);
        this.minYProperty = new SimpleDoubleProperty(minY);
    }

    /**
     * Returns the property of the zoom level
     *
     * @return (ReadOnlyIntegerProperty): The property
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoomProperty;
    }

    /**
     * Returns the zoom level
     *
     * @return (int): Zoom level
     */
    public int getZoom() { return zoomProperty.get(); }

    /**
     * Returns the property of the X coordinate
     *
     * @return (ReadOnlyDoubleProperty): Min X property
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minXProperty;
    }

    /**
     * Returns the X coordinate
     *
     * @return (double): X coordinate
     */
    public double getMinX() { return minXProperty.get(); }

    /**
     * Returns the property of the Y coordinate
     *
     * @return (ReadOnlyDoubleProperty): Min Y property
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minYProperty;
    }

    /**
     * Returns the Y coordinate
     *
     * @return (double): Y coordinate
     */
    public double getMinY() { return minYProperty.get(); }

    /**
     * Adds the given zoom difference to the current zoom level, keeping in within the limits
     *
     * @param zoomDelta (int): Given zoom difference
     */
    public void changeZoomLevel(int zoomDelta) {
        int oldZoom = getZoom();
        int newZoom = Math2.clamp(MIN_ZOOM, oldZoom + zoomDelta, MAX_ZOOM);

        minXProperty.set(Math.scalb(getMinX(), newZoom - oldZoom));
        minYProperty.set(Math.scalb(getMinY(), newZoom - oldZoom));
        this.zoomProperty.set(newZoom);
    }

    /**
     * Translates the top-left corner of the given coordinates
     *
     * @param x (double): Given x coordinate
     * @param y (double): Given y coordinate
     */
    public void scroll(double x, double y) {
        minXProperty.set(getMinX() + x);
        minYProperty.set(getMinY() + y);
    }
}
