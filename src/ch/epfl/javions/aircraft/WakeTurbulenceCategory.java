package ch.epfl.javions.aircraft;

/**
 * Public: Enumeration of the wake turbulences
 *
 * @author Pablo Robin Guerrero (356671)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * Gives the wake turbulence category corresponding to the given letter
     *
     * @param s (String): Given letter
     * @return (WakeTurbulenceCategory): Corresponding wake turbulence category
     */
    public static WakeTurbulenceCategory of(String s) {
        switch(s) {
            case "L" -> {return LIGHT;}
            case "M" -> {return MEDIUM;}
            case "H" -> {return HEAVY;}
            default -> {return UNKNOWN;}
        }
    }
}
