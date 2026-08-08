package com.siadmin.dto;

import com.siadmin.model.Karyawan;

public record RekapBulanan(Karyawan karyawan, long hadir, long izin, long sakit, long cuti, long alpa) {
}
