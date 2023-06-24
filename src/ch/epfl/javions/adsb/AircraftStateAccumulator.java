package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.time.Duration;

import static ch.epfl.javions.adsb.CprDecoder.decodePosition;
import static java.util.Objects.requireNonNull;

/**
 * Public: Represents an aircraft state accumulator, i.e. an object accumulating ADS-B
 * messages coming from a single aircraft to determine its state over time
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AircraftStateAccumulator <T extends AircraftStateSetter> {
    private static final long MAX_TIME = Duration.ofSeconds(10).toNanos();
    private static final int ODD_PARITY = 0, EVEN_PARITY = 1;
    private static final int[] PARITY_TABLE = new int[] {1, 0};
    private final AirbornePositionMessage[] messages = new AirbornePositionMessage[2];
    private final T state;

    /**
     * Default AircraftStateAccumulator constructor.
     * Defines an aircraft state accumulator associated with the given modifiable state
     *
     * @param stateSetter (T): State of the message
     * @throws NullPointerException If the given state is null
     */
    public AircraftStateAccumulator(T stateSetter) {
        requireNonNull(stateSetter);
        state = stateSetter;
    }

    /**
     * Determines the state of the aircraft
     *
     * @return (T): State of the aircraft
     */
    public T stateSetter() {
        return state;
    }

    /**
     * Updates the state basing on the given message
     *
     * @param message (Message): given message
     */
    public void update(Message message) {
        state.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                state.setCategory(aim.category());
                state.setCallSign(aim.callSign());
            }

            case AirbornePositionMessage apm -> {
                state.setAltitude(apm.altitude());

                messages[apm.parity()] = apm;

                if (messages[PARITY_TABLE[apm.parity()]] != null) {
                    GeoPos pos = decodePosition(
                            messages[ODD_PARITY].x(), messages[ODD_PARITY].y(),
                            messages[EVEN_PARITY].x(), messages[EVEN_PARITY].y(),
                            apm.parity());
                    if (pos != null && checkValidTimeStamps(apm)) state.setPosition(pos);
                }
            }

            case AirborneVelocityMessage avm -> {
                state.setVelocity(avm.speed());
                state.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> throw new Error();
        }
    }

    /**
     * Determines if the position can be calculated
     *
     * @param apm  (AirPositionMessage): Given message
     * @return (boolean): True if the position can be calculated
     */
    private boolean checkValidTimeStamps(AirbornePositionMessage apm) {
        return apm.timeStampNs() - messages[PARITY_TABLE[apm.parity()]].timeStampNs() <= MAX_TIME;
    }
}
