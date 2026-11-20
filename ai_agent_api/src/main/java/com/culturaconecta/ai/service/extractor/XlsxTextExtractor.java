
package com.culturaconecta.ai.service.extractor;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class XlsxTextExtractor {

    private final XlsxDateNormalizer dateNormalizer;
    private final SpreadsheetTextExtractor spreadsheetTextExtractor;

    public XlsxTextExtractor(
            XlsxDateNormalizer dateNormalizer,
            SpreadsheetTextExtractor spreadsheetTextExtractor) {

        this.dateNormalizer = dateNormalizer;
        this.spreadsheetTextExtractor = spreadsheetTextExtractor;
    }

    public String extract(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "A planilha XLSX é obrigatória.");
        }

        try {
            byte[] normalizedBytes =
                    dateNormalizer.normalize(file.getBytes());

            try (Workbook workbook = new XSSFWorkbook(
                    new ByteArrayInputStream(normalizedBytes))) {

                return spreadsheetTextExtractor.extract(workbook);
            }

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Não foi possível processar " +
                            "a planilha XLSX.", e);
        }
    }
}
