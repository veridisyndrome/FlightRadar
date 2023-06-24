package ch.epfl.javions;

/**
 * Public: Defines methods allowing to project geographical
 * coordinates basing on the WebMercator projection
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class WebMercator {
    /**
     * Default WebMercator constructor.
     * Defined as private to prevent instantiations of the class
     */
    private WebMercator() {}

    /**
     * Determines the x coordinate corresponding to the given longitude (in radians) at the given zoom level
     *
     * @param zoomLevel (int): Level of zoom desired
     * @param longitude (double): Given longitude
     * @return (double): x coordinate of the longitude
     */
    public static double x(int zoomLevel, double longitude) {
        return (Math.scalb(
                Units.convertTo(longitude, Units.Angle.TURN) + 0.5,
                8 + zoomLevel));
    }

    /**
     * Determines the y coordinate corresponding to the given latitude (in radians) at the given zoom level
     *
     * @param zoomLevel (int): Level of zoom desired
     * @param latitude  (double): Given latitude
     * @return (double): y coordinate of the latitude
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(
                Units.convertTo(-Math2.asinh(Math.tan(latitude)), Units.Angle.TURN) + 0.5,
                8 + zoomLevel);
    }
}

