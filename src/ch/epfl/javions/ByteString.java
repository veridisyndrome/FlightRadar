package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public and Final: Represents a sequence of butes
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class ByteString {
    /** Byte made with 1 bit only*/
    private static final long MASK = 0xff;
    private static final HexFormat HF = HexFormat.of().withUpperCase();
    private final byte[] bytes;


    /**
     * Default ByteString constructor.
     * Saves a copy on the byte array
     *
     * @param bytes (byte[]): Byte array to clone
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /** @return (int): Number of bytes in the byte array*/
    public int size() {
        return bytes.length;
    }

    /**
     * Returns the unsigned byte located at the desired index
     *
     * @param index (int): Index of the byte to extract
     * @return (int): Unsigned byte
     * @throws IndexOutOfBoundsException If the index is not valid
     */
    public int byteAt(int index) {
        return (int) (Byte.toUnsignedInt(bytes[index]) & MASK);
    }

    /**
     * Returns the bytes located between the fromIndex (included) and toIndex (excluded)
     *
     * @param fromIndex (int): Starting index
     * @param toIndex   (int): Destination index
     * @return (long): Bytes located in the given range
     * @throws IndexOutOfBoundsException If the range described by fromIndex and toIndex is not
     * completely between 0 and the byte array length.
     * @throws IllegalArgumentException If the difference between toIndex and fromIndex
     * is not strictly lower than the number of bytes inside a value of type long
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        checkArgument(1 <=
                (toIndex - Objects.checkFromToIndex(fromIndex, toIndex, bytes.length)));

        long inRange = 0;
        int length = toIndex - fromIndex - 1;
        for (int i = fromIndex; i < toIndex; i++) {
            inRange += (long) byteAt(i) << (length * Byte.SIZE);
            --length;
        }
        return inRange;
    }

    /**
     * Gives a ByteString representation of the given string
     *
     * @param hexString (String): Hexadecimal representation of the given string
     * @return (ByteString): ByteString representation of the given String
     * @throws IllegalArgumentException If the given string's length is not even
     * or if it contains a non-hexadecimal number
     */
    public static ByteString ofHexadecimalString(String hexString) {
        return new ByteString(HF.parseHex(hexString));
    }

    @Override
    public boolean equals(Object that0) {
        return that0 instanceof ByteString that && Arrays.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}