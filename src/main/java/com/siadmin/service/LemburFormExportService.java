package com.siadmin.service;

import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import com.siadmin.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class LemburFormExportService {

    private final UserService userService;
    private final UserRepository userRepository;

    public LemburFormExportService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public void tulisFormLembur(Karyawan karyawan, List<Lembur> daftarLembur, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Form Lembur");
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            CellStyle judulStyle = buatStyleJudul(workbook);
            CellStyle labelStyle = buatStyleLabel(workbook);
            CellStyle headerStyle = buatStyleHeader(workbook);
            CellStyle keteranganStyle = buatStyleInfo(workbook);

            int kolomTotal = 8;

            Row judul = sheet.createRow(0);
            judul.setHeightInPoints(24);
            Cell judulCell = judul.createCell(0);
            judulCell.setCellValue("FORM LEMBUR\n加班单");
            judulCell.setCellStyle(judulStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, kolomTotal - 1));

            Row baris1 = sheet.createRow(2);
            setLabelValue(baris1, 0, "Nama\n名字", karyawan.getNamaLengkap(), labelStyle);
            setLabelValue(baris1, 3, "No. ID\n工号", karyawan.getNip(), labelStyle);
            setLabelValue(baris1, 6, "Departemen\n部门", karyawan.getDepartemen(), labelStyle);

            int headerRowIdx = 4;
            Row header = sheet.createRow(headerRowIdx);
            header.setHeightInPoints(40);
            String[] kolom = {"Tanggal\n日期", "Waktu Mulai Lembur\n加班开始时间", "Waktu Selesai Lembur\n加班结束时间",
                    "Jumlah Jam Lembur\n加班时长", "Alasan lembur\n加班理由", "TTD Karyawan\n员工本人签字",
                    "TTD\nAtasan Langsung\n直接领导签字", "TTD Supervisor\n车间主任\n签字"};
            for (int i = 0; i < kolom.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(kolom[i]);
                c.setCellStyle(headerStyle);
            }

            byte[] ttdKaryawan = userRepository.findByKaryawan(karyawan)
                    .flatMap(userService::bacaTandaTangan)
                    .orElse(null);

            int rowIdx = headerRowIdx + 1;
            for (Lembur l : daftarLembur) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(l.getTanggal().toString());
                row.createCell(1).setCellValue(l.getJamMulai().toString());
                row.createCell(2).setCellValue(l.getJamSelesai().toString());
                row.createCell(3).setCellValue(l.getJumlahJam());
                row.createCell(4).setCellValue(l.getAlasan());
                row.createCell(5);
                row.createCell(6);
                row.createCell(7);

                if (ttdKaryawan != null) {
                    ExcelSignatures.tempel(sheet, drawing, rowIdx, 5, ttdKaryawan);
                }
                if (l.getDisetujuiOleh() != null) {
                    int baris = rowIdx;
                    userService.bacaTandaTangan(l.getDisetujuiOleh()).ifPresent(ttdApprover -> {
                        ExcelSignatures.tempel(sheet, drawing, baris, 6, ttdApprover);
                        ExcelSignatures.tempel(sheet, drawing, baris, 7, ttdApprover);
                    });
                }
                rowIdx++;
            }

            Row keterangan = sheet.createRow(rowIdx + 1);
            keterangan.createCell(0).setCellValue(
                    "Keterangan： Isi sesuai dengan konten yang terkait, penanggung jawab departemen bertanggung "
                            + "jawab untuk mengawasi pekerjaan. \n注意：请准确填写相应内容， "
                            + "部门负责人负责监督工作。");
            keterangan.getCell(0).setCellStyle(keteranganStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx + 1, rowIdx + 1, 0, kolomTotal - 1));

            for (int i = 0; i < kolomTotal; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private void setLabelValue(Row row, int col, String label, String value, CellStyle style) {
        Cell labelCell = row.createCell(col);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        row.createCell(col + 1).setCellValue(value != null ? value : "");
    }

    private CellStyle buatStyleJudul(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle buatStyleLabel(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

    private CellStyle buatStyleHeader(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buatStyleInfo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }
}
