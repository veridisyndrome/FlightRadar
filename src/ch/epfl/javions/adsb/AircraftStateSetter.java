package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Public: Defines methods to modify various properties of the messages and aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public interface AircraftStateSetter {
    /**
     * Changes the timestamp of the last received message to the given value.
     *
     * @param timeStampNs (long): Given new timestamp value
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Changes the category of the aircraft to the given value
     *
     * @param categoryProperty (int): Given category value
     */
    void setCategory(int categoryProperty);

    /**
     * Changes the call sign of the aircraft to the given value
     *
     * @param callSignProperty (CallSign): Given call sign value
     */
    void setCallSign(CallSign callSignProperty);

    /**
     * Changes the position of the aircraft to the given value
     *
     * @param geoPos (GeoPos): Given position value
     */
    void setPosition(GeoPos geoPos);

    /**
     * Changes the altitude of the aircraft to the given value
     *
     * @param altitudeProperty (double): Given altitude value
     */
    void setAltitude(double altitudeProperty);

    /**
     * Changes the velocity of the aircraft to the given value
     *
     * @param velocityProperty (double): Given velocity value
     */
    void setVelocity(double velocityProperty);

    /**
     * Changes the direction of the aircraft to the given value
     *
     * @param trackOrHeadingProperty (double): Given direction value
     */
    void setTrackOrHeading(double trackOrHeadingProperty);
}
