package controller;

import com.google.cloud.Timestamp;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import model.MemberRecord;
import model.TemperatureRecord;
import org.apache.commons.lang3.time.DateUtils;
import util.StateManager;
import util.TemperatureRecordCell;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static util.Constants.*;

public class TabHome extends AnchorPane {

    @FXML
    BorderPane memberListContainer;
    @FXML
    HBox chartHBox;
    @FXML
    Label chartCaption;
    @FXML
    TextField organisationCodeDisplay;
    @FXML
    Label submissionRateText;
    @FXML
    Label dateSectionTitleText;

    private List<TemperatureRecord> masterMemberTemperatureRecords = new ArrayList<>();
    private List<MemberRecord> masterMemberRecords = new ArrayList<>();

    private Integer currentSelectedChartIndex = 0;
    private PieChart currentSelectedChart = null;
    private static final Double CAPTION_OFFSET = 250.0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yy");
    IFirebaseDB firebaseDB = new FirebaseDB();

    public TabHome() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tab_home.fxml"));
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
        organisationCodeDisplay.setText(StateManager.getCurrentOrg());
        try {
            this.masterMemberRecords = firebaseDB.getMemberRecords(StateManager.getCurrentOrg());
            this.masterMemberTemperatureRecords = firebaseDB.getMemberTemperatureRecords(StateManager.getCurrentOrg(), Timestamp.now(), MAX_CHARTS_TO_BE_DISPLAYED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        fillChartHBox();
        displaySubmissionRecords(Timestamp.now());
        dateSectionTitleText.setText(simpleDateFormat.format(new Date()) + "'s records:");
    }

    // Query temperature records from past 5 days including today and fill 5 pie charts
    private void fillChartHBox() {
        List<PieChart> charts = new ArrayList<>();
        Integer maxCharts = MAX_CHARTS_TO_BE_DISPLAYED;
        Date currentDate = new Date();
        for (int i = 0; i < maxCharts; i++) {
            List<TemperatureRecord> temperatureRecordsForSelectedDate = getListOfTemperatureRecordsForSelectedDate(i);
            Integer submitted = 0;
            for (TemperatureRecord tempRecord : temperatureRecordsForSelectedDate) {
                if (tempRecord.getTemperature() != null) {
                    submitted++;
                }
            }

            // Filling data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Submitted", submitted),
                    new PieChart.Data("Not submitted", temperatureRecordsForSelectedDate.size()-submitted));

            // Setting event handlers for hover labels
            PieChart chart = new PieChart(pieChartData);
            chartCaption.setTextFill(Color.BLACK);
            chartCaption.setStyle("-fx-font: 24 arial; -fx-background-color: rgba(13, 13, 13, 0.3);");
            for (PieChart.Data data : chart.getData()) {
                int index = i;
                data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                        event -> {
                            chartCaption.setVisible(true);
                            // Set different offsets so labels do not go offscreen on the most right charts
                            if (index < maxCharts/2) {
                                chartCaption.setTranslateX(event.getSceneX());
                            } else {
                                chartCaption.setTranslateX(event.getSceneX() - CAPTION_OFFSET);
                            }
                            chartCaption.setTranslateY(event.getSceneY());
                            chartCaption.setText(data.getName() + ": " + data.getPieValue());
                        }
                );
                data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                        event -> chartCaption.setVisible(false)
                );
            }

