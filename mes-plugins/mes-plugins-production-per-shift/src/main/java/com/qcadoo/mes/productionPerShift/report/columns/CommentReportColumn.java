package com.qcadoo.mes.productionPerShift.report.columns;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.mes.productionPerShift.report.print.PPSReportXlsStyleContainer;
import com.qcadoo.model.api.Entity;

@Component("commentReportColumn")
public class CommentReportColumn extends AbstractReportColumn {

    private final PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    public CommentReportColumn(final TranslationService translationService, final PPSReportXlsHelper ppsReportXlsHelper) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
    }

    @Override
    public String getIdentifier() {
        return "comment";
    }

    @Override
    public Object getValue(final Entity productionPerShift) {
        return ppsReportXlsHelper.getOrder(productionPerShift).getStringField(OrderFields.DESCRIPTION);
    }

    @Override
    public Object getFirstRowValue(final Entity productionPerShift) {
        return getValue(productionPerShift);
    }

    @Override
    public String getChangeoverValue(final Entity productionPerShift) {
        Entity changeover = ppsReportXlsHelper.getChangeover(ppsReportXlsHelper.getOrder(productionPerShift));

        if (Objects.isNull(changeover)) {
            return StringUtils.EMPTY;
        }

        if (StringUtils.isEmpty(changeover.getStringField("name"))) {
            return changeover.getStringField("number");
        } else {
            return changeover.getStringField("name");
        }
    }

    @Override
    public String getFirstRowChangeoverValue(final Entity productionPerShift) {
        return getChangeoverValue(productionPerShift);
    }

    @Override
    public void setWhiteDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        if (!checkDescriptionLength(cell)) {
            cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyle));
        } else {
            cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleSmall));
        }
    }

    @Override
    public void setGreyDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        if (!checkDescriptionLength(cell)) {
            cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
        } else {
            cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleSmall));
        }
    }

    @Override
    public int getColumnWidth() {
        return 25 * 256;
    }

    private boolean checkDescriptionLength(final HSSFCell commentCell) {
        boolean checkSmall;

        HSSFRow row = commentCell.getRow();
        int orderDescriptionLength = commentCell.getStringCellValue().length();

        if (orderDescriptionLength <= 34) {
            row.setHeightInPoints(20);

            checkSmall = false;
        } else if ((orderDescriptionLength >= 34) && (orderDescriptionLength <= 69)) {
            row.setHeightInPoints(30);

            checkSmall = false;
        } else {
            row.setHeightInPoints(42);

            checkSmall = true;
        }

        return checkSmall;
    }

}
