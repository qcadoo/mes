/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.products.print.pdf.util;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;

public final class TableBorderEvent implements PdfPTableEvent {

    @Override
    public void tableLayout(final PdfPTable table, final float[][] widths, final float[] heights, final int headerRows,
            final int rowStart, final PdfContentByte[] canvases) {
        float[] width = widths[0];
        float x1 = width[0];
        float x2 = width[width.length - 1];
        float y1 = heights[0];
        float y2 = heights[heights.length - 1];
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.saveState();
        cb.setLineWidth(1);
        cb.setColorStroke(PdfUtil.getLineDarkColor());
        cb.rectangle(x1, y1, x2 - x1, y2 - y1);
        cb.stroke();
        cb.restoreState();
    }
}
