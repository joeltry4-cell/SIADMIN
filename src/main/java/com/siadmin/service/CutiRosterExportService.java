package com.siadmin.service;

import com.siadmin.model.Cuti;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusCuti;
import com.siadmin.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CutiRosterExportService {

    private final KaryawanService karyawanService;
    private final UserService userService;

    public CutiRosterExportService(KaryawanService karyawanService, UserService userService) {
        this.karyawanService = karyawanService;
        this.userService = userService;
    }

    public void tulisRosterCuti(List<Karyawan> karyawanAktif, YearMonth bulan,
                                 Map<Long, List<Cuti>> cutiPerKaryawan,
                                 Map<Long, byte[]> tandaTanganPerKaryawan,
                                 OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle judulStyle = buatStyleJudul(workbook);
            CellStyle headerStyle = buatStyleHeader(workbook);

            Map<String, List<Karyawan>> perTim = new TreeMap<>();
            for (Karyawan k : karyawanAktif) {
                String tim = (k.getTim() == null || k.getTim().isBlank()) ? "Tanpa Tim" : k.getTim();
                perTim.computeIfAbsent(tim, x -> new ArrayList<>()).add(k);
            }

            String[] kolom = {"NO\n序号", "NIK\n工号", "NAMA \n姓名", "JABATAN", "JOIN DATE\n入职时间",
                    "CUTI SEBELUMYA\n上次休假时间", "SELESAI CUTI\n休假结束", "EFEKTIF BEKERJA SETELAH CUTI\n上次休假结束开始工作时间",
                    "CUTI SEBENARNYA (KALI INI)\n本次休假时间", "IZIN DAN SAKIT", "HASIL", "TIM", "DIVISI",
                    "TGL CUTI ", "JENIS CUTI ", "TTD KARYAWAN "};

            for (Map.Entry<String, List<Karyawan>> entry : perTim.entrySet()) {
                Sheet sheet = workbook.createSheet(ExcelSheetNames.sanitasi(entry.getKey()));
                Drawing<?> drawing = sheet.createDrawingPatriarch();

                Row judul = sheet.createRow(0);
                judul.setHeightInPoints(30);
                Cell judulCell = judul.createCell(0);
                judulCell.setCellValue("LIST PENGAJUAN CUTI BULAN " + String.format("%02d", bulan.getMonthValue())
                        + " TAHUN " + bulan.getYear() + "\nDEPT. INSPEKSI HIDROMETALURGI TTRI");
                judulCell.setCellStyle(judulStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, kolom.length - 1));

                int headerRowIdx = 2;
                Row header = sheet.createRow(headerRowIdx);
                header.setHeightInPoints(30);
                for (int i = 0; i < kolom.length; i++) {
                    Cell c = header.createCell(i);
                    c.setCellValue(kolom[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Karyawan> daftar = entry.getValue().stream()
                        .sorted(Comparator.comparing(Karyawan::getNamaLengkap))
                        .toList();

                int rowIdx = headerRowIdx + 1;
                int no = 1;
                User approverUntukFooter = null;
                for (Karyawan k : daftar) {
                    List<Cuti> daftarCuti = cutiPerKaryawan.getOrDefault(k.getId(), List.of());

                    Cuti terakhirDisetujui = daftarCuti.stream()
                            .filter(c -> c.getStatus() == StatusCuti.DISETUJUI)
                            .max(Comparator.comparing(Cuti::getTanggalSelesai))
                            .orElse(null);

                    LocalDate cutiSebelumnya = terakhirDisetujui != null ? terakhirDisetujui.getTanggalMulai() : k.getTanggalMasuk();
                    LocalDate selesaiCuti = terakhirDisetujui != null ? terakhirDisetujui.getTanggalSelesai() : k.getTanggalMasuk();
                    LocalDate efektifBekerja = k.getTanggalMulaiSiklusCuti() != null ? k.getTanggalMulaiSiklusCuti() : k.getTanggalMasuk();
                    LocalDate cutiBerikutnya = karyawanService.jadwalCutiBerikutnya(k).orElse(null);

                    Cuti bulanIni = daftarCuti.stream()
                            .filter(c -> YearMonth.from(c.getTanggalMulai()).equals(bulan))
                            .findFirst()
                            .orElse(null);

                    Row row = sheet.createRow(rowIdx);
                    int col = 0;
                    row.createCell(col++).setCellValue(no++);
                    row.createCell(col++).setCellValue(k.getNip());
                    row.createCell(col++).setCellValue(k.getNamaLengkap());
                    row.createCell(col++).setCellValue(k.getJabatan());
                    row.createCell(col++).setCellValue(k.getTanggalMasuk().toString());
                    row.createCell(col++).setCellValue(cutiSebelumnya.toString());
                    row.createCell(col++).setCellValue(selesaiCuti.toString());
                    row.createCell(col++).setCellValue(efektifBekerja.toString());
                    row.createCell(col++).setCellValue(cutiBerikutnya != null ? cutiBerikutnya.toString() : "");
                    row.createCell(col++).setCellValue("");
                    row.createCell(col++).setCellValue("");
                    row.createCell(col++).setCellValue(k.getTim() != null ? k.getTim() : "");
                    row.createCell(col++).setCellValue(k.getDepartemen());
                    row.createCell(col++).setCellValue(bulanIni != null ? bulanIni.getTanggalMulai().toString() : "");
                    row.createCell(col++).setCellValue(bulanIni != null ? bulanIni.getJenis().toString() : "");
                    row.createCell(col);
                    byte[] ttdKaryawan = tandaTanganPerKaryawan.get(k.getId());
                    if (ttdKaryawan != null) {
                        ExcelSignatures.tempel(sheet, drawing, rowIdx, col, ttdKaryawan);
                    }

                    if (approverUntukFooter == null && bulanIni != null && bulanIni.getDisetujuiOleh() != null) {
                        approverUntukFooter = bulanIni.getDisetujuiOleh();
                    }

                    rowIdx++;
                }

                int footerRowIdx = rowIdx + 2;
                Row footer1 = sheet.createRow(footerRowIdx);
                footer1.createCell(1).setCellValue("DIBUAT OLEH :");
                footer1.createCell(8).setCellValue("DISETUJUI :");

                if (approverUntukFooter != null) {
                    userService.bacaTandaTangan(approverUntukFooter)
                            .ifPresent(ttd -> ExcelSignatures.tempel(sheet, drawing, footerRowIdx + 1, 8, ttd));
                }

                Row footer2 = sheet.createRow(footerRowIdx + 4);
                footer2.createCell(1).setCellValue("FOREMAN");
                footer2.createCell(8).setCellValue("SPV");

                for (int i = 0; i < kolom.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
        }
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
