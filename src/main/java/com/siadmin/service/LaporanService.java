package com.siadmin.service;

import com.siadmin.dto.RekapBulanan;
import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusAbsensi;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LaporanService {

    private final AbsensiService absensiService;
    private final KaryawanService karyawanService;

    public LaporanService(AbsensiService absensiService, KaryawanService karyawanService) {
        this.absensiService = absensiService;
        this.karyawanService = karyawanService;
    }

    public List<RekapBulanan> rekapBulanan(YearMonth periode) {
        var awal = periode.atDay(1);
        var akhir = periode.atEndOfMonth();
        List<Absensi> daftarAbsensi = absensiService.findByRentang(awal, akhir);
        Map<Long, List<Absensi>> byKaryawanId = daftarAbsensi.stream()
                .collect(Collectors.groupingBy(a -> a.getKaryawan().getId()));

        return karyawanService.findAll().stream()
                .filter(Karyawan::isAktif)
                .map(k -> {
                    List<Absensi> milik = byKaryawanId.getOrDefault(k.getId(), List.of());
                    return new RekapBulanan(k,
                            hitung(milik, StatusAbsensi.HADIR),
                            hitung(milik, StatusAbsensi.IZIN),
                            hitung(milik, StatusAbsensi.SAKIT),
                            hitung(milik, StatusAbsensi.CUTI),
                            hitung(milik, StatusAbsensi.ALPA));
                })
                .sorted(Comparator.comparing(r -> r.karyawan().getNamaLengkap()))
                .toList();
    }

    private long hitung(List<Absensi> daftar, StatusAbsensi status) {
        return daftar.stream().filter(a -> a.getStatus() == status).count();
    }
}
