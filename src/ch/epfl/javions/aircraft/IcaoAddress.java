package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents an ICAO address of an aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record IcaoAddress(String string) {
    private static final Pattern ICAO = Pattern.compile("[0-9A-F]{6}");

    /**
     * Compact constructor of IcaoAddress.
     * Determines whether the given string is valid or not
     *
     * @param string (String): String to which to determine validity
     * @throws IllegalArgumentException If the ICAO address doesn't match the pattern
     */
    public IcaoAddress {
        checkArgument(ICAO.matcher(string).matches());
    }
}