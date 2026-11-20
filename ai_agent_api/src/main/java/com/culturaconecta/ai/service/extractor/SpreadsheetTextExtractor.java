package com.culturaconecta.ai.service.extractor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpreadsheetTextExtractor {

    private static final int MAX_ROWS = 10_000;
    private static final int MAX_COLUMNS = 100;
    private static final int MAX_SHEETS = 20;
    private static final int MAX_EXTRACTED_CHARS = 1_000_000;

    public String extract(Workbook workbook) {

        if (workbook.getNumberOfSheets() > MAX_SHEETS) {
            throw new IllegalArgumentException(
                    "A planilha ultrapassa o limite de abas.");
        }

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator =
                workbook.getCreationHelper()
                        .createFormulaEvaluator();

        StringBuilder result = new StringBuilder();

        for (Sheet sheet : workbook) {

            if (sheet.getPhysicalNumberOfRows() == 0) {
                continue;
            }

            if (sheet.getLastRowNum() >= MAX_ROWS) {
                throw new IllegalArgumentException(
                        "A aba '" + sheet.getSheetName() +
                                "' ultrapassa o limite de linhas.");
            }

            Row headerRow = sheet.getRow(
                    sheet.getFirstRowNum());

            if (headerRow == null) {
                continue;
            }

            int columnCount = headerRow.getLastCellNum();

            if (columnCount <= 0) {
                continue;
            }

            if (columnCount > MAX_COLUMNS) {
                throw new IllegalArgumentException(
                        "A aba '" + sheet.getSheetName() +
                                "' ultrapassa o limite de colunas.");
            }

            List<String> headers = new ArrayList<>();

            for (int col = 0; col < columnCount; col++) {

                String header = getCellValue(
                        headerRow.getCell(col),
                        formatter,
                        evaluator
                );

                headers.add(
                        header.isBlank()
                                ? "Coluna " + (col + 1)
                                : header
                );
            }

            appendChecked(
                    result,
                    "Aba: " + sheet.getSheetName() + "\n"
            );

            appendChecked(
                    result,
                    "Cabeçalhos: " +
                            String.join(" | ", headers) +
                            "\n\n"
            );

            for (int rowIndex =
                 headerRow.getRowNum() + 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                StringBuilder record =
                        new StringBuilder();

                record.append("Aba: ")
                        .append(sheet.getSheetName())
                        .append(" | Linha: ")
                        .append(rowIndex + 1)
                        .append("\n");

                boolean hasContent = false;

                for (int col = 0;
                     col < columnCount;
                     col++) {

                    String value = getCellValue(
                            row.getCell(col),
                            formatter,
                            evaluator
                    );

                    if (value.isBlank()) {
                        continue;
                    }

                    hasContent = true;

                    record.append(headers.get(col))
                            .append(": ")
                            .append(value)
                            .append("\n");
                }

                if (hasContent) {
                    appendChecked(
                            result,
                            record + "\n"
                    );
                }
            }
        }

        String text = result.toString().strip();

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "Não foi possível extrair dados da planilha.");
        }

        return text;
    }

    private String getCellValue(
            Cell cell,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {

        if (cell == null) {
            return "";
        }

        try {
            return formatter
                    .formatCellValue(cell, evaluator)
                    .strip();

        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Erro ao interpretar uma célula " +
                            "da planilha.", e);
        }
    }

    private void appendChecked(
            StringBuilder builder,
            String value) {

        if ((long) builder.length() + value.length()
                > MAX_EXTRACTED_CHARS) {

            throw new IllegalArgumentException(
                    "O texto extraído ultrapassa " +
                            "o limite de 1 milhão de caracteres.");
        }

        builder.append(value);
    }
}
