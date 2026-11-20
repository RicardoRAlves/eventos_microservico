
package com.culturaconecta.ai.service.extractor;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocxTextExtractor {

    public String extract(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "O arquivo DOCX é obrigatório.");
        }

        try (XWPFDocument document =
                     new XWPFDocument(file.getInputStream())) {

            List<String> blocks = new ArrayList<>();

            for (IBodyElement element : document.getBodyElements()) {

                if (element instanceof XWPFParagraph paragraph) {
                    String text = paragraph.getText();

                    if (text != null && !text.isBlank()) {
                        blocks.add(text.strip());
                    }

                } else if (element instanceof XWPFTable table) {

                    for (XWPFTableRow row : table.getRows()) {
                        List<String> cells = new ArrayList<>();

                        for (XWPFTableCell cell : row.getTableCells()) {
                            cells.add(
                                    cell.getText()
                                            .replaceAll("\\s+", " ")
                                            .strip()
                            );
                        }

                        blocks.add(String.join(" | ", cells));
                    }
                }
            }

            String text = String.join("\n", blocks).strip();

            if (text.isBlank()) {
                throw new IllegalArgumentException(
                        "Não foi possível extrair texto do DOCX. " +
                                "O documento pode estar vazio ou conter " +
                                "somente imagens.");
            }

            return text;

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Não foi possível processar o arquivo DOCX.", e);
        }
    }
}
