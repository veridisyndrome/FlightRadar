package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents an identification ADS-B message of a given category
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress,
                                            int category, CallSign callSign) implements Message {
    private static final int CALL_SIGN_CHARACTER_SIZE = 6, CALL_SIGN_SIZE = 8;
    private static final int CA_START_INDEX = 48, CA_SIZE = 3;
    private static final int CATEGORY_MSB_COEFF = 14;
    private static final String ALPHABET= "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? " +
            "???????????????0123456789??????";

    /**
     * Compact constructor of AircraftIdentificationMessage
     *
     * @param timeStampNs (long): Timestamp of the message expressed in nanoseconds
     * @param icaoAddress (IcaoAddress): ICAO address of the sender
     * @param category    (int): Category of the aircraft
     * @param callSign    (CallSign): Call sign of the sender
     * @throws NullPointerException If icaoAddress or callSign are null
     * @throws IllegalArgumentException If timeStampNs is strictly negative
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        checkArgument(0 <= timeStampNs);
    }

    /**
     * Determines the identification message corresponding to the raw message
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (AircraftIdentificationMessage): Corresponding identification message
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();

        String callSign = callSignB(payload);

        return callSign.contains("?") ? null :
                new AircraftIdentificationMessage(
                        rawMessage.timeStampNs(),
                        rawMessage.icaoAddress(),
                        categoryCalculator(payload, rawMessage),
                        new CallSign(callSign));
    }

    /**
     * Determines the string of the call sign corresponding to the given payload
     *
     * @param payload (long): Given payload
     * @return (String): Corresponding string
     */
    private static String callSignB(long payload) {
        StringBuilder callSignB = new StringBuilder(CALL_SIGN_SIZE);

        for (int i = 0; i < Byte.SIZE; i++) {
            int character = Bits.extractUInt(
                    payload,
                    CALL_SIGN_CHARACTER_SIZE * i,
                    CALL_SIGN_CHARACTER_SIZE);

            if (' ' == ALPHABET.charAt(character) && callSignB.isEmpty()) continue;

            callSignB.append(ALPHABET.charAt(character));
        }
        return callSignB.reverse().toString();
    }

    /**
     * Calculates the category of the aircraft
     *
     * @param payload    (long): Given payload
     * @param rawMessage (RawMessage): Raw message sent by the aircraft
     * @return (int): Corresponding category
     */
    private static int categoryCalculator(long payload , RawMessage rawMessage) {
        int ca = Bits.extractUInt(payload, CA_START_INDEX, CA_SIZE);
        int typeCode = rawMessage.typeCode();

        return ((CATEGORY_MSB_COEFF - typeCode) << CA_SIZE + 1) |  ca;
    }
}
