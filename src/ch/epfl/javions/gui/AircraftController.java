package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

/**
 * Public: Manages the view of the aircraft
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class AircraftController {
    private static final int MIN_ZOOM_VISIBLE = 11;
    private static final double MAX_ALTITUDE = 12000d;
    private static final AircraftTypeDesignator UNKNOWN_TYPE_DESIGNATOR
            = new AircraftTypeDesignator("");
    private static final AircraftDescription UNKNOWN_DESCRIPTION
            = new AircraftDescription("");
    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftProperty;
    private final Pane aircraftControllerPane;

    /**
     * Default AircraftController constructor.
     * Defines the manager of the aircraft's view
     *
     * @param mapParameters   (MapParameters): Parameters of the portion
     *                        of the map visible on the screen
     * @param aircraftStates  (ObservableSet<ObservableAircraftState>): observable set of
     *                        aircraft states that should appear on the view
     * @param selectedAircraftProperty (ObjectProperty<ObservableAircraftState>):
     *                                 Property containing the state of the selected aircraft
     *
     */
    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraftProperty) {
        Preconditions.checkArgument(aircraftStates.isEmpty());

        this.mapParameters = mapParameters;
        this.aircraftStates = aircraftStates;
        this.selectedAircraftProperty = selectedAircraftProperty;

        aircraftControllerPane = new Pane();
        aircraftControllerPane.getStylesheets().add("aircraft.css");
        aircraftControllerPane.setPickOnBounds(false);

        addListeners();
    }

    /**
     * Returns the JavaFX pane displaying the aircraft
     *
     * @return (Pane): Aircraft's controller pane
     */
    public Pane pane() {
        return aircraftControllerPane;
    }

    /**
     * Handles the creation of the label
     *
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     * @return (Node): Node representing the label
     */
    private Node labelCreator(ObservableAircraftState addedAircraft) {
        Object registrationText = addedAircraft.getAircraftData() != null
                ? addedAircraft.getAircraftData().registration().string()
                : Bindings.when(addedAircraft.callSignProperty().isNotNull())
                .then(Bindings.convert(addedAircraft.callSignProperty().map(CallSign::string)))
                .otherwise(addedAircraft.getIcaoAddress().string());



        ObservableValue<String> velocityText = addedAircraft.velocityProperty().map(
                v -> Double.isNaN(addedAircraft.getVelocity()) ? "? km/h"
                        : String.format("%4.0f km/h",
                        Units.convertTo(addedAircraft.getVelocity(), Units.Speed.KILOMETER_PER_HOUR))
        );

        ObservableValue<String> altitudeText = addedAircraft.altitudeProperty().map(
                v -> Double.isNaN(addedAircraft.getAltitude()) ? "? m" :
                        String.format("\u2002%5.0f m", addedAircraft.getAltitude())
        );


        Text labelText = new Text();
        labelText.textProperty().bind(
                Bindings.format("%s" + "\n%s" + "%s",
                        registrationText,
                        velocityText,
                        altitudeText)
        );

        Rectangle labelRectangle = new Rectangle();
        labelRectangle.widthProperty().bind(
                labelText.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        labelRectangle.heightProperty().bind(
                labelText.layoutBoundsProperty().map((b -> b.getHeight() + 4)));

        Group labelGroup = new Group(labelRectangle, labelText);
        labelGroup.getStyleClass().add("label");

        labelGroup.visibleProperty().bind(
                Bindings.createBooleanBinding( () ->
                                (MIN_ZOOM_VISIBLE <= mapParameters.getZoom() ||
                                        addedAircraft.equals(selectedAircraftProperty.get())),
                        mapParameters.zoomProperty(), selectedAircraftProperty)
        );

        return labelGroup;
    }

    /**
     * Handles the creation of the icon
     *
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     * @return (Node): Node representing the icon
     */
    private Node iconCreator(ObservableAircraftState addedAircraft) {
        AircraftData aircraftData = addedAircraft.getAircraftData();

        AircraftTypeDesignator typeDesignator = aircraftData == null
                ? UNKNOWN_TYPE_DESIGNATOR
                : addedAircraft.getAircraftTypeDesignator();
        AircraftDescription aircraftDescription = aircraftData == null
                ? UNKNOWN_DESCRIPTION
                : addedAircraft.getAircraftDescription();
        WakeTurbulenceCategory wakeTurbulenceCategory = aircraftData == null
                ? WakeTurbulenceCategory.UNKNOWN
                : addedAircraft.getWakeTurbulenceCategory();

        ObservableValue<AircraftIcon> aircraftIcon = addedAircraft.categoryProperty()
                .map(c -> AircraftIcon.iconFor(
                        typeDesignator,
                        aircraftDescription,
                        c.intValue(),
                        wakeTurbulenceCategory));

        SVGPath iconSvgPath = new SVGPath();
        iconSvgPath.getStyleClass().add("aircraft");

        iconSvgPath.contentProperty().bind(aircraftIcon.map(AircraftIcon::svgPath));

        iconSvgPath.rotateProperty().bind(
                Bindings.createDoubleBinding( () ->
                    aircraftIcon.getValue().canRotate()
                    && ! Double.isNaN(addedAircraft.getTrackOrHeading())
                            ? Units.convertTo(addedAircraft.getTrackOrHeading(), Units.Angle.DEGREE)
                            : 0d
                , addedAircraft.trackOrHeadingProperty(), aircraftIcon));

        iconSvgPath.fillProperty().bind(
                Bindings.createObjectBinding( () -> colorAtAltitude(addedAircraft.getAltitude()),
                        addedAircraft.altitudeProperty(), aircraftIcon));

        iconSvgPath.setOnMouseClicked(e -> selectedAircraftProperty.set(addedAircraft));

        return iconSvgPath;
    }

    /**
     * Handles the grouping of the icon and the label
     *
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     * @return (Node): Node representing the icon-label group
     */
    private Node iconLabelCreator(ObservableAircraftState addedAircraft) {
        Group iconLabelGroup = new Group(iconCreator(addedAircraft), labelCreator(addedAircraft));

        iconLabelGroup.layoutXProperty().bind(
                Bindings.createDoubleBinding( () ->
                                WebMercator.x(mapParameters.getZoom(),
                                        addedAircraft.getPosition().longitude())
                                        - mapParameters.getMinX(),
                        mapParameters.zoomProperty(), mapParameters.minXProperty(),
                        addedAircraft.positionProperty()));

        iconLabelGroup.layoutYProperty().bind(
                Bindings.createDoubleBinding( () ->
                                WebMercator.y(mapParameters.getZoom(),
                                        addedAircraft.getPosition().latitude())
                                        - mapParameters.getMinY(),
                        mapParameters.zoomProperty(), mapParameters.minYProperty(),
                        addedAircraft.positionProperty()));

        return iconLabelGroup;
    }

    /**
     * Handles the creation of the trajectory
     *
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     * @return (Node): Node representing the trajectory
     */
    private Node trajectoryCreator(ObservableAircraftState addedAircraft) {
        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");

        trajectoryGroup.visibleProperty().bind(selectedAircraftProperty.isEqualTo(addedAircraft));

        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        InvalidationListener trajectoryNeeded = l -> trajectoryDrawer(trajectoryGroup, addedAircraft);

        trajectoryGroup.visibleProperty().addListener(
                (property, wasVisible, isVisible) -> {
                    if(isVisible) {
                        trajectoryDrawer(trajectoryGroup, addedAircraft);
                        addedAircraft.trajectoryList().addListener(trajectoryNeeded);
                        mapParameters.zoomProperty().addListener(trajectoryNeeded);
                    } else {
                        addedAircraft.trajectoryList().removeListener(trajectoryNeeded);
                        mapParameters.zoomProperty().removeListener(trajectoryNeeded);
                        trajectoryGroup.getChildren().clear();
                    }
                }
        );

        return trajectoryGroup;
    }

    /**
     * Draws the trajectoryGroup
     *
     * @param trajectoryGroup (Group): Given trajectoryGroup
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     */
    private void trajectoryDrawer(Group trajectoryGroup, ObservableAircraftState addedAircraft) {
        trajectoryGroup.getChildren().clear();

        double startX = WebMercator.x(mapParameters.getZoom(),
                addedAircraft.trajectoryList()
                        .get(0).position().longitude());
        double startY = WebMercator.y(mapParameters.getZoom(),
                addedAircraft.trajectoryList()
                        .get(0).position().latitude());

        double endX, endY;

        for (int i = 1; i < addedAircraft.trajectoryList().size(); ++i) {
            ObservableAircraftState.AirbornePos endAircraft =
                    addedAircraft.trajectoryList().get(i);

            endX = WebMercator.x(mapParameters.getZoom(),
                    endAircraft.position().longitude());
            endY = WebMercator.y(mapParameters.getZoom(),
                    endAircraft.position().latitude());

            Line trajectoryLine = new Line(startX, startY, endX, endY);

            startX = endX;
            startY = endY;

            trajectoryLine.strokeProperty()
                    .set(colorAtAltitude(endAircraft.altitude()));

            trajectoryGroup.getChildren().add(trajectoryLine);
        }
    }

    /**
     * Creates the representation of the aircraft
     *
     * @param addedAircraft (ObservableAircraftState): Subject aircraft
     */
    private void aircraftCreator(ObservableAircraftState addedAircraft) {
        Group aircraftGroup
                = new Group(trajectoryCreator(addedAircraft), iconLabelCreator(addedAircraft));

        aircraftGroup.setId(addedAircraft.getIcaoAddress().string());
        aircraftGroup.viewOrderProperty().bind(addedAircraft.altitudeProperty().negate());

        aircraftControllerPane.getChildren().add(aircraftGroup);
    }

    /**
     * Removes the aircraft from the pane
     *
     * @param removedAircraft (ObservableAircraftState): Aircraft to remove
     */
    private void aircraftRemover(ObservableAircraftState removedAircraft) {
        aircraftControllerPane.getChildren()
                .removeIf(n -> n.getId().equals(removedAircraft.getIcaoAddress().string()));
    }

    /**
     * Gives the color corresponding to the given altitude
     *
     * @param altitude (double): Given altitude
     * @return (Color): Corresponding color
     */
    private Color colorAtAltitude(double altitude) {
        return ColorRamp.PLASMA.at(Math.cbrt(altitude / MAX_ALTITUDE));
    }

    /** Adds all the aircraft states listeners*/
    private void addListeners() {
        aircraftStates.addListener(
                (SetChangeListener<ObservableAircraftState>) change -> {
                    if (change.wasRemoved()) aircraftRemover(change.getElementRemoved());
                    if (change.wasAdded())  aircraftCreator(change.getElementAdded());
                });
    }
}
