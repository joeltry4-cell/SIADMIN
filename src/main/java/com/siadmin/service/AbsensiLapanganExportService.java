package com.siadmin.service;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AbsensiLapanganExportService {

    private static final DateTimeFormatter TGL = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int KOLOM_TETAP = 6;

    public void tulisAbsensiMingguan(List<Karyawan> karyawanAktif, LocalDate awal, LocalDate akhir,
                                      Map<Long, List<Absensi>> absensiPerKaryawan,
                                      Map<Long, List<Lembur>> lemburPerKaryawan,
                                      Map<Long, byte[]> tandaTanganPerKaryawan,
                                      OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle judulStyle = buatStyleJudul(workbook);
            CellStyle infoStyle = buatStyleInfo(workbook);
            CellStyle headerStyle = buatStyleHeader(workbook);

            Map<String, List<Karyawan>> perDivisi = new TreeMap<>();
            for (Karyawan k : karyawanAktif) {
                String divisi = (k.getDepartemen() == null || k.getDepartemen().isBlank()) ? "Tanpa Divisi" : k.getDepartemen();
                perDivisi.computeIfAbsent(divisi, x -> new ArrayList<>()).add(k);
            }

            List<LocalDate> tanggalList = new ArrayList<>();
            for (LocalDate t = awal; !t.isAfter(akhir); t = t.plusDays(1)) {
                tanggalList.add(t);
            }
            int totalKolom = KOLOM_TETAP + tanggalList.size() * 4;

            for (Map.Entry<String, List<Karyawan>> entry : perDivisi.entrySet()) {
                Sheet sheet = workbook.createSheet(ExcelSheetNames.sanitasi(entry.getKey()));
                Drawing<?> drawing = sheet.createDrawingPatriarch();

                Row judul = sheet.createRow(0);
                judul.setHeightInPoints(30);
                Cell judulCell = judul.createCell(0);
                judulCell.setCellValue("Absensi Karyawan Lapangan\n员工现场考勤表");
                judulCell.setCellStyle(judulStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalKolom - 1));

                Row info1 = sheet.createRow(1);
                setInfoCell(info1, 0, "PT 公司 : TTRI", infoStyle);
                setInfoCell(info1, 4, "Divisi 车间 : " + entry.getKey(), infoStyle);

                Row info2 = sheet.createRow(2);
                setInfoCell(info2, 0, "Departement 部门 : " + entry.getKey(), infoStyle);
                setInfoCell(info2, 4, "Periode 周期 : " + awal.format(TGL) + " - " + akhir.format(TGL), infoStyle);

                int headerRow1Idx = 4;
                int headerRow2Idx = 5;
                Row h1 = sheet.createRow(headerRow1Idx);
                Row h2 = sheet.createRow(headerRow2Idx);
                h1.setHeightInPoints(30);
                h2.setHeightInPoints(30);

                String[] kolomTetapLabel = {"NO\n序号", "NO ID\n工号", "NAMA\n姓名",
                        "Jabatan\n岗位", "Shift\n倒班情况", "Tim"};
                for (int i = 0; i < kolomTetapLabel.length; i++) {
                    Cell c1 = h1.createCell(i);
                    c1.setCellValue(kolomTetapLabel[i]);
                    c1.setCellStyle(headerStyle);
                    Cell c2 = h2.createCell(i);
                    c2.setCellStyle(headerStyle);
                    sheet.addMergedRegion(new CellRangeAddress(headerRow1Idx, headerRow2Idx, i, i));
                }

                int col = KOLOM_TETAP;
                String[] subLabel = {"Mulai\n上班", "Selesai\n下班", "Jumlah Jam Lembur\n加班小时", "TTD\n签字"};
                for (LocalDate t : tanggalList) {
                    Cell tglCell = h1.createCell(col);
                    tglCell.setCellValue(t.format(TGL));
                    tglCell.setCellStyle(headerStyle);
                    sheet.addMergedRegion(new CellRangeAddress(headerRow1Idx, headerRow1Idx, col, col + 3));

                    for (int i = 0; i < subLabel.length; i++) {
                        Cell sub = h2.createCell(col + i);
                        sub.setCellValue(subLabel[i]);
                        sub.setCellStyle(headerStyle);
                    }
                    col += 4;
                }

                int rowIdx = headerRow2Idx + 1;
                int no = 1;
                for (Karyawan k : entry.getValue()) {
                    Row row = sheet.createRow(rowIdx);
                    int c = 0;
                    row.createCell(c++).setCellValue(no++);
                    row.createCell(c++).setCellValue(k.getNip());
                    row.createCell(c++).setCellValue(k.getNamaLengkap());
                    row.createCell(c++).setCellValue(k.getJabatan());
                    row.createCell(c++).setCellValue(k.getShift() != null ? k.getShift() : "");
                    row.createCell(c++).setCellValue(k.getTim() != null ? k.getTim() : "");

                    Map<LocalDate, Absensi> absensiPerTanggal = new LinkedHashMap<>();
                    for (Absensi a : absensiPerKaryawan.getOrDefault(k.getId(), List.of())) {
                        absensiPerTanggal.put(a.getTanggal(), a);
                    }
                    Map<LocalDate, Double> lemburPerTanggal = new LinkedHashMap<>();
                    for (Lembur l : lemburPerKaryawan.getOrDefault(k.getId(), List.of())) {
                        lemburPerTanggal.merge(l.getTanggal(), l.getJumlahJam(), Double::sum);
                    }
                    byte[] ttd = tandaTanganPerKaryawan.get(k.getId());

                    for (LocalDate t : tanggalList) {
                        Absensi a = absensiPerTanggal.get(t);
                        row.createCell(c).setCellValue(a != null && a.getJamMasuk() != null ? a.getJamMasuk().toString() : "");
                        c++;
                        row.createCell(c).setCellValue(a != null && a.getJamKeluar() != null ? a.getJamKeluar().toString() : "");
                        c++;
                        Double jamLembur = lemburPerTanggal.get(t);
                        row.createCell(c).setCellValue(jamLembur != null ? jamLembur : 0);
                        c++;
                        row.createCell(c);
                        if (a != null && ttd != null) {
                            ExcelSignatures.tempel(sheet, drawing, rowIdx, c, ttd);
                        }
                        c++;
                    }
                    rowIdx++;
                }

                Row footer1 = sheet.createRow(rowIdx + 1);
                footer1.createCell(0).setCellValue("TTD Petugas Absensi Lapangan\n现场考员签字:");
                Row footer2 = sheet.createRow(rowIdx + 2);
                footer2.createCell(0).setCellValue("TTD Supervisor Divisi\n车间主任签字:");

                for (int i = 0; i < totalKolom; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
        }
    }

    private void setInfoCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
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

    private CellStyle buatStyleInfo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
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
}
