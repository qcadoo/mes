package com.qcadoo.mes.products.print.view.pdf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.internal.DefaultEntity;

public final class MaterialRequirementPdfView extends AbstractPdfView {

    private static final String PDF_EXTENSION = ".pdf";

    // TODO KRNA check
    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        /*
         * PdfReader reader = new PdfReader((String) entity.getField("fileName") + PDF_EXTENSION); int n =
         * reader.getNumberOfPages(); int i = 0; while (i < n) { i++; writer.getImportedPage(reader, i); }
         */
        // writer.freeReader(reader);
    }
}
