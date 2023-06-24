package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Public: Represents a color gradient
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class ColorRamp {
    private static final int MIN_COLOR = 2;

    /** Plasma gradient defined by Nathaniel J. Smith and Stefan van der Walt*/
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private final List<Color> colors;

    /**
     * Default ColorRamp constructor.
     * Defines the color gradient
     *
     * @param colors (Color...): Given a sequence of colors
     * @throws IllegalArgumentException If the length of the list is not greater or equal than 2
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(MIN_COLOR <= colors.length);
        this.colors = Arrays.stream(colors.clone()).toList();
    }

    /**
     * Gives the color corresponding to the given double
     *
     * @param c (double): Given double
     * @return (Color): Corresponding color
     */
    public Color at(double c) {
        //Negative or zero altitude
        if (c <= 0) return colors.get(0);

        //Altitude higher or equal than the maximum altitude
        if (1 <= c) return colors.get(colors.size() - 1);

        double colorIndex = c * (colors.size() - 1);

        //Calculating if we have to mix two colors
        int colorIndexFloor = (int) Math.floor(colorIndex);
        double distance = colorIndex - colorIndexFloor;

        if (distance == 0) return colors.get(colorIndexFloor);

        //Mixing the two color between the color index
        Color c1 = colors.get(colorIndexFloor);
        Color c2 = colors.get(colorIndexFloor + 1);

        //Return the desired color
        return c1.interpolate(c2, distance);
    }
}