            // Last few configs
            chart.setTitle(simpleDateFormat.format(DateUtils.addDays(currentDate, -(maxCharts-1-i))));
            chart.setLabelsVisible(false);
            chart.setAnimated(true);
            chart.setCursor(Cursor.HAND);
            chart.setStyle(CHART_UNSELECTED);
            chart.setOnMouseClicked(event -> {
                // Handle showing temperature records for this specific day
                dateSectionTitleText.setText(chart.getTitle() + "'s records:");
                chartHBox.getChildren().get(this.currentSelectedChartIndex).setStyle(CHART_UNSELECTED);
                this.currentSelectedChartIndex = chartHBox.getChildren().indexOf(chart);
                chart.setStyle(CHART_SELECTED_HIGHLIGHT);
                System.out.println(this.currentSelectedChartIndex);
                displaySubmissionRecords(Timestamp.now());
            });
            charts.add(chart);
        }
        // Auto select last chart to show first
        PieChart pieChart = charts.get(charts.size()-1);
        pieChart.setStyle(CHART_SELECTED_HIGHLIGHT);
        this.currentSelectedChartIndex = charts.size()-1;

        // If the HBox is not empty, clear it first.
        if (!chartHBox.getChildren().isEmpty()) {
            chartHBox.getChildren().clear();
        }
        chartHBox.getChildren().addAll(charts);
    }

    // Query temperature submission records for the current date TODO: Update this title
    private void displaySubmissionRecords(Timestamp date) {
        List<TemperatureRecord> selectedDateTempRecords;
        selectedDateTempRecords = getListOfTemperatureRecordsForSelectedDate(null);

        // Calculate submission rate
        Integer submitted = 0;
        for (TemperatureRecord tempRecord : selectedDateTempRecords) {
            if (tempRecord.getTemperature() != null) {
                submitted++;
            }
        }
        Double submissionRate = (submitted / (double) masterMemberRecords.size());
        submissionRate = BigDecimal.valueOf(submissionRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
        submissionRate *= 100;
        submissionRateText.setText(submissionRate + "%");

        // Now display
        Collections.sort(selectedDateTempRecords,
                Comparator.comparing(TemperatureRecord::getSubmissionDate, Comparator.nullsLast(Comparator.naturalOrder()))); // Not really sort by temperature but sort by whether temperature has been submitted
        ObservableList<TemperatureRecord> list = FXCollections.observableArrayList(selectedDateTempRecords);
        final ListView<TemperatureRecord> lv = new ListView<TemperatureRecord>(list);
        lv.setCellFactory(new Callback<ListView<TemperatureRecord>, ListCell<TemperatureRecord>>() {
            @Override
            public ListCell<TemperatureRecord> call(ListView<TemperatureRecord> param) {
                return new TemperatureRecordCell();
            }
        });
        memberListContainer.setCenter(lv);
    }

    private List<TemperatureRecord> getListOfTemperatureRecordsForSelectedDate(Integer providedIndex) {
        List<TemperatureRecord> selectedDateTempRecords = new ArrayList<>();
        List<TemperatureRecord> selectedDateSubmittedTempRecords = new ArrayList<>();
        for (MemberRecord memberRecord : masterMemberRecords) {
            selectedDateTempRecords.add(new TemperatureRecord(memberRecord)); // Map to temperature record given member without submission date and temperature
        }

        // Filter only temperature records matching the current selected date view
        Integer daysToAdd = -(this.currentSelectedChartIndex+1-MAX_CHARTS_TO_BE_DISPLAYED); // Calculate based on chart selection event
        if (providedIndex != null) {
            daysToAdd = -(providedIndex+1-MAX_CHARTS_TO_BE_DISPLAYED); // Calculate based on provided index
        }
        Date selectedChartDate = DateUtils.addDays(new Date(), daysToAdd);
        for (TemperatureRecord tempRecord : masterMemberTemperatureRecords) {
            // Filter through master list to find only those that has submitted to have a checked symbol
            if (DateUtils.isSameDay(tempRecord.getSubmissionDate().toDate(), selectedChartDate)) {
                selectedDateSubmittedTempRecords.add(tempRecord);
            }
        }

        for (TemperatureRecord record : selectedDateSubmittedTempRecords) {
            TemperatureRecord copy = new TemperatureRecord(record);
            copy.setSubmissionDate(null);
            copy.setTemperature(null);
            copy.setId(null);
            Integer index = selectedDateTempRecords.indexOf(copy);
            if (index != -1) {
                TemperatureRecord submittedRecord = selectedDateTempRecords.get(index);
                submittedRecord.setTemperature(record.getTemperature());
                submittedRecord.setSubmissionDate(record.getSubmissionDate());
            }
        }
        return selectedDateTempRecords;
    }
}
