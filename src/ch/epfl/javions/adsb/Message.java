package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Public: Defines methods to receive data from the messages and their expeditor
 *
 * @author Pablo Robin Guerrero (356671)
 */
public interface Message {

    /**
     * Determines the timestamp of the message, in nanoseconds
     *
     * @return (long): The timestamp of the message
     */
    long timeStampNs();

    /**
     * Determines the ICAO Address of the sender
     *
     * @return (IcaoAddress): The ICAO Address
     */
    IcaoAddress icaoAddress();
}
