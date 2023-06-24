package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents the aircraft registration of the aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AircraftRegistration(String string) {
    private static final Pattern REGISTRATION = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Compact constructor of AircraftRegistration.
     * Determines whether the given string is valid or not
     *
     * @param string (String): String to which to determine validity
     * @throws IllegalArgumentException If the registration doesn't match the pattern
     */
    public AircraftRegistration {
        checkArgument(REGISTRATION.matcher(string).matches());
    }
}
