package ch.epfl.javions.demodulation;

import java.io.IOException;
import java.io.InputStream;

import static ch.epfl.javions.Preconditions.checkArgument;

/**
 * Public and Final: Represents a power calculator, i.e. an object capable of calculating
 * the power samples of the signal from the samples produced by a sample decoder
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class PowerComputer {
    private static final int ODD_VALUE_INDEX_ZERO = 1, ODD_VALUE_INDEX_ONE = 3,
            ODD_VALUE_INDEX_TWO = 5, ODD_VALUE_INDEX_THREE = 7;
    private static final int EVEN_VALUE_INDEX_ZERO = 0, EVEN_VALUE_INDEX_ONE = 2,
            EVEN_VALUE_INDEX_TWO = 4, EVEN_VALUE_INDEX_THREE = 6;

    private final SamplesDecoder samplesDecoder;
    private final short[] samplesComputer;
    private final int[] lastSamples = new int[Byte.SIZE];

    /**
     * Default PowerComputer Constructor.
     * Defines the short array size to the double of the batch size
     *
     * @param stream    (InputStream): Given stream
     * @param batchSize (int): Size of the batches
     * @throws IllegalArgumentException If batch size is not a strictly positive multiple of 8
     */
    public PowerComputer(InputStream stream, int batchSize) {
        checkArgument(0 < batchSize);
        checkArgument( batchSize % Byte.SIZE == 0);

        this.samplesComputer = new short[batchSize * Short.BYTES];
        this.samplesDecoder = new SamplesDecoder(stream, samplesComputer.length);
    }

    /**
     * Calculates the batches of power samples and places them in the given array
     *
     * @param batch (int[]): Given array
     * @return (int): Number of samples placed in the array
     * @throws IOException If there is an Input/Output error
     * @throws IllegalArgumentException If the given array size is not equal to the batch size
     */
    public int readBatch(int[] batch) throws IOException {
        checkArgument(batch.length == samplesComputer.length / Short.BYTES);

        int samples = samplesDecoder.readBatch(samplesComputer);
        int i = 0;

        while (i < samples) {
            lastSamples[i % Byte.SIZE] = samplesComputer[i];
            lastSamples[(i+1) % Byte.SIZE] = samplesComputer[i+1];

            int oddValue =
                    lastSamples[ODD_VALUE_INDEX_ZERO] - lastSamples[ODD_VALUE_INDEX_ONE]
                            + lastSamples[ODD_VALUE_INDEX_TWO] - lastSamples[ODD_VALUE_INDEX_THREE];
            int evenValue = lastSamples[EVEN_VALUE_INDEX_ZERO] - lastSamples[EVEN_VALUE_INDEX_ONE]
                    + lastSamples[EVEN_VALUE_INDEX_TWO] - lastSamples[EVEN_VALUE_INDEX_THREE];

            batch[i/Short.BYTES] = oddValue * oddValue + evenValue * evenValue;

            i += Short.BYTES;
        }
        return samples / Short.BYTES;
    }
}
