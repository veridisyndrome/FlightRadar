package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Public: Manages the status line
 *
 * @author Pablo Robin Guerrero (356671)
 */
public final class StatusLineController {
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;
    private final BorderPane statusPane;

    /**
     * Default StatusLineController constructor.
     * Defines a scene graph
     */
    public StatusLineController() {
        this.aircraftCountProperty = new SimpleIntegerProperty(0);
        this.messageCountProperty = new SimpleLongProperty(0);

        Text leftText = new Text();
        leftText.textProperty().bind(Bindings.format("Aéronefs visibles :  %d", aircraftCountProperty));

        Text rightText = new Text();
        rightText.textProperty().bind(Bindings.format("Messages reçus :  %d", messageCountProperty));

        this.statusPane = new BorderPane(null, null, rightText, null, leftText);
        this.statusPane.getStylesheets().add("status.css");
    }

    /**
     * Returns the JavaFX pane displaying the line status
     *
     * @return (Pane): The pane
     */
    public Pane pane() {return statusPane;}

    /**
     * Returns the modifiable property containing the number of aircraft currently visible
     *
     * @return (IntegerProperty): Aircraft's count property
     */
    public IntegerProperty getAircraftCountProperty() {return aircraftCountProperty;}

    /**
     * Returns the modifiable property containing the number of messages
     * received since the launch of the program
     *
     * @return (LongProperty): Aircraft's message count property
     */
    public LongProperty getMessageCountProperty() {return messageCountProperty;}
}
