package com.qcadoo.mes.products.print.service.pdf;

import java.awt.Color;

import org.springframework.core.io.ClassPathResource;

import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public final class PdfPageNumbering extends PdfPageEventHelper {

    /** The PdfTemplate that contains the total number of pages. */
    private PdfTemplate total;

    /** The font that will be used. */
    private BaseFont arial;

    private Color lightColor;

    private final String page;

    private final String in;

    public PdfPageNumbering(final String page, final String in) {
        super();
        this.page = page;
        this.in = in;
    }

    /**
     * @see com.lowagie.text.pdf.PdfPageEvent#onOpenDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    @Override
    public void onOpenDocument(final PdfWriter writer, final Document document) {
        total = writer.getDirectContent().createTemplate(100, 100);
        total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
        try {
            ClassPathResource classPathResource = new ClassPathResource(MaterialRequirementPdfService.FONT_PATH);
            FontFactory.register(classPathResource.getPath());
            arial = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            lightColor = new Color(77, 77, 77);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * @see com.lowagie.text.pdf.PdfPageEvent#onStartPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    @Override
    public void onStartPage(final PdfWriter writer, final Document document) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = page + " " + writer.getPageNumber() + " " + in + " ";
        float textBase = document.top() + 22;
        float textSize = arial.getWidthPoint(text, 7);
        cb.setColorFill(lightColor);
        cb.setColorStroke(lightColor);
        cb.beginText();
        cb.setFontAndSize(arial, 7);
        float adjust = arial.getWidthPoint("0", 7);
        cb.setTextMatrix(document.right() - textSize - adjust, textBase);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(total, document.right() - adjust, textBase);
        cb.setLineWidth(1);
        cb.setLineDash(2, 2, 1);
        cb.moveTo(document.left(), document.top() + 12);
        cb.lineTo(document.right(), document.top() + 12);
        cb.stroke();
        cb.restoreState();
    }

    /**
     * @see com.lowagie.text.pdf.PdfPageEvent#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    @Override
    public void onEndPage(final PdfWriter writer, final Document document) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = page + " " + writer.getPageNumber() + " " + in + " ";
        float textBase = document.bottom() - 25;
        float textSize = arial.getWidthPoint(text, 7);
        cb.setColorFill(lightColor);
        cb.setColorStroke(lightColor);
        cb.setLineWidth(1);
        cb.setLineDash(2, 2, 1);
        cb.moveTo(document.left(), document.bottom() - 10);
        cb.lineTo(document.right(), document.bottom() - 10);
        cb.stroke();
        cb.beginText();
        cb.setFontAndSize(arial, 7);
        float adjust = arial.getWidthPoint("0", 7);
        cb.setTextMatrix(document.right() - textSize - adjust, textBase);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(total, document.right() - adjust, textBase);
        cb.restoreState();
    }

    /**
     * @see com.lowagie.text.pdf.PdfPageEvent#onCloseDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    @Override
    public void onCloseDocument(final PdfWriter writer, final Document document) {
        total.beginText();
        total.setFontAndSize(arial, 7);
        total.setTextMatrix(0, 0);
        total.showText(String.valueOf(writer.getPageNumber() - 1));
        total.endText();
    }
}
