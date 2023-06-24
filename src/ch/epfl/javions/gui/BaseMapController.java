package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Objects;

/**
 * Public: Manages the display and interactions of the map
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class BaseMapController {
    private static final int TILE_PIXELS = 256;
    private static final long DELTA_TIME = 200;
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private final ObjectProperty<Point2D> mousePositionProperty;
    private final Canvas canvas;
    private final Pane pane;
    private final GraphicsContext graphicsContext;
    private boolean redrawNeeded;

    /**
     * Default BaseMapController constructor.
     * Defines the map controller
     *
     * @param tileManager   (TileManager): Tile manager required to get the tiles
     * @param mapParameters (MapParameters): Parameters of the visible portion of the map
     * @throws NullPointerException If the Tile manager is null
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = Objects.requireNonNull(tileManager);
        this.mapParameters = mapParameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.mousePositionProperty = new SimpleObjectProperty<>(Point2D.ZERO);

        handleMouseEvents();
        addBindings();
        addListeners();
    }

    /**
     * Returns the JavaFX pane displaying the map
     *
     * @return (Pane): The pane
     */
    public Pane pane(){
        return pane;
    }

    /**
     * Redraws the map if the boolean redrawIfNeeded is true
     *
     * @throws RuntimeException If there is a Runtime error
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        try {
            double minX = mapParameters.getMinX();
            double minY = mapParameters.getMinY();

            int minIndexX = (int) (minX / TILE_PIXELS);
            int minIndexY = (int) (minY / TILE_PIXELS);

            int tileWidth = (int) ((canvas.getWidth() + minX) / TILE_PIXELS);
            int tileHeight = (int) ((canvas.getHeight() + minY) / TILE_PIXELS);

            for (int i = minIndexX; i <= tileWidth; i++) {
                double x = i * TILE_PIXELS - minX;
                for (int j = minIndexY; j <= tileHeight; j++) {
                    double y = j * TILE_PIXELS - minY;
                    if (TileManager.TileId.isValid(mapParameters.getZoom(), i, j)) {
                        Image tile = tileManager.imageForTileAt(
                                new TileManager.TileId(mapParameters.getZoom(), i, j));

                        graphicsContext.drawImage(tile, x, y);
                    }
                }
            }
        } catch (IOException ignored) {  }
    }

    /** Asks for a redrawing at the next pulse*/
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /** Handles all the mouse or trackpad interactions*/
    private void handleMouseEvents() {
        pane.setOnMousePressed(e ->
            mousePositionProperty.set(new Point2D(e.getX(), e.getY()))
        );

        pane.setOnMouseDragged(e -> {
            Point2D previousPosition = mousePositionProperty.get();
            Point2D currentPosition = new Point2D(e.getX(), e.getY());
            if (!Objects.equals(previousPosition, currentPosition)) {
                mapParameters.scroll(
                        (previousPosition.getX() - currentPosition.getX()),
                        (previousPosition.getY() - currentPosition.getY()));
            }
            mousePositionProperty.set(currentPosition);
        });

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + DELTA_TIME);

            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            double x = e.getX();
            double y = e.getY();

            mousePositionProperty.set(new Point2D(x, y));

            mapParameters.scroll(x, y);
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-x, -y);

        });
    }

    /**
     * Moves the visible portion of the map to center the given point
     *
     * @param geoPos (GeoPos): Given point
     */
    public void centerOn(GeoPos geoPos) {
        double newX = WebMercator.x(mapParameters.getZoom(), geoPos.longitude());
        double newY = WebMercator.y(mapParameters.getZoom(), geoPos.latitude());

        double oldX = mapParameters.getMinX();
        double oldY = mapParameters.getMinY();

        mapParameters.scroll(
                newX - oldX - canvas.getWidth() / 2,
                newY - oldY - canvas.getHeight() / 2);
    }

    /** Adds all the canvas bindings*/
    private void addBindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    /** Adds all the canvas and map parameters listeners*/
    private void addListeners() {
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener(c -> redrawOnNextPulse());
        canvas.heightProperty().addListener(c -> redrawOnNextPulse());

        mapParameters.minXProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(c -> redrawOnNextPulse());
    }
}
