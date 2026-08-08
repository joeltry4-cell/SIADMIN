package com.siadmin.controller;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import com.siadmin.repository.UserRepository;
import com.siadmin.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dokumen")
public class DokumenLapanganController {

    private final KaryawanService karyawanService;
    private final AbsensiService absensiService;
    private final CutiService cutiService;
    private final LemburService lemburService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AbsensiLapanganExportService absensiLapanganExportService;
    private final CutiRosterExportService cutiRosterExportService;
    private final LemburFormExportService lemburFormExportService;

    public DokumenLapanganController(KaryawanService karyawanService, AbsensiService absensiService,
                                      CutiService cutiService, LemburService lemburService,
                                      UserService userService, UserRepository userRepository,
                                      AbsensiLapanganExportService absensiLapanganExportService,
                                      CutiRosterExportService cutiRosterExportService,
                                      LemburFormExportService lemburFormExportService) {
        this.karyawanService = karyawanService;
        this.absensiService = absensiService;
        this.cutiService = cutiService;
        this.lemburService = lemburService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.absensiLapanganExportService = absensiLapanganExportService;
        this.cutiRosterExportService = cutiRosterExportService;
        this.lemburFormExportService = lemburFormExportService;
    }

    private Map<Long, byte[]> resolveTandaTangan(List<Karyawan> karyawanList) {
        Map<Long, byte[]> hasil = new HashMap<>();
        for (Karyawan k : karyawanList) {
            userRepository.findByKaryawan(k)
                    .flatMap(userService::bacaTandaTangan)
                    .ifPresent(ttd -> hasil.put(k.getId(), ttd));
        }
        return hasil;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("daftarKaryawan", karyawanService.findAll());
        return "dokumen/index";
    }

    @GetMapping("/absensi-mingguan")
    public void absensiMingguan(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate awal,
                                 HttpServletResponse response) throws IOException {
        LocalDate a = awal != null ? awal : LocalDate.now();
        LocalDate akhir = a.plusDays(6);

        List<Karyawan> karyawanAktif = karyawanService.findAll().stream().filter(Karyawan::isAktif).toList();
        List<Absensi> absensi = absensiService.findByRentang(a, akhir);
        Map<Long, List<Absensi>> absensiPerKaryawan = absensi.stream()
                .collect(Collectors.groupingBy(x -> x.getKaryawan().getId()));
        List<Lembur> lembur = lemburService.findApprovedByRentang(a, akhir);
        Map<Long, List<Lembur>> lemburPerKaryawan = lembur.stream()
                .collect(Collectors.groupingBy(x -> x.getKaryawan().getId()));
        Map<Long, byte[]> tandaTanganPerKaryawan = resolveTandaTangan(karyawanAktif);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=absensi-mingguan-" + a + ".xlsx");
        absensiLapanganExportService.tulisAbsensiMingguan(karyawanAktif, a, akhir, absensiPerKaryawan, lemburPerKaryawan,
                tandaTanganPerKaryawan, response.getOutputStream());
    }

    @GetMapping("/roster-cuti")
    public void rosterCuti(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth bulan,
                            HttpServletResponse response) throws IOException {
        YearMonth b = bulan != null ? bulan : YearMonth.now();
        List<Karyawan> karyawanAktif = karyawanService.findAll().stream().filter(Karyawan::isAktif).toList();
        Map<Long, List<com.siadmin.model.Cuti>> cutiPerKaryawan = karyawanAktif.stream()
                .collect(Collectors.toMap(Karyawan::getId, cutiService::findByKaryawan));
        Map<Long, byte[]> tandaTanganPerKaryawan = resolveTandaTangan(karyawanAktif);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=roster-cuti-" + b + ".xlsx");
        cutiRosterExportService.tulisRosterCuti(karyawanAktif, b, cutiPerKaryawan, tandaTanganPerKaryawan, response.getOutputStream());
    }

    @GetMapping("/form-lembur")
    public void formLembur(@RequestParam Long karyawanId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth bulan,
                            HttpServletResponse response) throws IOException {
        YearMonth b = bulan != null ? bulan : YearMonth.now();
        Karyawan karyawan = karyawanService.findById(karyawanId);
        List<Lembur> daftarLembur = lemburService.findApprovedByKaryawanAndBulan(karyawan, b);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=form-lembur-" + karyawan.getNip() + "-" + b + ".xlsx");
        lemburFormExportService.tulisFormLembur(karyawan, daftarLembur, response.getOutputStream());
    }
}
