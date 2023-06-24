package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Preconditions.checkArgument;
import static ch.epfl.javions.Units.Angle.*;
import static ch.epfl.javions.Units.Speed.KNOT;

/**
 * Public: Represents an airborne velocity ADS-B message
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AirborneVelocityMessage(
        long timeStampNs,
        IcaoAddress icaoAddress,
        double speed,
        double trackOrHeading) implements Message{
    private static final double INVALID_SPEED = Double.NaN, INVALID_TOR = Double.NaN;
    private static final int AS_TO_KNOT = 4;
    private static final int INVALID_SPEED_VALUE = -1;
    private static final int ST_START_INDEX = 48, ST_SIZE = 3;
    private static final int SPEEDCONTENT_START_INDEX = 21, SPEED_TOR_CONTENT_SIZE = 22;
    private static final int TORCONTENT_START_INDEX = 21;
    private static final int ST_VALUE_ONE = 1, ST_VALUE_TWO = 2, ST_VALUE_THREE = 3, ST_VALUE_FOUR = 4;
    private static final int VEW_VNS_SIZE = 10, VEW_START_INDEX = VEW_VNS_SIZE + 1, VNS_START_INDEX = 0;
    private static final int DEW_DNS_SIZE = 1, DEW_START_INDEX = VEW_START_INDEX + VEW_VNS_SIZE,
            DNS_START_INDEX = VEW_VNS_SIZE;
    private static final int AS_START_INDEX = 0, AS_HDG_SIZE = 10, HDG_START_INDEX = AS_HDG_SIZE + 1;
    private static final int SH_INDEX = HDG_START_INDEX + AS_HDG_SIZE;
    private static final double[] DIRECTION_COMPOSANTE = {1, -1};

    /**
     * Compact AirborneVelocityMessage constructor
     *
     * @param timeStampNs    (long): Timestamp of the message expressed in nanoseconds
     * @param icaoAddress    (IcaoAddress): ICAO address of the sender
     * @param speed          (double): Speed of the aircraft
     * @param trackOrHeading (double): Direction of the movement of the aircraft
     * @throws NullPointerException If the icaoAddress is null
     * @throws IllegalArgumentException If timeStampNs, speed or trackOrHeading
     * are strictly negative
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        checkArgument(0 <= timeStampNs && 0 <= speed && 0 <= trackOrHeading) ;
    }

    /**
     * Determines the velocity message corresponding to the raw message
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (AircraftPositionMessage): Corresponding velocity message
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        double speed = speed(rawMessage);
        double trackOrHeading = trackOrHeading(rawMessage);

        return Double.isNaN(speed) || Double.isNaN(trackOrHeading) ? null :
                new AirborneVelocityMessage(
                        rawMessage.timeStampNs(),
                        rawMessage.icaoAddress(),
                        speed,
                        trackOrHeading);
    }

    /**
     * Calculates the speed of the aircraft
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (double): Corresponding speed
     */
    private static double speed(RawMessage rawMessage) {
        int st = Bits.extractUInt(rawMessage.payload(), ST_START_INDEX , ST_SIZE);
        long speedContent = Bits.extractUInt(
                rawMessage.payload(),
                SPEEDCONTENT_START_INDEX,
                SPEED_TOR_CONTENT_SIZE);

        switch (st) {
            case ST_VALUE_ONE, ST_VALUE_TWO -> {
                int vew = Bits.extractUInt(speedContent, VEW_START_INDEX, VEW_VNS_SIZE) - 1;
                int vns = Bits.extractUInt(speedContent, VNS_START_INDEX, VEW_VNS_SIZE) - 1;

                if (vew <= INVALID_SPEED_VALUE || vns <= INVALID_SPEED_VALUE) return INVALID_SPEED;

                double speed = Math.hypot(vns, vew);

                return speedValue(speed, st);
            }
            case ST_VALUE_THREE, ST_VALUE_FOUR -> {
                double as = Bits.extractUInt(speedContent, AS_START_INDEX, AS_HDG_SIZE) - 1;

                if (as <= INVALID_SPEED_VALUE) return INVALID_SPEED;

                return speedValue(as, st);
            }
            case default -> {
                return INVALID_SPEED;
            }
        }
    }

    /**
     * Determines the speed value encoded in the given attributes
     *
     * @param speed (double): Speed attribute
     * @param st    (int): ST attribute
     * @return (double): The speed value
     */
    private static double speedValue(double speed, int st) {
        return st == ST_VALUE_ONE || st == ST_VALUE_THREE ?
                Units.convertFrom(speed, KNOT) :
                Units.convertFrom(speed, KNOT) * AS_TO_KNOT;
    }

    /**
     * Calculates the direction of the aircraft
     *
     * @param rawMessage (RawMessage): Given raw message
     * @return (double): Corresponding direction
     */

    private static double trackOrHeading(RawMessage rawMessage) {
        int st = Bits.extractUInt(rawMessage.payload(), ST_START_INDEX , ST_SIZE);
        long torContent = Bits.extractUInt(
                rawMessage.payload(),
                TORCONTENT_START_INDEX,
                SPEED_TOR_CONTENT_SIZE);

        switch (st) {
            case ST_VALUE_ONE, ST_VALUE_TWO -> {
                int dew = Bits.extractUInt(torContent, DEW_START_INDEX, DEW_DNS_SIZE);
                int vew = (Bits.extractUInt(torContent, VEW_START_INDEX, VEW_VNS_SIZE) - 1);
                int dns = Bits.extractUInt(torContent, DNS_START_INDEX, DEW_DNS_SIZE);
                int vns = (Bits.extractUInt(torContent, VNS_START_INDEX, VEW_VNS_SIZE) - 1);

                double angle = Math.atan2(
                        vew * DIRECTION_COMPOSANTE[dew],
                        vns * DIRECTION_COMPOSANTE[dns]);

                return angle < 0 ? angle + TURN : angle;
            }

            case ST_VALUE_THREE, ST_VALUE_FOUR -> {
                double hdg = Bits.extractUInt(torContent, HDG_START_INDEX, AS_HDG_SIZE);

                return Bits.testBit(torContent, SH_INDEX)
                        ? Units.convertFrom(hdg / (1 << 10), TURN)
                        : INVALID_TOR;
            }

            case default -> {
                return INVALID_TOR;
            }
        }
    }
}
