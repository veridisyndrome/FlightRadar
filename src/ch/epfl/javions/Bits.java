package ch.epfl.javions;

import java.util.Objects;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Defines methods allowing to extract a subset of the 64 bits of a long value
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Bits {
    /**
     * Default Bits constructor.
     * Defined as private to prevent instantiations of the class
     */
    private Bits() {}

    /**
     * Extracts from a given value the range of given size bits starting at the given index
     *
     * @param value (long): Value from which the 64-bits subpart is extracted
     * @param start (int): Index from where the extraction begins
     * @param size  (int): Size of the extracted 64-bits subpart
     * @return (int): The extracted 64-bits subpart
     * @throws IllegalArgumentException If the size is not strictly greater than
     * 0 and strictly less than 32
     * @throws IndexOutOfBoundsException If the range described by start and size is not completely
     * between 0 (included) and 64 (excluded)
     */
    public static int extractUInt(long value, int start, int size) {
        checkArgument(0 < size && size < Integer.SIZE);

        int range = Objects.checkFromIndexSize(start, size, Long.SIZE) + size;
        
        return (int) ((value << (Long.SIZE - range)) >>> (Long.SIZE - size));
    }

    /**
     * Determines if the bit at the given index is a 1
     *
     * @param value (long): Given 64-bits vector
     * @param index (int): Index to verify
     * @return (boolean): True if the bit is a 1
     * @throws IndexOutOfBoundsException If the index is not between 0 (included) and 64 (excluded)
     */
    public static boolean testBit(long value, int index) {
        return (1 & (value >> Objects.checkIndex(index, Long.SIZE))) == 1;
    }
}
