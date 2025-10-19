package com.mysearchapp.MySearchApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

import com.mysearchapp.MySearchApp.util.ExcelReader;

public class MainController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ObservableList<String>> resultTable;

    @FXML
    private Label fileLabel;

    private File selectedFile;

    @FXML
    protected void initialize() {
        // Nothing to initialize at startup for dynamic columns
    }

    @FXML
	protected void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx")
        );

        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedFile = file;
            fileLabel.setText("Selected file: " + selectedFile.getName());
        } else {
            fileLabel.setText("No file selected.");
        }
    }

    @FXML
    private void handleSearch() {
        if (selectedFile == null) {
            fileLabel.setText("Please select an Excel file first.");
            return;
        }

        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            fileLabel.setText("Please enter a search string.");
            return;
        }

        List<String[]> rawResults = ExcelReader.searchExcelForTable(selectedFile, query);

        // Clear previous columns
        resultTable.getColumns().clear();

        // If no results, show empty
        if (rawResults.isEmpty()) {
            resultTable.setItems(FXCollections.emptyObservableList());
            fileLabel.setText("No matches found.");
            return;
        }

        int columnCount = rawResults.get(0).length;

        // Create dynamic columns
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column " + (i + 1));
            column.setCellValueFactory(cellData -> {
                ObservableList<String> row = cellData.getValue();
                String value = (colIndex < row.size()) ? row.get(colIndex) : "";
                return new javafx.beans.property.SimpleStringProperty(value);
            });
            resultTable.getColumns().add(column);
        }

        // Fill the table with rows
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        for (String[] row : rawResults) {
            ObservableList<String> obsRow = FXCollections.observableArrayList(row);
            tableData.add(obsRow);
        }

        resultTable.setItems(tableData);
        fileLabel.setText("Found " + tableData.size() + " matching row(s).");
    }
}
