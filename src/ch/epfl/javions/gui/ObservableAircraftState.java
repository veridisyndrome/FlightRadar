package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * Public: Represents the state of an aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNsProperty;
    private long lastTimeStampNs;
    private final IntegerProperty categoryProperty;
    private final ObjectProperty<CallSign> callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final ObservableList<AirbornePos> trajectoryList;
    private final ObservableList<AirbornePos> trajectoryUnmodifiableList;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;

    /**
     * Defines the points representing the trajectory of the aircraft
     *
     * @param position (GeoPos): Position at Earth's surface
     * @param altitude (double): Altitude of the aircraft
     */
    public record AirbornePos(GeoPos position, double altitude) {}

    /**
     * Default ObservableAircraftState constructor.
     * Defines an observable state for the aircraft
     *
     * @param icaoAddress (IcaoAddress): ICAO Address of the aircraft
     * @param aircraftData (AircraftData): Fixed characteristics of the aircraft
     * @throws NullPointerException If icaoAddress is null
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = Objects.requireNonNull(icaoAddress);
        this.aircraftData = aircraftData;

        lastMessageTimeStampNsProperty = new SimpleLongProperty();
        lastTimeStampNs = -1;
        categoryProperty = new SimpleIntegerProperty();
        callSignProperty = new SimpleObjectProperty<>();
        positionProperty = new SimpleObjectProperty<>();
        altitudeProperty = new SimpleDoubleProperty(Double.NaN);
        velocityProperty = new SimpleDoubleProperty(Double.NaN);
        trackOrHeadingProperty = new SimpleDoubleProperty();

        trajectoryList = FXCollections.observableArrayList();
        trajectoryUnmodifiableList = FXCollections.unmodifiableObservableList(trajectoryList);
    }

    /**
     * Returns the ICAO Address
     *
     * @return (IcaoAddress): ICAO Address
     */
    public IcaoAddress getIcaoAddress() { return icaoAddress; }

    /**
     * Returns the Aircraft's data
     *
     * @return (AircraftData): Aircraft's data
     */
    public AircraftData getAircraftData() { return aircraftData; }

    /**
     * Returns the Aircraft's type designator
     *
     * @return (AircraftTypeDesignator): Aircraft's type designator
     */
    public AircraftTypeDesignator getAircraftTypeDesignator() {
        return aircraftData.typeDesignator();
    }

    /**
     * Returns the Aircraft's description
     *
     * @return (AircraftDescription): Aircraft's description
     */
    public AircraftDescription getAircraftDescription() {
        return aircraftData.description();
    }

    /**
     * Returns the Aircraft's wake turbulence category
     *
     * @return (WakeTurbulenceCategory): Aircraft's wake turbulence category
     */
    public WakeTurbulenceCategory getWakeTurbulenceCategory() {
        return aircraftData.wakeTurbulenceCategory();
    }

    /**
     * Returns the Aircraft's timestamp of the last message
     *
     * @return (long): The last message's timestamp
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNsProperty.get();
    }

    /**
     * Set the value of the Aircraft's time stamp property
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNsProperty.set(timeStampNs);
    }

    /**
     * Returns the property of the Aircraft's category
     *
     * @return (ReadOnlyIntegerProperty): Aircraft's category property
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return categoryProperty;
    }

    /**
     * Returns the Aircraft's category
     *
     * @return (int): Aircraft's category
     */
    public int getCategory() {
        return categoryProperty.get();
    }

    /**
     * Set the value of the Aircraft's category property
     */
    @Override
    public void setCategory(int category) {
        categoryProperty.set(category);
    }

    /**
     * Returns the property of the Aircraft's call sign
     *
     * @return (ReadOnlyObjectProperty): Aircraft's call sign property
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSignProperty;
    }

    /**
     * Set the value of the Aircraft's call sign property
     */
    @Override
    public void setCallSign(CallSign callSign) {
        callSignProperty.set(callSign);
    }

    /**
     * Returns the property of the Aircraft's geographical position
     *
     * @return (ReadOnlyObjectProperty): Aircraft's geographical property
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return positionProperty;
    }

    /**
     * Returns the Aircraft's geographical position
     *
     * @return (GeoPos): Aircraft's geographical position
     */
    public GeoPos getPosition() {
        return positionProperty.get();
    }

    /**
     * Set the value of Aircraft's geographical position property and update the trajectory property
     */
    @Override
    public void setPosition(GeoPos geoPos) {
        positionProperty.set(geoPos);
        updateTrajectory(geoPos, getAltitude());
    }

    /**
     * Returns the unmodifiable list of the trajectory
     *
     * @return (ObservableList<AirbornePos>): Trajectory's list
     */
    public ObservableList<AirbornePos> trajectoryList() {
        return trajectoryUnmodifiableList;
    }

    /**
     * Returns the property of the Aircraft's altitude
     *
     * @return (ReadOnlyDoubleProperty): Altitude's property
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitudeProperty;
    }

    /**
     * Returns the Aircraft's altitude
     *
     * @return (double): Aircraft's altitude
     */
    public double getAltitude() {
        return altitudeProperty.get();
    }

    /**
     * Set the value of the Aircraft's altitude property and update the trajectory property
     */
    @Override
    public void setAltitude(double altitude) {
        altitudeProperty.set(altitude);
        updateTrajectory(getPosition(), altitude);
    }

    /**
     * Returns the property of the Aircraft's velocity
     *
     * @return (ReadOnlyDoubleProperty): Velocity's property
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocityProperty;
    }

    /**
     * Returns the Aircraft's velocity
     *
     * @return (double): Aircraft's velocity
     */
    public double getVelocity() {
        return velocityProperty.get();
    }

    /**
     * Set the value of the Aircraft's velocity property
     */
    @Override
    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }

    /**
     * Returns the property of the Aircraft's track or heading
     *
     * @return (ReadOnlyDoubleProperty): Track Or Heading's property
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeadingProperty;
    }

    /**
     * Returns the Aircraft's track or heading
     *
     * @return (double): Aircraft's track or heading
     */
    public double getTrackOrHeading() {
        return trackOrHeadingProperty.get();
    }

    /**
     * Set the value of the Aircraft's track or heading property
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        trackOrHeadingProperty.set(trackOrHeading);
    }

    /**
     * Updates the trajectory with the given points
     *
     * @param position (GeoPos): Position at Earth's surface
     * @param altitude (double): Altitude of the aircraft
     */
    private void updateTrajectory(GeoPos position, double altitude) {
        if (position != null && !Double.isNaN(altitude)) {
            int lastTrajectoryIndex = trajectoryList.size() - 1;

            if (trajectoryList.isEmpty()) {
                trajectoryList.add(new AirbornePos(getPosition(), getAltitude()));
            } else if (getPosition().equals(trajectoryList.get(lastTrajectoryIndex).position)) {
                trajectoryList.add(new AirbornePos(getPosition(), getAltitude()));
            } else if (getLastMessageTimeStampNs() == lastTimeStampNs) {
                trajectoryList.set(lastTrajectoryIndex, new AirbornePos(position, altitude));
            }

            lastTimeStampNs = getLastMessageTimeStampNs();
        }
    }
}
