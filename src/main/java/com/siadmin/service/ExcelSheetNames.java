package com.siadmin.service;

/**
 * Sheet names di Excel maksimal 31 karakter dan tidak boleh mengandung karakter [ ] : * ? / \.
 */
final class ExcelSheetNames {

    private ExcelSheetNames() {
    }

    static String sanitasi(String nama) {
        String bersih = nama.replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        return bersih.length() > 31 ? bersih.substring(0, 31) : bersih;
    }
}
