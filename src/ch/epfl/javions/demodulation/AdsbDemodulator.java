package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Public and Final: Represents a demodulator for ADS-B messages
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AdsbDemodulator {
    private static final int CST_PREAMBLE_ONE = 80, CST_PREAMBLE_TWO = 85, CST_PREAMBLE_THREE = 10;
    private static final int CONVERT_TO_TIMESTAMP_NS = 100;
    private static final int WINDOW_SIZE = 1200;
    private static final int PULSE_WEIGHT = 5;
    private final PowerWindow powerWindow;
    private final byte[] message;

    /**
     * Default AdsbDemodulator constructor.
     * Defines a demodulator receiving the bytes containing the samples of the given stream
     *
     * @param samplesStream (InputStream): Given stream
     * @throws IOException If there is an Input/Output error
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        powerWindow = new PowerWindow(samplesStream, WINDOW_SIZE);
        this.message = new byte[RawMessage.LENGTH];
    }

    /**
     * Gives the next ADS-B message of the stream
     *
     * @return (RawMessage): Next raw ADS-B message, or null if there are no more
     * @throws IOException If there is an Input/Output error
     */
    public RawMessage nextMessage() throws IOException {
        int p = powerWindow.get(0)
                + powerWindow.get(PULSE_WEIGHT * 2)
                + powerWindow.get(PULSE_WEIGHT * 7)
                + powerWindow.get(PULSE_WEIGHT * 9);

        int pMinus1 = 0;

        while (powerWindow.isFull()) {
            int pPlus1 = powerWindow.get(1)
                    + powerWindow.get(PULSE_WEIGHT * 2 +1)
                    + powerWindow.get(PULSE_WEIGHT * 7 + 1)
                    + powerWindow.get(PULSE_WEIGHT * 9 + 1);

            int v = powerWindow.get(PULSE_WEIGHT)
                    + powerWindow.get(PULSE_WEIGHT * 3)
                    + powerWindow.get(PULSE_WEIGHT * 4)
                    + powerWindow.get(PULSE_WEIGHT * 5)
                    + powerWindow.get(PULSE_WEIGHT * 6)
                    + powerWindow.get(PULSE_WEIGHT * 8);

            // We verify that the preamble start the beginning of a window
            if ((2 * v <= p) && (pPlus1 < p) && (pMinus1 < p)) {
                message[0] = getByte(0);
                if (RawMessage.size(message[0]) != 0) {
                    for (int i = 1; i < message.length; i++) {
                        message[i] = getByte(i);
                    }
                    RawMessage rawMessage = RawMessage
                            .of(powerWindow.position() * CONVERT_TO_TIMESTAMP_NS, message);
                    if (rawMessage != null) {
                        powerWindow.advanceBy(WINDOW_SIZE);
                        return rawMessage;
                    }
                }
            }
            powerWindow.advance();

            pMinus1 = p;
            p = pPlus1;
        }
        return null;
    }

    /**
     * Decodes the byte of the message at the given index
     *
     * @param i (int): Index of the desired byte
     * @return (byte): Byte at the given index
     */
    private byte getByte(int i) {
        int byteInMessage = 0;
        for (int j = 0; j < Byte.SIZE; j++) {
            int p1 = powerWindow
                    .get(CST_PREAMBLE_ONE + CST_PREAMBLE_THREE * (i * Byte.SIZE + j));
            int p2 = powerWindow
                    .get(CST_PREAMBLE_TWO + CST_PREAMBLE_THREE * (i * Byte.SIZE + j));
            int bit = p1 < p2 ? 0 : 1;

            byteInMessage = (byteInMessage << 1) | bit;
        }
        return (byte) byteInMessage;
    }
}
