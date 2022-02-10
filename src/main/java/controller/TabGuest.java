package controller;

import com.google.cloud.Timestamp;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import model.TemperatureRecord;
import util.StateManager;
import util.TemperatureRecordCell;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TabGuest extends VBox {

    @FXML
    DatePicker guestDatePicker;
    @FXML
    BorderPane guestListContainer;
    @FXML
    Label noGuestRecordsText;

    private ListView<TemperatureRecord> guestListView = new ListView<>();
    private IFirebaseDB firebaseDB = new FirebaseDB();

    public TabGuest() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tab_guest.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Init datepicker
        guestDatePicker.setValue(LocalDate.now());
        guestDatePicker.valueProperty().addListener((ov, oldValue, newValue) -> {
            showGuestListForSelectedDate( Date.from( newValue.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant() ) );
        });
        // Query the current date guests on start
        LocalDateTime currentLocalDateTime = LocalDate.now().atStartOfDay();
        showGuestListForSelectedDate( Date.from(currentLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()) );
    }

    private void showGuestListForSelectedDate(Date selectedDate) {
        List<TemperatureRecord> guestTemperatureRecords = new ArrayList<>();
        Timestamp selectedTimestamp = Timestamp.of(selectedDate);
        try {
            guestTemperatureRecords = firebaseDB.getGuestTemperatureRecords(StateManager.getCurrentOrg(), selectedTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set notice for no records
        if (guestTemperatureRecords.isEmpty()) {
            noGuestRecordsText.setVisible(true);
        } else {
            noGuestRecordsText.setVisible(false);
        }

        ObservableList<TemperatureRecord> observableList = FXCollections.observableArrayList(guestTemperatureRecords);
        guestListView.setItems(observableList);
        guestListView.setCellFactory(new Callback<ListView<TemperatureRecord>, ListCell<TemperatureRecord>>() {
            @Override
            public ListCell<TemperatureRecord> call(ListView<TemperatureRecord> param) {
                return new TemperatureRecordCell();
            }
        });
        guestListContainer.setCenter(guestListView);
    }
}
