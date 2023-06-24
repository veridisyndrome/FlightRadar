package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Public: Represents the call sign of an aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record CallSign(String string) {
    private static final Pattern CALL_SIGN = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Compact constructor of CallSign.
     * Determines whether the given string is valid or not
     *
     * @param string (String): String to which to determine validity
     * @throws IllegalArgumentException If the call sign doesn't match the pattern
     */
    public CallSign {
        Preconditions.checkArgument(CALL_SIGN.matcher(string).matches());
    }
}

