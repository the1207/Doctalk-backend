package com.Doctalk.Doctalk_backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PdfParsingService {

    public String extractText(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String lowerFilename = filename != null ? filename.toLowerCase() : "";

        try {
            if (isPdf(contentType, lowerFilename)) {
                return extractPdfText(file);
            }
            if (isDocx(contentType, lowerFilename)) {
                return extractDocxText(file);
            }
            if (isDoc(contentType, lowerFilename)) {
                return extractDocText(file);
            }
            if (isText(contentType, lowerFilename)) {
                return extractTextFile(file);
            }
            throw new RuntimeException("Type de fichier non supporté : " + contentType + " / " + filename);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'extraction du texte du fichier", e);
        }
    }

    private boolean isPdf(String contentType, String filename) {
        return "application/pdf".equalsIgnoreCase(contentType)
                || filename.endsWith(".pdf");
    }

    private boolean isDocx(String contentType, String filename) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType)
                || filename.endsWith(".docx");
    }

    private boolean isDoc(String contentType, String filename) {
        return "application/msword".equalsIgnoreCase(contentType)
                || filename.endsWith(".doc");
    }

    private boolean isText(String contentType, String filename) {
        return (contentType != null && contentType.startsWith("text/"))
                || filename.endsWith(".txt");
    }

    private String extractPdfText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocxText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDocText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFile(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
