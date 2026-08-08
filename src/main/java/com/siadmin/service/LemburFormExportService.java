package com.siadmin.service;

import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class LemburFormExportService {

    public void tulisFormLembur(Karyawan karyawan, List<Lembur> daftarLembur, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Form Lembur");

            Row judul = sheet.createRow(0);
            judul.createCell(0).setCellValue("FORM LEMBUR");

            Row baris1 = sheet.createRow(2);
            baris1.createCell(0).setCellValue("Nama");
            baris1.createCell(1).setCellValue(karyawan.getNamaLengkap());
            baris1.createCell(3).setCellValue("No. ID");
            baris1.createCell(4).setCellValue(karyawan.getNip());
            baris1.createCell(6).setCellValue("Departemen");
            baris1.createCell(7).setCellValue(karyawan.getDepartemen());

            Row header = sheet.createRow(4);
            String[] kolom = {"Tanggal", "Waktu Mulai Lembur", "Waktu Selesai Lembur", "Jumlah Jam Lembur",
                    "Alasan Lembur", "TTD Karyawan", "TTD Atasan Langsung", "TTD Supervisor"};
            for (int i = 0; i < kolom.length; i++) {
                header.createCell(i).setCellValue(kolom[i]);
            }

            int rowIdx = 5;
            for (Lembur l : daftarLembur) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getTanggal().toString());
                row.createCell(1).setCellValue(l.getJamMulai().toString());
                row.createCell(2).setCellValue(l.getJamSelesai().toString());
                row.createCell(3).setCellValue(l.getJumlahJam());
                row.createCell(4).setCellValue(l.getAlasan());
                row.createCell(5).setCellValue("");
                row.createCell(6).setCellValue("");
                row.createCell(7).setCellValue("");
            }

            for (int i = 0; i < kolom.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }
}
