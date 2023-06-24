package ch.epfl.javions.adsb;

/**
 * Public: Transforms raw ADS-B messages into one of three types of message:
 * identification message, airborne position message of airborne velocity
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class MessageParser {
    private static final int POSITION_TYPE_CODE_1 = 9, POSITION_TYPE_CODE_2 = POSITION_TYPE_CODE_1 * 2,
            POSITION_TYPE_CODE_3 = 20, POSITION_TYPE_CODE_4 = 22;
    private static final int IDENTIFICATION_TYPE_1 = 1, IDENTIFICATION_TYPE_2 = 4;
    private static final int VELOCITY_TYPE_CODE = 19;

    /**
     * Default MessageParser constructor
     * Defined as private to prevent instantiations of the class
     */
    private MessageParser() {}

    /**
     * Determines the instantiation of the type corresponding to the given raw message
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (Message): Corresponding message
     */
    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();

        if (typeCode == VELOCITY_TYPE_CODE)
            return AirborneVelocityMessage.of(rawMessage);

        if ((POSITION_TYPE_CODE_1 <= rawMessage.typeCode() &&
                        rawMessage.typeCode() <= POSITION_TYPE_CODE_2) ||
                (POSITION_TYPE_CODE_3 <= rawMessage.typeCode() &&
                        rawMessage.typeCode() <= POSITION_TYPE_CODE_4))
            return AirbornePositionMessage.of(rawMessage);


        if (IDENTIFICATION_TYPE_1 <= typeCode && typeCode <= IDENTIFICATION_TYPE_2)
            return AircraftIdentificationMessage.of(rawMessage);

        return null;
    }
}