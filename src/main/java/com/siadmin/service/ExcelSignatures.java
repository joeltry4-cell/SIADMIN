package com.siadmin.service;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Menempel gambar tanda tangan (PNG) ke satu sel Excel.
 */
final class ExcelSignatures {

    private ExcelSignatures() {
    }

    static void tempel(Sheet sheet, Drawing<?> drawing, int row, int col, byte[] gambarPng) {
        if (gambarPng == null) {
            return;
        }
        Workbook workbook = sheet.getWorkbook();
        int pictureIdx = workbook.addPicture(gambarPng, Workbook.PICTURE_TYPE_PNG);
        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1);
        anchor.setRow2(row + 1);
        drawing.createPicture(anchor, pictureIdx);
    }
}
