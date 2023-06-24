package ch.epfl.javions;

/**
 * Public and Final: Represents a CRC of 24 bits calculator
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Crc24 {
    /**
     * Integer containing the 24 LSBs of the generator,
     * of which the MSB is equal to 1
     */
    public static final int GENERATOR = 0xFFF409;
    private static final int GENERATOR_TABLE_SIZE= 1 << 8 , CRC_SIZE = 24;
    private final int[] table;

    /**
     * Default Crc24 constructor.
     * Returns a CRC24 calculator
     *
     * @param generator (int): Integer from which the 24 bits are used
     */
    public Crc24(int generator) {
        this.table = buildTable(generator);
    }

    /**
     * Returns the CRC24 of the byte array
     *
     * @param bytes (byte[]): Byte array from which the CRC24 has to be returned
     * @return (int): CRC24 of the given byte array
     */
    public int crc(byte[] bytes) {
        int crc = 0;

        for (byte b : bytes)
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(b)) ^
                    table[Bits.extractUInt(crc, CRC_SIZE - Byte.SIZE, Byte.SIZE)];


        for (int i = 0; i < (CRC_SIZE / Byte.SIZE); i++)
            crc = (crc << Byte.SIZE) ^ table[Bits.extractUInt(crc, CRC_SIZE - Byte.SIZE,
                    Byte.SIZE)];


        return Bits.extractUInt(crc, 0, CRC_SIZE);
    }

    /**
     * Calculates the crc bit by bit
     *
     * @param generator (int): Desired generator
     * @param toCrc24   (byte[]): Byte array to convert into crc
     * @return (int): The crc
     */
    private static int crc_bitwise(int generator, byte[] toCrc24) {
        int[] table = new int[] {0, generator};
        int crc = 0;

        for (byte b : toCrc24) {
            for (int j = Byte.SIZE - 1; 0 <= j; j--) {
                crc = ((crc << 1) | Bits.extractUInt(b, j, 1)) ^
                        table[Bits.extractUInt(crc, CRC_SIZE - 1, 1)];
            }
        }

        for (int i = 0; i < CRC_SIZE; i++)
            crc = (crc << 1) ^ table[Bits.extractUInt(crc, CRC_SIZE - 1, 1)];

        return Bits.extractUInt(crc, 0, CRC_SIZE);
    }

    /**
     * Builds a table corresponding to the given generator
     *
     * @param generator (int): Given generator
     * @return (int[]): Table corresponding to the generator
     */
    private static int[] buildTable(int generator) {
        int[] table = new int[GENERATOR_TABLE_SIZE];

        for (int i = 0; i < table.length; i++)
            table[i] = crc_bitwise(generator, new byte[] {(byte) i});

        return table;
    }
}
