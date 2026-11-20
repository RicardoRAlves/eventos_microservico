
package com.culturaconecta.ai.service.extractor;

import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.*;

@Component
public class XlsxDateNormalizer {

    private static final int MAX_ENTRIES = 2_000;
    private static final long MAX_UNCOMPRESSED_BYTES =
            50L * 1024 * 1024;

    private static final String SPREADSHEET_NS =
            "http://schemas.openxmlformats.org/"
                    + "spreadsheetml/2006/main";

    public byte[] normalize(byte[] original) {

        try {
            boolean date1904 = uses1904Dates(original);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Set<String> entryNames = new HashSet<>();

            long totalBytes = 0;
            int entryCount = 0;

            try (ZipInputStream input =
                         new ZipInputStream(
                                 new ByteArrayInputStream(original));
                 ZipOutputStream zipOutput =
                         new ZipOutputStream(output)) {

                ZipEntry entry;

                while ((entry = input.getNextEntry()) != null) {

                    if (++entryCount > MAX_ENTRIES) {
                        throw new IllegalArgumentException(
                                "XLSX com excesso de arquivos internos.");
                    }

                    String name = entry.getName();

                    if (!entryNames.add(name)
                            || name.startsWith("/")
                            || name.contains("..")
                            || name.contains("\\")) {
                        throw new IllegalArgumentException(
                                "Estrutura interna do XLSX inválida.");
                    }

                    ByteArrayOutputStream entryOutput =
                            new ByteArrayOutputStream();

                    byte[] buffer = new byte[8192];
                    int count;

                    while ((count = input.read(buffer)) != -1) {

                        totalBytes += count;

                        if (totalBytes > MAX_UNCOMPRESSED_BYTES) {
                            throw new IllegalArgumentException(
                                    "Conteúdo descompactado do XLSX "
                                            + "ultrapassa o limite.");
                        }

                        entryOutput.write(buffer, 0, count);
                    }

                    byte[] content = entryOutput.toByteArray();

                    if (name.matches(
                            "xl/worksheets/sheet[0-9]+\\.xml")) {

                        content = normalizeSheet(
                                content, date1904);
                    }

                    ZipEntry newEntry = new ZipEntry(name);
                    zipOutput.putNextEntry(newEntry);
                    zipOutput.write(content);
                    zipOutput.closeEntry();

                    input.closeEntry();
                }
            }

            return output.toByteArray();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Não foi possível normalizar o XLSX.", e);
        }
    }

    private byte[] normalizeSheet(
            byte[] xml, boolean date1904) throws Exception {

        Document document = parseXml(xml);

        NodeList cells = document.getElementsByTagNameNS(
                SPREADSHEET_NS, "c");

        boolean changed = false;

        for (int i = 0; i < cells.getLength(); i++) {

            Element cell = (Element) cells.item(i);

            if (!"d".equals(cell.getAttribute("t"))) {
                continue;
            }

            Element valueElement = null;

            for (Node node = cell.getFirstChild();
                 node != null;
                 node = node.getNextSibling()) {

                if (node instanceof Element element
                        && SPREADSHEET_NS.equals(
                        element.getNamespaceURI())
                        && "v".equals(element.getLocalName())) {

                    valueElement = element;
                    break;
                }
            }

            if (valueElement == null) {
                throw new IllegalArgumentException(
                        "Célula de data sem valor: "
                                + cell.getAttribute("r"));
            }

            LocalDateTime dateTime =
                    parseDate(valueElement.getTextContent());

            double excelDate = DateUtil.getExcelDate(
                    dateTime, date1904);

            if (excelDate < 0) {
                throw new IllegalArgumentException(
                        "Data fora do intervalo suportado: "
                                + cell.getAttribute("r"));
            }

            valueElement.setTextContent(
                    Double.toString(excelDate));

            cell.setAttribute("t", "n");
            changed = true;
        }

        return changed ? serializeXml(document) : xml;
    }

    private LocalDateTime parseDate(String value) {

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // Tenta formatos ISO com offset.
        }

        try {
            return OffsetDateTime.parse(value)
                    .toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Tenta data sem horário.
        }

        try {
            return LocalDate.parse(value)
                    .atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data ISO inválida no XLSX: " + value, e);
        }
    }

    private boolean uses1904Dates(byte[] original)
            throws Exception {

        try (ZipInputStream input =
                     new ZipInputStream(
                             new ByteArrayInputStream(original))) {

            ZipEntry entry;

            while ((entry = input.getNextEntry()) != null) {

                if (!"xl/workbook.xml".equals(entry.getName())) {
                    continue;
                }

                byte[] xml = input.readNBytes(
                        (int) MAX_UNCOMPRESSED_BYTES + 1);

                if (xml.length > MAX_UNCOMPRESSED_BYTES) {
                    throw new IllegalArgumentException(
                            "Metadados XLSX excessivamente grandes.");
                }

                Document document = parseXml(xml);

                NodeList properties =
                        document.getElementsByTagNameNS(
                                SPREADSHEET_NS, "workbookPr");

                if (properties.getLength() == 0) {
                    return false;
                }

                String value = ((Element) properties.item(0))
                        .getAttribute("date1904");

                return "1".equals(value)
                        || "true".equalsIgnoreCase(value);
            }
        }

        throw new IllegalArgumentException(
                "XLSX sem xl/workbook.xml.");
    }

    private Document parseXml(byte[] xml) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        factory.setFeature(
                "http://apache.org/xml/features/"
                        + "disallow-doctype-decl", true);

        factory.setFeature(
                "http://xml.org/sax/features/"
                        + "external-general-entities", false);

        factory.setFeature(
                "http://xml.org/sax/features/"
                        + "external-parameter-entities", false);

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, "");

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml));
    }

    private byte[] serializeXml(Document document)
            throws Exception {

        TransformerFactory factory =
                TransformerFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, "");

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer =
                factory.newTransformer();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        transformer.transform(
                new DOMSource(document),
                new StreamResult(output));

        return output.toByteArray();
    }
}

