
package com.culturaconecta.ai.service;

import com.culturaconecta.ai.entity.KnowledgeVisibility;
import com.culturaconecta.ai.service.extractor.DocxTextExtractor;
import com.culturaconecta.ai.service.extractor.PdfTextExtractor;

import com.culturaconecta.ai.service.extractor.XlsTextExtractor;
import com.culturaconecta.ai.service.extractor.XlsxTextExtractor;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class KnowledgeFileIngestionService {

    private static final long MAX_FILE_SIZE =
            10L * 1024 * 1024;

    private final PdfTextExtractor pdfTextExtractor;
    private final DocxTextExtractor docxTextExtractor;
    private final XlsxTextExtractor xlsxTextExtractor;
    private final XlsTextExtractor xlsTextExtractor;
    private final KnowledgeIngestionService ingestionService;

    public Long ingestFile(
            String organizationKey,
            MultipartFile file,
            KnowledgeVisibility visibility) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Arquivo obrigatório.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "O arquivo não pode ultrapassar 10 MB.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome do arquivo obrigatório.");
        }

        String normalizedFilename =
                filename.toLowerCase(Locale.ROOT);


        String text;

        if (normalizedFilename.endsWith(".pdf")) {

            text = pdfTextExtractor.extract(file);

        } else if (normalizedFilename.endsWith(".docx")) {

            text = docxTextExtractor.extract(file);

        } else if (normalizedFilename.endsWith(".xlsx")) {

            text = xlsxTextExtractor.extract(file);

        } else if (normalizedFilename.endsWith(".xls")) {

            text = xlsTextExtractor.extract(file);

        } else {
            throw new IllegalArgumentException(
                    "Formato não suportado. " +
                            "Envie PDF, DOCX, XLSX ou XLS.");
        }

        return ingestionService.ingest(
                organizationKey,
                filename,
                text,
                visibility
        );
    }
}
