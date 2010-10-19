package com.qcadoo.mes.products;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class PdfOrderView extends AbstractPdfView {

    @Override
    protected void buildPdfDocument(final Map<String, Object> model, final Document document, final PdfWriter writer,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        document.open();
        document.add(new Paragraph("First Page."));
        document.setPageSize(PageSize.A3);
        document.newPage();
        document.add(new Paragraph("This PageSize is A3."));
    }

}
