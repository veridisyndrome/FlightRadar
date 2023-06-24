package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents the aircraft description
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record AircraftDescription(String string) {
    private static final Pattern DESCRIPTION = Pattern.compile(
            "[ABDGHLPRSTV-][0123468][EJPT-]");
    /**
     * Compact constructor of AircraftDescription.
     * Determines whether the given string is valid or not
     *
     * @param string (String): String to which to determine validity
     * @throws IllegalArgumentException If the description doesn't match the pattern and it's not empty
     */
    public AircraftDescription {
        checkArgument(DESCRIPTION.matcher(string).matches() || string.isEmpty());
    }
}
