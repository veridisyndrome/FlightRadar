package ch.epfl.javions;

/**
 * Public and Final: Checks that the given condition is respected
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Preconditions {
    /**
     * Default Preconditions constructor.
     * Defined as private to prevent instantiations of the class
     */
    private Preconditions() {}

    /**
     * Determines if the given argument is valid
     *
     * @param shouldBeTrue (boolean): Condition to verify
     * @throws IllegalArgumentException If the argument is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}