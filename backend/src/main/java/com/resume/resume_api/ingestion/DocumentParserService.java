package com.resume.resume_api.ingestion;

import com.resume.resume_api.exception.DocumentParsingException;
import com.resume.resume_api.normalization.TextNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class DocumentParserService {

    /**
     * Extracts text from uploaded PDF, DOCX, or TXT document.
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentParsingException("Uploaded file is empty or missing.");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try (InputStream is = file.getInputStream()) {
            if (filename.endsWith(".pdf")) {
                return extractFromPdf(is);
            } else if (filename.endsWith(".docx")) {
                return extractFromDocx(is);
            } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
                return extractFromPlainText(is);
            } else {
                // Try plain text as fallback
                return extractFromPlainText(is);
            }
        } catch (DocumentParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse document: {}", filename, e);
            throw new DocumentParsingException("Failed to extract text from " + filename + ": " + e.getMessage());
        }
    }

    public String extractFromPdf(InputStream is) {
        try {
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new DocumentParsingException("PDF is password protected / encrypted.");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);
                return TextNormalizer.cleanText(text);
            }
        } catch (Exception e) {
            log.error("PDF extraction error", e);
            throw new DocumentParsingException("Could not extract text from PDF: " + e.getMessage());
        }
    }

    public String extractFromDocx(InputStream is) {
        try (XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            return TextNormalizer.cleanText(text);
        } catch (Exception e) {
            log.error("DOCX extraction error", e);
            throw new DocumentParsingException("Could not extract text from DOCX: " + e.getMessage());
        }
    }

    public String extractFromPlainText(InputStream is) {
        try {
            byte[] bytes = is.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            return TextNormalizer.cleanText(text);
        } catch (Exception e) {
            log.error("Plain text extraction error", e);
            throw new DocumentParsingException("Could not extract plain text: " + e.getMessage());
        }
    }
}
