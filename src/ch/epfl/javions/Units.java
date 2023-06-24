package ch.epfl.javions;

/**
 * Public and Final: A database of the main units used in this project
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Units {
    /**
     * Default Units constructor.
     * Defined as private to prevent instantiations of the class
     */
    private Units() {}
    /** Prefix representing one-hundredth of a given unit*/
    public static final double CENTI = 1e-2;

    /** Prefix representing one-thousandth of a given unit*/
    public static final double KILO = 1e3;

    /** Defines all the units used for angles*/
    public static class Angle {
        /** Unit representing radians*/
        public static final double RADIAN = 1;

        /** Unit representing turns*/
        public static final double TURN = 2 * Math.PI * RADIAN;

        /** Unit representing degrees*/
        public static final double DEGREE = TURN / 360;

        /** Unit representing "T32", a non-standard unit practical for
         * representing longitudes et latitudes with signed integers
         */
        public static final double T32 = TURN / Math.scalb(1, 32);
    }

    /** Defines all the units used for lengths*/
    public static final class Length {
        /** Unit representing meters*/
        public static final double METER = 1;

        /** Unit representing centimeters*/
        public static final double CENTIMETER = CENTI * METER;

        /** Unit representing kilometers*/
        public static final double KILOMETER = KILO * METER;

        /** Unit representing inches*/
        public static final double INCH = 2.54 * CENTIMETER;

        /** Unit representing feet*/
        public static final double FOOT = 12 * INCH;

        /** Unit representing nautical miles*/
        public static final double NAUTICAL_MILE = 1852 * METER;
    }

    /** Defines all the units used for time*/
    public static final class Time {
        /** Unit representing seconds*/
        public static final double SECOND = 1;

        /** Unit representing minutes*/
        public static final double MINUTE = 60 * SECOND;

        /** Unit representing hours*/
        public static final double HOUR = 60 * MINUTE;
    }

    /** Defines all the units used for speed*/
    public static final class Speed {
        /** Unit representing knots*/
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;

        /** Unit representing kilometers per hour*/
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
    }

    /**
     * Converts a value expressed in a given unit to the desired unit
     *
     * @param value    (double): Value to convert
     * @param fromUnit (double): Starting unit
     * @param toUnit   (double): Desired unit
     * @return (double): Conversion of the value
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * Converts a value expressed in a given unit to the base unit
     *
     * @param value    (double): Value to convert
     * @param fromUnit (double): Starting unit
     * @return (double): Conversion of the value
     */
    public static double convertFrom(double value, double fromUnit) {
        return convert(value, fromUnit, 1);
    }

    /**
     * Converts a value express in the base unit to the desired unit
     *
     * @param value  (double): Value to convert
     * @param toUnit (double): Desired unit
     * @return (double): Conversion of the value
     */
    public static double convertTo(double value, double toUnit) {
        return convert(value, 1, toUnit);
    }
}