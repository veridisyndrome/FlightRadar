package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Public and Final: Represents a mictronics database
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AircraftDatabase {
    private static final String SEPARATOR = ",";
    private static final String CSV = ".csv";
    private static final int FILENAME_SIZE = 4,  AIRCRAFT_REGISTRATION_INDEX = 1,
    AIRCRAFT_TYPE_DESIGNATOR_INDEX = 2, AIRCRAFT_MODEL_INDEX = 3,
    AIRCRAFT_DESCRIPTION_INDEX = 4, WAKE_TURBULENCE_INDEX = 5;
    private final String fileName;
    /**
     * Default AircraftDatabase constructor.
     * Defines an object representing mictronics database
     *
     * @param fileName (String): File name
     * @throws NullPointerException If argument is null
     */
    public AircraftDatabase(String fileName) {
        this.fileName = requireNonNull(fileName);
    }

    /**
     * Creates an AircraftData with the data of the ICAO address
     *
     * @param address (IcaoAddress): Given address
     * @return (AircraftData): Data of the aircraft
     * @throws IOException If there is an input/output error
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        try (ZipFile zipFile = new ZipFile(fileName);
             InputStream inputStream = zipFile
                     .getInputStream(zipFile
                             .getEntry(address
                                     .string()
                                     .substring(FILENAME_SIZE) + CSV));
             Reader reader = new InputStreamReader(inputStream, UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            while (true) {
                String line = bufferedReader.readLine();

                if (line == null ||
                        !line.startsWith(address.string()) &&
                        0 < line.compareTo(address.string())) {
                    return null;
                }

                if (line.startsWith(address.string())) {
                    String[] aircraftData = line.split(SEPARATOR, -1);

                    return new AircraftData(
                            new AircraftRegistration(aircraftData[AIRCRAFT_REGISTRATION_INDEX]),
                            new AircraftTypeDesignator(aircraftData[AIRCRAFT_TYPE_DESIGNATOR_INDEX]),
                            aircraftData[AIRCRAFT_MODEL_INDEX],
                            new AircraftDescription(aircraftData[AIRCRAFT_DESCRIPTION_INDEX]),
                            WakeTurbulenceCategory.of(aircraftData[WAKE_TURBULENCE_INDEX]));
                }
            }
        }
    }
}
