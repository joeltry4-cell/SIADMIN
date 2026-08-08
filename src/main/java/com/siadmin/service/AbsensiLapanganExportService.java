package com.siadmin.service;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AbsensiLapanganExportService {

    private static final DateTimeFormatter TGL = DateTimeFormatter.ofPattern("dd/MM");

    public void tulisAbsensiMingguan(List<Karyawan> karyawanAktif, LocalDate awal, LocalDate akhir,
                                      Map<Long, List<Absensi>> absensiPerKaryawan,
                                      Map<Long, List<Lembur>> lemburPerKaryawan,
                                      OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Map<String, List<Karyawan>> perDivisi = new TreeMap<>();
            for (Karyawan k : karyawanAktif) {
                String divisi = (k.getDepartemen() == null || k.getDepartemen().isBlank()) ? "Tanpa Divisi" : k.getDepartemen();
                perDivisi.computeIfAbsent(divisi, x -> new java.util.ArrayList<>()).add(k);
            }

            for (Map.Entry<String, List<Karyawan>> entry : perDivisi.entrySet()) {
                Sheet sheet = workbook.createSheet(ExcelSheetNames.sanitasi(entry.getKey()));
                List<LocalDate> tanggalList = new java.util.ArrayList<>();
                for (LocalDate t = awal; !t.isAfter(akhir); t = t.plusDays(1)) {
                    tanggalList.add(t);
                }

                Row header = sheet.createRow(0);
                int col = 0;
                String[] kolomTetap = {"NO", "NIK", "Nama", "Jabatan", "Shift", "Tim"};
                for (String k : kolomTetap) {
                    header.createCell(col++).setCellValue(k);
                }
                for (LocalDate t : tanggalList) {
                    String tgl = t.format(TGL);
                    header.createCell(col++).setCellValue(tgl + " Masuk");
                    header.createCell(col++).setCellValue(tgl + " Keluar");
                    header.createCell(col++).setCellValue(tgl + " Jam Lembur");
                    header.createCell(col++).setCellValue(tgl + " TTD");
                }

                int rowIdx = 1;
                int no = 1;
                for (Karyawan k : entry.getValue()) {
                    Row row = sheet.createRow(rowIdx++);
                    col = 0;
                    row.createCell(col++).setCellValue(no++);
                    row.createCell(col++).setCellValue(k.getNip());
                    row.createCell(col++).setCellValue(k.getNamaLengkap());
                    row.createCell(col++).setCellValue(k.getJabatan());
                    row.createCell(col++).setCellValue(k.getShift() != null ? k.getShift() : "");
                    row.createCell(col++).setCellValue(k.getTim() != null ? k.getTim() : "");

                    Map<LocalDate, Absensi> absensiPerTanggal = new LinkedHashMap<>();
                    for (Absensi a : absensiPerKaryawan.getOrDefault(k.getId(), List.of())) {
                        absensiPerTanggal.put(a.getTanggal(), a);
                    }
                    Map<LocalDate, Double> lemburPerTanggal = new LinkedHashMap<>();
                    for (Lembur l : lemburPerKaryawan.getOrDefault(k.getId(), List.of())) {
                        lemburPerTanggal.merge(l.getTanggal(), l.getJumlahJam(), Double::sum);
                    }

                    for (LocalDate t : tanggalList) {
                        Absensi a = absensiPerTanggal.get(t);
                        row.createCell(col++).setCellValue(a != null && a.getJamMasuk() != null ? a.getJamMasuk().toString() : "");
                        row.createCell(col++).setCellValue(a != null && a.getJamKeluar() != null ? a.getJamKeluar().toString() : "");
                        Double jamLembur = lemburPerTanggal.get(t);
                        row.createCell(col++).setCellValue(jamLembur != null ? jamLembur : 0);
                        row.createCell(col++).setCellValue("");
                    }
                }

                for (int i = 0; i < kolomTetap.length + tanggalList.size() * 4; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
        }
    }
}
