package ch.epfl.javions.demodulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public and Final: Represents a fixed-size window over a sequence
 * of power samples produced by a power calculator
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class PowerWindow {
    private static final int MAX_SIZE =  1 << 16;
    private final PowerComputer powerComputer;
    private int actualPosition = 0, absPosition = 0;
    private int[] first, second;
    private final int windowSize;
    private int samples;

    /**
     * Default PowerWindow constructor.
     * Gives a window of given size over the sequence of power samples
     *
     * @param stream       (InputStream): Given stream
     * @param windowSize   (int): Size of the window
     * @throws IOException If there is an Input/Output error
     * @throws IllegalArgumentException If the window size is not between
     * 0 (excluded) and 2^16 (included)
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        checkArgument(0 < windowSize && windowSize <= MAX_SIZE);

        this.windowSize = windowSize;
        this.powerComputer = new PowerComputer(stream, MAX_SIZE);

        first = new int[MAX_SIZE];
        second = new int[MAX_SIZE];

        samples = powerComputer.readBatch(first);
    }

    /**
     * Determines the window's size
     *
     * @return (int): Window's size
     */
    public int size() {return windowSize;}

    /**
     * Determines the current window's position
     *
     * @return (long): Current window's position
     */
    public long position() {
        return absPosition;
    }

    /**
     * Determines if the window is full
     *
     * @return (boolean): True if the window is full
     */
    public boolean isFull() {
        return windowSize <= samples;
    }

    /**
     * Gives the power sample at the window's given index
     *
     * @param i (int): Desired index
     * @return (int): Sample corresponding to the index position
     * @throws IndexOutOfBoundsException If the given index is not between
     * 0 (included) and the size of the window (excluded)
     */
    public int get(int i) {
        int j = actualPosition + Objects.checkIndex(i, windowSize);

        if (j < MAX_SIZE) {
            return first[j];
        } else {
            return second[j - MAX_SIZE];
        }
    }

    /**
     * Advances the window by one sample
     *
     * @throws IOException If there is an Input/Output error
     */
    public void advance() throws IOException {
        ++actualPosition;
        ++absPosition;
        --samples;
        if (actualPosition + windowSize == (MAX_SIZE + 1)) {
            samples += powerComputer.readBatch(second);
        } else if (actualPosition == MAX_SIZE) {
            int[] temp = first;
            first = second;
            second = temp;
            actualPosition = 0;
        }
    }

    /**
     * Advances the window by the desired number of samples
     *
     * @param offset (int): Desired offset
     * @throws IOException If there is an Input/Output error
     * @throws IllegalArgumentException If the given number is not positive or equal to 0
     */
    public void advanceBy(int offset) throws IOException {
        checkArgument(0 <= offset);

        for (int i = 0; i < offset; i++) advance();
    }
}
