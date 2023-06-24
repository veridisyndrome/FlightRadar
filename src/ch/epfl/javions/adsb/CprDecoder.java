package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import static ch.epfl.javions.GeoPos.isValidLatitudeT32;
import static ch.epfl.javions.Preconditions.checkArgument;
import static ch.epfl.javions.Units.Angle.*;

/**
 * Public: Represents a decoder of CPR position
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class CprDecoder {
    private static final int LATITUDE_EVEN_ZONE = 60, LATITUDE_ODD_ZONE = 59;
    private static final int MOST_RECENT_EVEN_VALUE = 0, MOST_RECENT_ODD_VALUE = 1;
    private static final double LATITUDE_ZONE_NUMERATOR = 1 - Math.cos(TURN / LATITUDE_EVEN_ZONE);

    /**
     * Default CprDecoder constructor.
     * Defined as private to prevent instantiations of the class
     */
    private CprDecoder() {}
    /**
     * Determines the geographical position corresponding to the given normalized local position
     *
     * @param x0         (double): Local longitude of an even message
     * @param y0         (double): Local latitude of an even message
     * @param x1         (double): Local longitude of an odd message
     * @param y1         (double): Local latitude of an odd message
     * @param mostRecent (int): Parity of the most recent message
     * @return (GeoPos): Geographical position
     * @throws IllegalArgumentException If mostRecent is not equal to 0 or 1
     */
    public static GeoPos decodePosition(double x0,
                                        double y0,
                                        double x1,
                                        double y1,
                                        int mostRecent) {
        checkArgument(
                mostRecent == MOST_RECENT_EVEN_VALUE ||
                mostRecent == MOST_RECENT_ODD_VALUE);

        int latitudeZone = (int) Math.rint(y0 * LATITUDE_ODD_ZONE - y1 * LATITUDE_EVEN_ZONE);

        double latitudeEven = latitude(y0, latitudeZone, LATITUDE_EVEN_ZONE);
        double latitudeOdd = latitude(y1, latitudeZone, LATITUDE_ODD_ZONE);

        int longitudeEvenZone = longitudeZone(latitudeEven);
        int longitudeOddZone = longitudeZone(latitudeOdd);

        // We verify that the aircraft zone didn't change
        if (longitudeEvenZone != longitudeOddZone) {
            return null;
        } else if (longitudeEvenZone != 1) {
            longitudeOddZone -= 1;
        }

        int longitudeZone = (int) Math.rint(x0 * longitudeOddZone - x1 * longitudeEvenZone);

        double longitudeEven = longitude(longitudeEvenZone, longitudeZone, x0);
        double longitudeOdd = longitude(longitudeOddZone, longitudeZone, x1);

        return geoPos(latitudeOdd,latitudeEven,longitudeOdd, longitudeEven, mostRecent);
    }

    /**
     * Calculates the latitude at which the aircraft was located.
     *
     * @param y            (double): Local latitude
     * @param latitudeZone (int): Latitude zone
     * @param latitudeType (int): Latitude type
     * @return (double): Latitude
     */
    private static double latitude(double y, int latitudeZone, int latitudeType) {
        int latitudeIndex = latitudeZone < 0 ? latitudeZone + latitudeType : latitudeZone;
        double latitude = (latitudeIndex + y) / latitudeType;

        return latitude < 0.5 ? latitude : latitude - 1;
    }

    /**
     * Calculates the longitude at which the aircraft was located.
     *
     * @param longitudeZone       (int): Longitude zone
     * @param longitudeZoneIndex (int): Longitude zone index
     * @param x                   (double): Local longitude
     * @return (double): Longitude
     */
    private static double longitude(int longitudeZone, int longitudeZoneIndex, double x) {
        int longitudeIndex;

        if (longitudeZone == 1) {
            return x;
        } else if (longitudeZoneIndex < 0) {
            longitudeIndex = longitudeZoneIndex + longitudeZone;
        } else {
            longitudeIndex = longitudeZoneIndex;
        }

        double longitude = (longitudeIndex + x) / longitudeZone;

        return longitude < 0.5 ? longitude : longitude - 1;
    }

    /**
     * Calculates the longitude zone at which the aircraft was located
     *
     * @param latitude (double): Latitude
     * @return (int): Longitude Zone
     */
    private static int longitudeZone(double latitude) {
        double cosLat = Math.cos(Units.convertFrom(latitude, TURN));
        double a = Math.acos(1 - LATITUDE_ZONE_NUMERATOR/ (cosLat * cosLat));

        double longitudeZone = Math.floor(TURN / a);
        
        return Double.isNaN(longitudeZone) ? 1 : (int) longitudeZone;
    }


    /**
     * Actual calculator of the geographical position defined in the constructor
     *
     * @param latitudeOdd   (double): Latitude of an odd message
     * @param latitudeEven  (double): Latitude of an even message
     * @param longitudeOdd  (double): Longitude of an odd message
     * @param longitudeEven (double): Latitude of an even message
     * @param mostRecent    (int): Parity of the most recent message
     * @return (GeoPos): Geographical position
     */
    private static GeoPos geoPos(double latitudeOdd, double latitudeEven,
                                 double longitudeOdd, double longitudeEven, int mostRecent) {
        latitudeEven = Math.rint(Units.convert(latitudeEven, TURN, T32));
        latitudeOdd = Math.rint(Units.convert(latitudeOdd, TURN, T32));

        if (mostRecent == 0 && !isValidLatitudeT32((int) latitudeEven)) {
            return null;
        } else if(mostRecent == 1 && !isValidLatitudeT32((int) latitudeOdd)) {
            return null;
        }

        return mostRecent == 0 ?
                new GeoPos((int) Math.rint(Units.convert(longitudeEven, TURN, T32)),
                        (int) latitudeEven) :
                new GeoPos((int) Math.rint(Units.convert(longitudeOdd, TURN, T32)),
                        (int) latitudeOdd);
    }
}
