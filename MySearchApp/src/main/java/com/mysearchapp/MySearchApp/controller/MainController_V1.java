package com.mysearchapp.MySearchApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.util.List;

import com.mysearchapp.MySearchApp.util.ExcelReader;

public class MainController_V1 extends MainController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ObservableList<String>> resultTable;

    @FXML
    private Label fileLabel;

    @FXML
    private Label statusLabel;

    private File selectedFile;

    @FXML
	protected void initialize() {
        fileLabel.setText("No file selected.");
        statusLabel.setText("Ready.");

        // Enable Excel-like selection behavior
        resultTable.getSelectionModel().setCellSelectionEnabled(true);
        resultTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add key listener for Ctrl+C copy functionality
        resultTable.setOnKeyPressed(this::handleCopyShortcut);
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
            statusLabel.setText("File loaded successfully.");
        } else {
            statusLabel.setText("No file selected.");
        }
    }

    @FXML
    private void handleSearch() {
        if (selectedFile == null) {
            statusLabel.setText("Please select an Excel file first.");
            return;
        }

        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            statusLabel.setText("Please enter a search string.");
            return;
        }

        List<String[]> rawResults = ExcelReader.searchExcelForTable(selectedFile, query);
        resultTable.getColumns().clear();

        if (rawResults.isEmpty()) {
            resultTable.setItems(FXCollections.emptyObservableList());
            statusLabel.setText("No matches found.");
            return;
        }

        int columnCount = rawResults.get(0).length;
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column =
                new TableColumn<>("Column " + (i + 1));
            column.setCellValueFactory(cellData -> {
                ObservableList<String> row = cellData.getValue();
                String value = (colIndex < row.size()) ? row.get(colIndex) : "";
                return new javafx.beans.property.SimpleStringProperty(value);
            });
            resultTable.getColumns().add(column);
        }

        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
        for (String[] row : rawResults) {
            tableData.add(FXCollections.observableArrayList(row));
        }

        resultTable.setItems(tableData);
        statusLabel.setText("Found " + tableData.size() + " matching row(s).");
    }

    @FXML
    private void handleReset() {
        resultTable.getItems().clear();
        statusLabel.setText("Results cleared. Ready for new search.");
    }

    @FXML
    private void handleCopyShortcut(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            copySelectedCells();
        }
    }

    @FXML
    private void handleCopy() {
        copySelectedCells();
    }

    private void copySelectedCells() {
        ObservableList<TablePosition> selectedCells = resultTable.getSelectionModel().getSelectedCells();

        if (selectedCells == null || selectedCells.isEmpty()) {
            statusLabel.setText("No cells selected to copy.");
            return;
        }

        StringBuilder clipboardString = new StringBuilder();

        int prevRow = -1;
        for (TablePosition<?, ?> pos : selectedCells) {
            int row = pos.getRow();
            int col = pos.getColumn();
            Object cell = resultTable.getColumns().get(col)
                    .getCellData(row);

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell == null ? "" : cell.toString());
            prevRow = row;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("Copied " + selectedCells.size() + " cell(s) to clipboard.");
    }
}
