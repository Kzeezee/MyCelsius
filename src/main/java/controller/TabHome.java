package controller;

import com.google.cloud.Timestamp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import model.TemperatureRecord;
import util.TemperatureRecordCell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabHome extends AnchorPane {

    @FXML
    BorderPane memberListContainer;
    @FXML
    HBox chartHBox;
    @FXML
    Label chartCaption;

    private Integer currentSelectedChartIndex = 0;
    private PieChart currentSelectedChart = null;
    private static final Double CAPTION_OFFSET = 250.0;

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
        fillChartHBox();
        displaySubmissionRecords(Timestamp.now());
    }

    // Query temperature records from past 5 days including today and fill 5 pie charts
    private void fillChartHBox() {
        List<PieChart> charts = new ArrayList<>();
        Integer maxCharts = 5;
        for (int i = 0; i < maxCharts; i++) {
            // Filling data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Submitted", 13),
                    new PieChart.Data("Not submitted", 25));

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
                            chartCaption.setText(String.valueOf(data.getName() + ": " + data.getPieValue()));
                        }
                );
                data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                        event -> chartCaption.setVisible(false)
                );
            }

            // Last few configs
            chart.setTitle("Tuesday");
            chart.setLabelsVisible(false);
            chart.setAnimated(true);
            chart.setCursor(Cursor.HAND);
            chart.setStyle("-fx-background-color: #f2f2f2");
            chart.setOnMouseClicked(event -> {
                // Handle showing temperature records for this specific day
                chartHBox.getChildren().get(this.currentSelectedChartIndex).setStyle("-fx-background-color: #f2f2f2");
                this.currentSelectedChartIndex = chartHBox.getChildren().indexOf(chart);
                chart.setStyle("-fx-background-color: #8affff");
                System.out.println(this.currentSelectedChartIndex);
                displaySubmissionRecords(Timestamp.now());
            });
            charts.add(chart);
        }
        // If the HBox is not empty, clear it first.
        if (!chartHBox.getChildren().isEmpty()) {
            chartHBox.getChildren().clear();
        }
        chartHBox.getChildren().addAll(charts);
    }

    // Query temperature submission records for the current date
    private void displaySubmissionRecords(Timestamp date) {
        // Get records from firebase
        List<TemperatureRecord> records = new ArrayList<>();

        // testing
        records.add(new TemperatureRecord("John", "912399123", 35.7, Timestamp.now()));
        records.add(new TemperatureRecord("Jane", "213123154", 38.2, Timestamp.now()));

        // Now display
        ObservableList<TemperatureRecord> list = FXCollections.observableArrayList(records);
        final ListView<TemperatureRecord> lv = new ListView<TemperatureRecord>(list);
        lv.setCellFactory(new Callback<ListView<TemperatureRecord>, ListCell<TemperatureRecord>>() {
            @Override
            public ListCell<TemperatureRecord> call(ListView<TemperatureRecord> param) {
                return new TemperatureRecordCell();
            }
        });
        memberListContainer.setCenter(lv);
    }

    private TemperatureRecord sampleTemp() {
        TemperatureRecord record = new TemperatureRecord();
        return record;
    }
}
