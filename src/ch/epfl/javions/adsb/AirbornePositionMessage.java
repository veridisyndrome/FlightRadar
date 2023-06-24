package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import static ch.epfl.javions.Preconditions.checkArgument;
import static ch.epfl.javions.Units.Length.FOOT;
import static java.util.Objects.requireNonNull;

/**
 * Public: Represents an airborne (position in flight) ADS-B message
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude,
                                      int parity, double x, double y) implements Message {
    private final static long MASK = 0xF;
    private static final int POSITION_NORMALISATION_VALUE = - 17;
    private static final int ALT_START_INDEX = 36, ALT_SIZE = 12;
    private static final int FORMAT_START_INDEX = 34;
    private static final int PARITY_SIZE = 1;
    private static final int LON_LAT_CPR_SIZE = 17, LON_CPR_INDEX = 0, LAT_CPR_INDEX = LON_LAT_CPR_SIZE;
    private static final int Q_INDEX = 4;
    private static final int MSB_SIZE = 9, LSB_SIZE = 3, LSB_START_INDEX = 0;
    private static final double INVALID_ALT = Double.NaN;
    private static final int INVALID_LSB_ALT_ONE = 0, INVALID_LSB_ALT_TWO = 5, INVALID_LSB_ALT_THREE = 6;
    private static final int CORRECTION_ALT_ZERO = -1_300, CORRECTION_ALT_1 = -1_000;
    private static final int MSB_COEFF_ALT_ZERO = 500, LSB_COEFF_ALT_ZERO = 100;
    private static final int ALT_COEFF_ONE = 25;
    private static final int NUMBER_GRAY_GROUP = 4, GRAY_GROUP_SIZE = 3;
    private static final int[] INDEX_TABLE = {0, 6, 1, 7};

    /**
     * Compact AirbornePositionMessage constructor
     *
     * @param timeStampNs (long): Timestamp of the message expressed in nanoseconds
     * @param icaoAddress (IcaoAddress): ICAO address of the sender
     * @param altitude    (double): Altitude at which the aircraft was located
     * @param parity      (int): Parity of the message
     * @param x           (double): Local normalized longitude at which the aircraft was located
     * @param y           (double): Local normalized latitude at which the aircraft was located
     * @throws NullPointerException If icaoAddress is null
     * @throws IllegalArgumentException If timeStampNs is strictly negative, if parity is not
     * equal to 0 or 1 or if x or y are not between 0 (included) and 1 (excluded)
     */
    public AirbornePositionMessage {
        requireNonNull(icaoAddress);
        checkArgument(0 <= timeStampNs);
        checkArgument(parity == 0 || parity == 1);
        checkArgument((0 <= x && x < 1) && (0 <= y && y < 1));

    }

    /**
     * Determines the position message corresponding to the raw message
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (AircraftPositionMessage): Corresponding position message
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        double altitude = altitude(rawMessage);
        long payload = rawMessage.payload();

        return Double.isNaN(altitude) ? null : new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                altitude,
                Bits.extractUInt(payload, FORMAT_START_INDEX, PARITY_SIZE),
                Math.scalb((double) Bits.extractUInt(
                                payload,
                                LON_CPR_INDEX, LON_LAT_CPR_SIZE),
                        POSITION_NORMALISATION_VALUE),
                Math.scalb((double) Bits.extractUInt(
                                payload,
                                LAT_CPR_INDEX, LON_LAT_CPR_SIZE),
                        POSITION_NORMALISATION_VALUE));
    }

    /**
     * Calculates the altitude of the aircraft
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (double): Corresponding altitude
     */
    private static double altitude(RawMessage rawMessage) {
        long altitudeContent = Bits.extractUInt(rawMessage.payload(), ALT_START_INDEX, ALT_SIZE);

        if (Bits.testBit(altitudeContent, Q_INDEX)) {
            int codedAltitude =
                    (int) (((altitudeContent >>> Q_INDEX + 1) << Q_INDEX) | (MASK & altitudeContent));
            int altitude = CORRECTION_ALT_1 + codedAltitude * ALT_COEFF_ONE;

            return altitude <= 0 ? 0 : Units.convertFrom(altitude, FOOT);
        } else {
            long message = disentangling(altitudeContent);
            long msbAltitude = Bits.extractUInt(message, LSB_SIZE, MSB_SIZE);
            long lsbAltitude = Bits.extractUInt(message, LSB_START_INDEX, LSB_SIZE);

            if (lsbAltitude == INVALID_LSB_ALT_ONE
                    || lsbAltitude == INVALID_LSB_ALT_TWO
                    || lsbAltitude == INVALID_LSB_ALT_THREE)  return INVALID_ALT;

            if (lsbAltitude == 7) lsbAltitude = 5;

            if ((msbAltitude % 2) != 0) lsbAltitude = 6 - lsbAltitude;

            return Units.convertFrom(
                    CORRECTION_ALT_ZERO
                            + lsbAltitude * LSB_COEFF_ALT_ZERO
                            + msbAltitude * MSB_COEFF_ALT_ZERO,
                    FOOT);
        }
    }

    /**
     * Disentangles the bits of the encoding of the altitude value
     *
     * @param altitudeContent (long): Bits to disentangle
     * @return (long): Disentangled bits
     */
    private static long disentangling(long altitudeContent) {
        long decodedMessage = 0;

        for (int j = 0; j < NUMBER_GRAY_GROUP; j++) {
            for (int i = 0; i < GRAY_GROUP_SIZE; i++) {
                decodedMessage = (decodedMessage << 1) |
                        (Bits.testBit(altitudeContent, 2 * (2 - i) + INDEX_TABLE[j]) ? 1 : 0);
            }
        }

        long msbContentGray = decodeGrayCode(
                Bits.extractUInt(decodedMessage, LSB_SIZE, MSB_SIZE),MSB_SIZE);
        long lsbContentGray = decodeGrayCode(
                Bits.extractUInt(decodedMessage, LSB_START_INDEX, LSB_SIZE), LSB_SIZE);

        return msbContentGray << LSB_SIZE | lsbContentGray;
    }

    /**
     * Determines the binary value corresponding to the given Gray value
     *
     * @param codedContent  (long): Given Gray value
     * @param messageLength (int): Length of the value
     * @return (long): Binary representation
     */
    private static long decodeGrayCode(long codedContent, int messageLength) {
        long decodedMessage = 0;

        for (int i = 0; i < messageLength; i++) decodedMessage ^= codedContent >> i;

        return decodedMessage;
    }
}