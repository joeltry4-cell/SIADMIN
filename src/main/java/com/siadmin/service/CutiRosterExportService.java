package com.siadmin.service;

import com.siadmin.model.Cuti;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusCuti;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CutiRosterExportService {

    private final KaryawanService karyawanService;

    public CutiRosterExportService(KaryawanService karyawanService) {
        this.karyawanService = karyawanService;
    }

    public void tulisRosterCuti(List<Karyawan> karyawanAktif, YearMonth bulan,
                                 Map<Long, List<Cuti>> cutiPerKaryawan, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Map<String, List<Karyawan>> perTim = new TreeMap<>();
            for (Karyawan k : karyawanAktif) {
                String tim = (k.getTim() == null || k.getTim().isBlank()) ? "Tanpa Tim" : k.getTim();
                perTim.computeIfAbsent(tim, x -> new java.util.ArrayList<>()).add(k);
            }

            String[] kolom = {"NO", "NIK", "Nama", "Jabatan", "Join Date", "Cuti Sebelumnya", "Selesai Cuti",
                    "Efektif Bekerja", "Cuti Berikutnya", "Tim", "Divisi", "Tgl Cuti Bulan Ini", "Jenis Cuti", "TTD Karyawan"};

            for (Map.Entry<String, List<Karyawan>> entry : perTim.entrySet()) {
                Sheet sheet = workbook.createSheet(ExcelSheetNames.sanitasi(entry.getKey()));
                Row header = sheet.createRow(0);
                for (int i = 0; i < kolom.length; i++) {
                    header.createCell(i).setCellValue(kolom[i]);
                }

                List<Karyawan> daftar = entry.getValue().stream()
                        .sorted(Comparator.comparing(Karyawan::getNamaLengkap))
                        .toList();

                int rowIdx = 1;
                int no = 1;
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

                    Row row = sheet.createRow(rowIdx++);
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
                    row.createCell(col++).setCellValue(k.getTim() != null ? k.getTim() : "");
                    row.createCell(col++).setCellValue(k.getDepartemen());
                    row.createCell(col++).setCellValue(bulanIni != null ? bulanIni.getTanggalMulai().toString() : "");
                    row.createCell(col++).setCellValue(bulanIni != null ? bulanIni.getJenis().toString() : "");
                    row.createCell(col).setCellValue("");
                }

                for (int i = 0; i < kolom.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
        }
    }
}
