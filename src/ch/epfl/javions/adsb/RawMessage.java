package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public: Represents a raw ADS-B message, i.e. a message of
 * which the ME attribute has not been analyzed yet
 *
 * @author Pablo Robin Guerrero (356671)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    /** Integer representing the length of a raw message*/
    public static final int LENGTH = 14;
    private static final int DF_VALID_VALUE = 17, DF_CA_START_INDEX = 0;
    private static final int ME_MSB_START_POSITION = 51, ME_MSB_SIZE = 5;
    private static final int ME_END_INDEX = 10;
    private static final int CA_SIZE = 3, DF_SIZE = 5;
    private static final int ICAO_ADDRESS_START_INDEX = 1,
            ICAO_ADDRESS_END_INDEX = 3, ICAO_ADDRESS_LENGTH = 3;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final Crc24 CRC24 = new Crc24(Crc24.GENERATOR);

    /**
     * Compact constructor of RawMessage
     *
     * @param timeStampNs (long): Timestamp of the message expressed in nanoseconds
     * @param bytes       (ByteString): Bytes of the message
     * @throws IllegalArgumentException If the timestamp is negative or if the
     * string of bytes isn't LENGTH long.
     */
    public RawMessage {
        checkArgument(0 <= timeStampNs);
        checkArgument(bytes.size() == LENGTH);
    }

    /**
     * Determines the raw ADS-B message with the given timestamp and bytes
     *
     * @param timeStampNs (long): Given timestamp
     * @param bytes       (byte[]): Given bytes
     * @return (RawMessage): The message or null if the bytes' CRC24 isn't equal to 0
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        return CRC24.crc(bytes) == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    /**
     * Determines the size of the message which the first byte is the given one
     *
     * @param byte0 (byte): Given first byte
     * @return (int): Message's size, equal to 14 if the DF attribute in the byte is equal to 17
     */
    public static int size(byte byte0) {
        return (Byte.toUnsignedInt(byte0) >>> CA_SIZE) == DF_VALID_VALUE ? LENGTH : 0;
    }

    /**
     * Determines the type code of the given ME attribute
     *
     * @param payload (long): Given ME attribute
     * @return (int): Corresponding type code
     */
    public static int typeCode(long payload) {
        return extractUInt(payload, ME_MSB_START_POSITION, ME_MSB_SIZE);
    }

    /**
     * Determines the DF attribute stored in the first byte
     *
     * @return (int): DF attribute
     */
    public int downLinkFormat() {
        return extractUInt(bytes.byteAt(DF_CA_START_INDEX), CA_SIZE, DF_SIZE);
    }

    /** Determines the ICAO Address of the sender
     *
     * @return (IcaoAddress): The ICAO Address
     */
    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HEX_FORMAT
                .toHexDigits(bytes.bytesInRange(ICAO_ADDRESS_START_INDEX, ICAO_ADDRESS_END_INDEX + 1),
                        ICAO_ADDRESS_LENGTH * 2));
    }

    /**
     * Determines the ME attribute of the message
     *
     * @return (long): ME attribute
     */
    public long payload() {
        return bytes.bytesInRange(ICAO_ADDRESS_END_INDEX + 1, ME_END_INDEX + 1);
    }

    /**
     * Determines the type code of the message
     *
     * @return (int): Type code
     */
    public int typeCode() {
        return typeCode(payload());
    }
}
