package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Public: Manages the main program
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class Main extends Application {
    private static final String TILE_CACHE_PATH = "tile-cache",
            TILE_SERVER_PATH = "tile.openstreetmap.org";
    private static final String DATABASE_PATH = "/aircraft.zip";
    private static final long PURGE_TIME = Duration.ofSeconds(1).toNanos();
    private static final long NANO_TO_MILLI = Duration.ofMillis(1).toNanos();
    private static final int MIN_WIDTH_WINDOW = 800;
    private static final int MIN_HEIGHT_WINDOW = 600;
    private static final int BEGINNING_ZOOM = 8;
    private static final int BEGINNING_X = 33_530;
    private static final int BEGINNING_Y = 23_070;
    public static void main(String[] args) {launch(args);}


    @Override
    public void start(Stage primaryStage) throws Exception {
        //Map creation
        TileManager tm =
                new TileManager(Path.of(TILE_CACHE_PATH), TILE_SERVER_PATH);
        MapParameters mp =
                new MapParameters(BEGINNING_ZOOM, BEGINNING_X, BEGINNING_Y);
        BaseMapController bmc = new BaseMapController(tm, mp);

        //Database creation
        URL aircraftDatabseUrl = getClass().getResource(DATABASE_PATH);
        assert aircraftDatabseUrl != null;
        Path aircraftDatabasePath = Path.of(aircraftDatabseUrl.toURI());
        AircraftDatabase db = new AircraftDatabase(aircraftDatabasePath.toString());

        //Aircraft functionality creation
        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
        AircraftController ac = new AircraftController(mp, asm.states(), sap);
        AircraftTableController at = new AircraftTableController(asm.states(), sap);
        StatusLineController slc = new StatusLineController();

        //Table mouse and trackpad event handler
        at.setOnDoubleClick(f -> bmc.centerOn(f.getPosition()));

        //Status line controller aircraft count binding
        slc.getAircraftCountProperty().bind(Bindings.size(asm.states()));

        //Window panes creation
        StackPane mapAndAircraftPane = new StackPane(bmc.pane(), ac.pane());
        BorderPane tableAndStatusPane
                = new BorderPane(at.pane(), slc.pane(), null, null, null);

        //Window parameters
        SplitPane mainPane = new SplitPane(mapAndAircraftPane, tableAndStatusPane);
        mainPane.setOrientation(Orientation.VERTICAL);

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(MIN_WIDTH_WINDOW);
        primaryStage.setMinHeight(MIN_HEIGHT_WINDOW);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();


        //Messages list creation
        ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();

        //Message supplier creation
        Supplier<Message> supplier = getParameters().getRaw().isEmpty()
                ? radioSupplier()
                : fileSupplier(getParameters().getRaw().get(0));


        //Parallel thread (for the aircraft functionalities) creation
        Thread parallelThread = new Thread(() -> {
            while (true) {
                Message message = supplier.get();
                if (message != null) messages.add(message);
            }
        });
        parallelThread.setDaemon(true);
        parallelThread.start();

        new AnimationTimer() {
            long lastPurge = 0;

            //Status line controller messages count binding
            private final LongProperty messagesCountProperty = slc.getMessageCountProperty();
            @Override
            public void handle(long now) {
                try {
                    if (PURGE_TIME <= now - lastPurge) {
                        asm.purge();
                        lastPurge = now;
                    }

                    long messagesCount = 0;

                    while (!messages.isEmpty()) {
                        ++messagesCount;
                        asm.updateWithMessage(messages.remove());
                    }

                    messagesCountProperty.set(messagesCountProperty.get() + messagesCount);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    /**
     * Method reading all the messages int the file
     *
     * @param fileName (String): Given file
     * @return (List<Message>): List of parsed messages
     * @throws RuntimeException If there is a Runtime error
     */
    private static List<Message> readAllMessages(String fileName){
        List<Message> messagesList = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))){
            byte[] bytes = new byte[RawMessage.LENGTH];

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                Message m = MessageParser.parse(Objects.requireNonNull(rawMessage));

                messagesList.add(m);
            }
        } catch (EOFException e) {
            return messagesList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method in charge of the radio messages supplier creation
     *
     * @return (Supplier<Message>): Radio supplier
     * @throws RuntimeException If there is a Runtime error
     */
    private static Supplier<Message> radioSupplier() throws IOException {
        AdsbDemodulator ad = new AdsbDemodulator(System.in);
        return () -> {
            try {
                while (true) {
                    RawMessage m;
                    if ((m = ad.nextMessage()) != null) {
                        return MessageParser.parse(m);
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Method in charge of the file messages supplier creation
     *
     * @param fileName (String): Given file
     * @return (Supplier<Message>): File supplier
     * @throws RuntimeException If there is a Runtime error
     */
    private static Supplier<Message> fileSupplier(String fileName) {
        List<Message> messagesList = readAllMessages(fileName);
        Iterator<Message> messageIterator = messagesList.iterator();

        long startTime = System.nanoTime();

        return (() -> {
            if (messageIterator.hasNext()) {
                Message m = messageIterator.next();
                long sleepTime = m.timeStampNs() - (System.nanoTime() - startTime);

                if (0 < sleepTime) {
                    try {
                        Thread.sleep(sleepTime / NANO_TO_MILLI);
                        return m;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return m;
            } else {
                return null;
            }
        });
    }
}