package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Public: Manages the aircraft table
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AircraftTableController {
    private static final int ICAO_ADDRESS_PREF_WIDTH = 60, CALL_SIGN_DESCRIPTION_PREF_WIDTH = 70,
                                REGISTRATION_PREF_WIDTH = 80, MODEL_PREF_WIDTH = 230,
                                TYPE_PREF_WIDTH = 50, NUM_PREF_WIDTH = 85;
    private static final int POSITION_FRAC_DIGIT = 4, ALT_VEL_FRAC_DIGIT = 0;
    private final TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftProperty;
    private Consumer<ObservableAircraftState> consumer;

    /**
     * Default AircraftTableController.
     * Defines the controller of the table
     *
     * @param aircraftStates   (ObservableSet<ObservableAircraftState>): observable set of
     *                         aircraft states that should appear on the view
     * @param selectedAircraftProperty (ObjectProperty<ObservableAircraftState>): Property containing the
     *                         state of the selected aircraft
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        this.aircraftStates = aircraftStates;
        this.selectedAircraftProperty = selectedAircraftProperty;
        this.tableView = new TableView<>();

        tableView.getStylesheets().add("table.css");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        tableCreator();
        handleMouseEvent();
        addListeners();
    }

    /**
     * Returns the JavaFX node displaying the table
     *
     * @return (Node): Aircraft's table controller node
     */
    public Node pane() {
        return tableView;
    }

    /** Generates the table*/
    private void tableCreator() {
        tableView.getColumns().setAll(
                textualColumn("OACI", ICAO_ADDRESS_PREF_WIDTH,
                        f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress().string())),
                textualColumn("Indicatif", CALL_SIGN_DESCRIPTION_PREF_WIDTH,
                        f -> f.callSignProperty().map(CallSign::string)),
                textualColumn("Immatriculation", REGISTRATION_PREF_WIDTH,
                        f -> (new ReadOnlyObjectWrapper<>(f.getAircraftData())
                                        .map(d -> d.registration().string()))),
                textualColumn("Modèle", MODEL_PREF_WIDTH,
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                                .map(AircraftData::model)),
                textualColumn("Type", TYPE_PREF_WIDTH,
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                                .map(d -> d.typeDesignator().string())),
                textualColumn("Description", CALL_SIGN_DESCRIPTION_PREF_WIDTH,
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                                .map(d -> d.description().string())),
                numericalColumn("Longitude (°)", Units.Angle.DEGREE, POSITION_FRAC_DIGIT,
                        f -> f.positionProperty().map(GeoPos::longitude)),
                numericalColumn("Latitude (°)", Units.Angle.DEGREE, POSITION_FRAC_DIGIT,
                        f -> f.positionProperty().map(GeoPos::latitude)),
                numericalColumn("Altitude (m)", Units.Length.METER, ALT_VEL_FRAC_DIGIT,
                        f -> f.altitudeProperty().map(Number::doubleValue)),
                numericalColumn("Vitesse (km/h)", Units.Speed.KILOMETER_PER_HOUR,
                        ALT_VEL_FRAC_DIGIT,
                        f -> f.velocityProperty().map(Number::doubleValue))
        );
    }

    /**
     * Generates a column containing textual data with the given parameters
     *
     * @param title     (String): Title of the column
     * @param prefWidth (int): Width of the column
     * @param function  (Function<ObservableAircraftState, ObservableValue<String>>): Function
     *                   applied to the cell
     * @return (TableColumn<ObservableAircraftState, String>): Desired column
     */
    private TableColumn<ObservableAircraftState, String> textualColumn(String title, int prefWidth,
                               Function<ObservableAircraftState, ObservableValue<String>> function) {

        TableColumn<ObservableAircraftState, String> textColumn = new TableColumn<>(title);
        textColumn.setCellValueFactory(v -> function.apply(v.getValue()));
        textColumn.setPrefWidth(prefWidth);

        return textColumn;
    }

    /**
     * Generates a column containing numerical data with the given parameters
     *
     * @param title        (String): Title of the column
     * @param unit         (Double): Unit of the values in the column
     * @param fracDigit (NumberFormat): Formatting of the displayed number,
     *                     containing the fixed number of fractional digits
     * @param function     (Function<ObservableAircraftState, ObservableValue<Double>>): Function
     *                     applied to the cell
     * @return (TableColumn<ObservableAircraftState, String>): Desired column
     */
    private TableColumn<ObservableAircraftState, String> numericalColumn(String title, Double unit,
        int fracDigit, Function<ObservableAircraftState, ObservableValue<Double>> function) {

        //Formatting the number value
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRENCH);//I have a US computer
        numberFormat.setMinimumFractionDigits(fracDigit);
        numberFormat.setMaximumFractionDigits(fracDigit);

        //Creating the column with the desired parameters
        TableColumn<ObservableAircraftState, String> numColumn = new TableColumn<>(title);
        numColumn.setCellValueFactory(v -> function.apply(v.getValue()).map(m -> Double.isNaN(m)
                        ? "" : numberFormat.format(Units.convertTo(m, unit))));
        numColumn.setPrefWidth(NUM_PREF_WIDTH);
        numColumn.getStyleClass().add("numeric");

        //Setting up the comparator in order to sort the values of the column
        numColumn.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            } else {
                try {
                    return Double.compare(
                            numberFormat.parse(s1).doubleValue(),
                            numberFormat.parse(s2).doubleValue());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return numColumn;
    }

    /**
     * Calls the accept method of the given consumer if a double click is detected
     *
     * @param consumer (Consumer<ObservableAircraftState>): Given consumer
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        this.consumer = consumer;
    }

    private void handleMouseEvent() {
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2
                    && MouseButton.PRIMARY.equals(e.getButton())
                    && consumer != null
                    && tableView.getSelectionModel().getSelectedItem() != null)
                consumer.accept(tableView.getSelectionModel().getSelectedItem());
        });
    }

    /** Adds all the aircraft states listeners */
    private void addListeners() {
        aircraftStates.addListener(
                (SetChangeListener<ObservableAircraftState>) change -> {
                    if (change.wasAdded()) {
                        tableView.getItems().add(change.getElementAdded());
                        tableView.sort();
                    }
                    if (change.wasRemoved())
                        tableView.getItems().remove(change.getElementRemoved());
                });

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (p, oldS, newS) -> selectedAircraftProperty.set(newS));

        selectedAircraftProperty.addListener((p, oldS, newS) -> {
            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), newS))
                    tableView.scrollTo(newS);
            tableView.getSelectionModel().select(newS);
        });
    }
}
