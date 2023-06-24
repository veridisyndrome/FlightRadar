package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents the type indicator of the aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AircraftTypeDesignator(String string) {
    private static final Pattern TYPE_DESIGNATOR = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * Compact constructor of AircraftTypeDesignator.
     * Determines whether the given string is valid or not
     *
     * @param string (String): String to which to determine validity
     * @throws IllegalArgumentException If the type designator doesn't match the pattern and it's not empty
     */
    public AircraftTypeDesignator {
        checkArgument(TYPE_DESIGNATOR.matcher(string).matches() || string.isEmpty());
    }
}
