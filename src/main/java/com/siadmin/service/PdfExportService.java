package com.siadmin.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.siadmin.dto.RekapBulanan;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.YearMonth;
import java.util.List;

@Service
public class PdfExportService {

    public void tulisRekapBulanan(List<RekapBulanan> data, YearMonth periode, OutputStream out) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font judulFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font isiFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph judul = new Paragraph("Laporan Bulanan Absensi - " + periode, judulFont);
            judul.setAlignment(Element.ALIGN_CENTER);
            judul.setSpacingAfter(12);
            document.add(judul);

            String[] kolom = {"NIP", "Nama", "Departemen", "Hadir", "Izin", "Sakit", "Cuti", "Alpa"};
            PdfPTable table = new PdfPTable(kolom.length);
            table.setWidthPercentage(100);
            for (String k : kolom) {
                PdfPCell cell = new PdfPCell(new Paragraph(k, headerFont));
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (RekapBulanan r : data) {
                table.addCell(new PdfPCell(new Paragraph(r.karyawan().getNip(), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(r.karyawan().getNamaLengkap(), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(r.karyawan().getDepartemen(), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(r.hadir()), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(r.izin()), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(r.sakit()), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(r.cuti()), isiFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(r.alpa()), isiFont)));
            }

            document.add(table);
        } finally {
            document.close();
        }
    }
}
