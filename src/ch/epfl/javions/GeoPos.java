package ch.epfl.javions;

import static ch.epfl.javions.Units.Angle.*;
import static ch.epfl.javions.Units.convert;
import static ch.epfl.javions.Units.convertFrom;

/**
 * Public: Represents a longitude/latitude pair
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {
    private static final double MAX_ABSOLUTE_LATITUDE_T32 = Math.scalb(1, 30);
    /**
     * Compact constructor of GeoPos
     *
     * @param longitudeT32 (int): Longitude expressed in T32
     * @param latitudeT32  (int): Latitude expressed in T32
     * @throws IllegalArgumentException If the given Latitude is invalid
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Determines if the given latitude is valid
     *
     * @param latitudeT32 (int): Input latitude
     * @return (boolean): True if the Input latitude is valid
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return (-MAX_ABSOLUTE_LATITUDE_T32 <= latitudeT32) && (latitudeT32 <= MAX_ABSOLUTE_LATITUDE_T32);
    }

    /**
     * Converts the longitude in radians
     *
     * @return (double): Longitude expressed in radians
     */
    public double longitude() {return convertFrom(longitudeT32, T32);}

    /**
     * Converts the latitude in radians
     *
     * @return (double): Latitude expressed in radians
     */
    public double latitude() {return convertFrom(latitudeT32, T32);}

    @Override
    public String toString() {
        return "(" + convert(longitudeT32, T32, DEGREE)
                + "°, "
                + convert(latitudeT32, T32, DEGREE) + "°)";
    }
}