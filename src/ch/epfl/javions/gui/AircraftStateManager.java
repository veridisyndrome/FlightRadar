package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Public: Defines a manager for the aircraft states
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AircraftStateManager {
    private static final long LIMIT_TIME_STAMP = Duration.ofMinutes(1).toNanos();
    private final AircraftDatabase aircraftDatabase;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> icaoAddressMap;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObservableSet<ObservableAircraftState> unmodifiableAircraftStates;
    private long lastTimeStampNs;

    /**
     * Default AircraftStateManager constructor.
     * Keeps the states up-to-date of a set of aircraft based on messages received from them
     *
     * @param aircraftDatabase (AircraftDatabase): Database containing the fixed characteristics of aircraft
     * @throws NullPointerException If aircraftDatabase is null
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
        this.icaoAddressMap = new HashMap<>();
        this.aircraftStates = FXCollections.observableSet();
        this.unmodifiableAircraftStates = FXCollections.unmodifiableObservableSet(aircraftStates);
    }

    /**
     * Returns the unmodifiable observable set of observable states of aircraft whose position is known
     *
     * @return (ObservableSet<ObservableAircraftState>): The unmodifiable observable set of states
     */
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableAircraftStates;
    }

    /**
     * Updates the aircraft's state basing on the given message
     *
     * @param message (Message): Given message
     * @throws IOException If there is an Input/Output error
     */
    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress icaoAddress = message.icaoAddress();
        AircraftStateAccumulator<ObservableAircraftState> stateAccumulator
                = icaoAddressMap.get(icaoAddress);

        if (stateAccumulator == null) {
            stateAccumulator = new AircraftStateAccumulator<>(
                            new ObservableAircraftState(icaoAddress, aircraftDatabase.get(icaoAddress)));

            icaoAddressMap.put(message.icaoAddress(), stateAccumulator);
        }

        stateAccumulator.update(message);

        ObservableAircraftState stateSetter = stateAccumulator.stateSetter();

        if (stateSetter.getPosition() != null) {
            aircraftStates.add(stateSetter);
        }

        lastTimeStampNs = message.timeStampNs();
    }

    /**
     * Removes from the set of observable states all those corresponding to aircraft for which no
     * message has been received in the minute preceding the reception of the last message passed
     * to updateWithMessage
     * */
    public void purge() {
        Iterator<AircraftStateAccumulator<ObservableAircraftState>> stateAccumulatorIterator
                = icaoAddressMap.values().iterator();
        while (stateAccumulatorIterator.hasNext()) {
            AircraftStateAccumulator<ObservableAircraftState> nextStateAccumulator
                    = stateAccumulatorIterator.next();
            ObservableAircraftState stateSetter = nextStateAccumulator.stateSetter();

            long lastTs = stateSetter.getLastMessageTimeStampNs();

            if (lastTs < lastTimeStampNs - LIMIT_TIME_STAMP) {
                stateAccumulatorIterator.remove();
                aircraftStates.remove(stateSetter);
            }
        }
    }
}

