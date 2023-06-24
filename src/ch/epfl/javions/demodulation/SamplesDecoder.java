package ch.epfl.javions.demodulation;

import java.io.*;
import java.util.Objects;
import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public and Final: Represents a sample decoder, capable of transforming
 * the bytes coming from the AirSpy into 12-signed-bit samples
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class SamplesDecoder {
    private static final int BIAS =  1 << 11;
    private final InputStream stream;
    private final byte[] bytes;

    /**
     * Default SamplesDecoder constructor.
     * Defines the byte array size to the double of the batch size
     *
     * @param stream    (InputStream): Given stream
     * @param batchSize (int): Size of the batches
     * @throws IllegalArgumentException If batch size is not strictly positive
     * @throws NullPointerException If the stream is null
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        checkArgument(0 < batchSize);

        this.stream = Objects.requireNonNull(stream);
        bytes = new byte[Short.BYTES * batchSize];
    }

    /**
     * Converts read bytes in signed samples
     *
     * @param batch (short[]): Array in which the samples are stored
     * @return (int): Number of samples actually converted
     * @throws IOException If there is an Input/Output error
     * @throws IllegalArgumentException If the size of the given array isn't
     * equal to the length of a batch
     */
    public int readBatch(short[] batch) throws IOException {
        checkArgument(bytes.length / Short.BYTES == batch.length);

        int samples = stream.readNBytes(bytes, 0, bytes.length);

        for (int i = 0; i < samples; i += Short.BYTES)
            batch[i/Short.BYTES] =
                    (short) ((Byte.toUnsignedInt(bytes[i]) | (bytes[i+1] << Byte.SIZE)) - BIAS);

        return samples / Short.BYTES;
    }
}
