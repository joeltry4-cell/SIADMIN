package com.siadmin.service;

import com.siadmin.dto.RekapBulanan;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExcelExportService {

    public void tulisRekapBulanan(List<RekapBulanan> data, YearMonth periode, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Laporan Bulanan " + periode);
            Row header = sheet.createRow(0);
            String[] kolom = {"NIP", "Nama", "Departemen", "Hadir", "Izin", "Sakit", "Cuti", "Alpa"};
            for (int i = 0; i < kolom.length; i++) {
                header.createCell(i).setCellValue(kolom[i]);
            }

            int rowIdx = 1;
            for (RekapBulanan r : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.karyawan().getNip());
                row.createCell(1).setCellValue(r.karyawan().getNamaLengkap());
                row.createCell(2).setCellValue(r.karyawan().getDepartemen());
                row.createCell(3).setCellValue(r.hadir());
                row.createCell(4).setCellValue(r.izin());
                row.createCell(5).setCellValue(r.sakit());
                row.createCell(6).setCellValue(r.cuti());
                row.createCell(7).setCellValue(r.alpa());
            }

            for (int i = 0; i < kolom.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }
}
