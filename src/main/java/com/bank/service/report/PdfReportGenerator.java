package com.bank.service.report;

import com.bank.dto.response.UserReportDto;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class PdfReportGenerator implements ReportGenerator {

    @Override
    public String getType() {
        return "pdf";
    }

    @Override
    public byte[] generate(List<UserReportDto> users) {
        log.info("Generating PDF report for {} users", users.size());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Альбомная ориентация
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {5, 15, 10, 15, 12, 10, 10, 10};
            table.setWidths(columnWidths);

            addTableHeader(table);

            for (UserReportDto user : users) {
                addUserRow(table, user);
            }

            document.add(table);

            addFooter(document);

            document.close();

            log.info("PDF report generated successfully, size: {} bytes", out.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getFileExtension() {
        return ".pdf";
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Отчёт по пользователям", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
    }

    private void addTableHeader(PdfPTable table) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        String[] headers = {"ID", "Имя", "Дата рождения", "Emails", "Телефоны", "Баланс", "Нач. баланс"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addUserRow(PdfPTable table, UserReportDto user) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        table.addCell(new Phrase(String.valueOf(user.getUserId()), normalFont));
        table.addCell(new Phrase(truncate(user.getName(), 30), normalFont));
        table.addCell(new Phrase(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "", normalFont));
        table.addCell(new Phrase(truncate(user.getEmails(), 40), normalFont));
        table.addCell(new Phrase(truncate(user.getPhones(), 20), normalFont));
        table.addCell(new Phrase(user.getBalance() != null ? user.getBalance().toString() : "0", normalFont));
        table.addCell(new Phrase(user.getInitialBalance() != null ? user.getInitialBalance().toString() : "0", normalFont));
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
        String footerText = "Дата генерации: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        Paragraph footer = new Paragraph(footerText, footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}