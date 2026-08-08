package com.siadmin.controller;

import com.siadmin.service.ExcelExportService;
import com.siadmin.service.LaporanService;
import com.siadmin.service.PdfExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.YearMonth;

@Controller
@RequestMapping("/laporan")
public class LaporanController {

    private final LaporanService laporanService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    public LaporanController(LaporanService laporanService, ExcelExportService excelExportService, PdfExportService pdfExportService) {
        this.laporanService = laporanService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/bulanan")
    public String bulanan(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth periode, Model model) {
        YearMonth p = periode != null ? periode : YearMonth.now();
        model.addAttribute("periode", p);
        model.addAttribute("daftarRekap", laporanService.rekapBulanan(p));
        return "laporan/bulanan";
    }

    @GetMapping("/bulanan/excel")
    public void excel(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth periode,
                       HttpServletResponse response) throws IOException {
        YearMonth p = periode != null ? periode : YearMonth.now();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=laporan-bulanan-" + p + ".xlsx");
        excelExportService.tulisRekapBulanan(laporanService.rekapBulanan(p), p, response.getOutputStream());
    }

    @GetMapping("/bulanan/pdf")
    public void pdf(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth periode,
                     HttpServletResponse response) throws IOException {
        YearMonth p = periode != null ? periode : YearMonth.now();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=laporan-bulanan-" + p + ".pdf");
        pdfExportService.tulisRekapBulanan(laporanService.rekapBulanan(p), p, response.getOutputStream());
    }
}
