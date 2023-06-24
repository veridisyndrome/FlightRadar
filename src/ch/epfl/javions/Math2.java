package ch.epfl.javions;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public and Final: Defines methods to simplify some mathematical operations
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Math2 {
    /**
     * Default Math2 constructor.
     * Defined as private to prevent instantiations of the class
     */
    private Math2() {}

    /**
     * Limits a given value to the range from min to max
     *
     * @param min (int): Lower bound of the range
     * @param v   (int): Value to limit
     * @param max (int): Upper bound of the range
     * @return (int): Limited value
     * @throws IllegalArgumentException if min is strictly greater to max
     */
    public static int clamp(int min, int v, int max) {
        checkArgument(min <= max);

        return Math.min(Math.max(v, min), max);
    }

    /**
     * Defines the reciprocal hyperbolic sine
     *
     * @param x (double): Input value
     * @return (double): Reciprocal hyperbolic sine of the input value
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }
}