package com.mysearchapp.MySearchApp.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelReader {

    public static List<String[]> searchExcelForTable(File file, String searchKey) {
        List<String[]> results = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;

            if (file.getName().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getName().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + file.getName());
            }

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);

                for (Row row : sheet) {
                    boolean matchFound = false;
                    List<String> rowData = new ArrayList<>();

                    for (Cell cell : row) {
                        String cellValue = getCellStringValue(cell);
                        rowData.add(cellValue);

                        if (!matchFound && cellValue != null && !cellValue.isEmpty()) {
                            if (cellValue.equalsIgnoreCase(searchKey) ||
                                cellValue.toLowerCase().contains(searchKey.toLowerCase())) {
                                matchFound = true;
                            }
                        }
                    }

                    if (matchFound) {
                        results.add(rowData.toArray(new String[0]));
                    }
                }
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }
}
