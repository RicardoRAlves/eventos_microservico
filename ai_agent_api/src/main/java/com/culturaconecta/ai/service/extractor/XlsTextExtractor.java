
package com.culturaconecta.ai.service.extractor;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class XlsTextExtractor {

    private final SpreadsheetTextExtractor spreadsheetTextExtractor;

    public XlsTextExtractor(
            SpreadsheetTextExtractor spreadsheetTextExtractor) {

        this.spreadsheetTextExtractor = spreadsheetTextExtractor;
    }

    public String extract(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "A planilha XLS é obrigatória.");
        }

        try (Workbook workbook =
                     new HSSFWorkbook(file.getInputStream())) {

            return spreadsheetTextExtractor.extract(workbook);

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Não foi possível processar " +
                            "a planilha XLS.", e);
        }
    }
}
