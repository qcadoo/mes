package com.qcadoo.mes.materialFlowResources.print.helper;

import com.qcadoo.report.api.pdf.HeaderAlignment;

public class HeaderAlignmentWithWidth {

    private HeaderAlignment alignment;

    private int width;

    public HeaderAlignmentWithWidth(HeaderAlignment alignment, int width) {
        this.alignment = alignment;
        this.width = width;
    }

    public HeaderAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(HeaderAlignment alignment) {
        this.alignment = alignment;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
