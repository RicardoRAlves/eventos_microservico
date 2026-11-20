
package com.culturaconecta.ai.service.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PdfTextExtractor {

    public String extract(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "O arquivo PDF é obrigatório.");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {

            if (document.isEncrypted()) {
                throw new IllegalArgumentException(
                        "PDF protegido por senha não é suportado.");
            }

            String text = new PDFTextStripper()
                    .getText(document);

            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "Não foi possível extrair texto do PDF. " +
                                "Somente PDFs com texto selecionável " +
                                "são suportados. PDFs digitalizados " +
                                "ainda não são aceitos.");
            }

            return text.strip();

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo PDF.", e);
        }
    }
}
