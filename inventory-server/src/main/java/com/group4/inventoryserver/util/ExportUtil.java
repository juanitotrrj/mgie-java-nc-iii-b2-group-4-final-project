package com.group4.inventoryserver.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExportUtil {

  private ExportUtil() {}

  public static byte[] toCsv(String[] headers, List<String[]> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append(escapeCsvRow(headers)).append('\n');
    for (String[] row : rows) {
      sb.append(escapeCsvRow(row)).append('\n');
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] toXlsx(String sheetName, String[] headers, List<String[]> rows)
      throws IOException {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet(sheetName);

      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFont(headerFont);

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
        Row row = sheet.createRow(rowIdx + 1);
        String[] values = rows.get(rowIdx);
        for (int colIdx = 0; colIdx < values.length; colIdx++) {
          Cell cell = row.createCell(colIdx);
          setCellValue(cell, values[colIdx]);
        }
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    }
  }

  private static String escapeCsvRow(String[] fields) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(escapeCsvField(fields[i] != null ? fields[i] : ""));
    }
    return sb.toString();
  }

  private static String escapeCsvField(String field) {
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
  }

  private static void setCellValue(Cell cell, String value) {
    if (value == null || value.isEmpty()) {
      cell.setCellValue("");
      return;
    }
    try {
      double num = Double.parseDouble(value);
      cell.setCellValue(num);
    } catch (NumberFormatException e) {
      cell.setCellValue(value);
    }
  }
}
