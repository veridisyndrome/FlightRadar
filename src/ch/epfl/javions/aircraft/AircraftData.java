package ch.epfl.javions.aircraft;

import static java.util.Objects.requireNonNull;

/**
 * Public: Collects the fixed data of an aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AircraftData(AircraftRegistration registration,
                           AircraftTypeDesignator typeDesignator,
                           String model,
                           AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * Compact constructor of AircraftData
     *
     * @param registration           (AircraftRegistration): Aircraft's registration
     * @param typeDesignator         (AircraftTypeDesignator): Aircraft's type designator
     * @param model                  (String): Aircraft's model
     * @param description            (AircraftDescription): Aircraft's description
     * @param wakeTurbulenceCategory (WakeTurbulenceCategory): Aircraft's wake turbulence category
     * @throws NullPointerException If one of its arguments is null
     */
    public AircraftData {
        requireNonNull(registration);
        requireNonNull(typeDesignator);
        requireNonNull(model);
        requireNonNull(description);
        requireNonNull(wakeTurbulenceCategory);
    }
}
