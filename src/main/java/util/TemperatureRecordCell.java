package util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import model.TemperatureRecord;

import static util.Constants.SPACING_STYLE;

// Credits: https://stackoverflow.com/questions/15661500/javafx-listview-item-with-an-image-button
public class TemperatureRecordCell extends ListCell<TemperatureRecord> {
    HBox hbox = new HBox();
    Label name = new Label("John Doe");
    Label temperature = new Label("35.0");
    ImageView status = new ImageView();
    Pane pane = new Pane();
    Button button = new Button("Action");
    String lastItem;

    public TemperatureRecordCell() {
        super();
        padContent();
        hbox.getChildren().addAll(name, temperature, status, pane, button);
        hbox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pane, Priority.ALWAYS);
        button.setOnAction(event -> System.out.println(lastItem + " : " + event));
    }

    @Override
    protected void updateItem(TemperatureRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) { // <== test for null item and empty parameter
            name.setText(item.getMemberName());
            temperature.setText(item.getTemperature().toString() + " \u00B0C"); // To show degree celsius
            Image img = new Image(getClass().getResourceAsStream("/assets/submitted.png"));
            status.setImage(img);
            setGraphic(hbox);
        } else {
            setGraphic(null);
        }
    }

    // Hardcoded styling for now
    private void padContent() {
        name.setStyle(SPACING_STYLE + "-fx-pref-width: 316; -fx-min-width: 316; -fx-max-width: 316;");
        temperature.setStyle(SPACING_STYLE + "-fx-pref-width: 116; -fx-min-width: 116; -fx-max-width: 116;");
        status.setFitHeight(24);
        status.setFitWidth(24);
    }
}
