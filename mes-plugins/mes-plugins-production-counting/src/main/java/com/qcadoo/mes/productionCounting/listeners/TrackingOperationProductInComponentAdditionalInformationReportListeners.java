package com.qcadoo.mes.productionCounting.listeners;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class TrackingOperationProductInComponentAdditionalInformationReportListeners {

    private static final String L_FROM_DATE = "fromDate";

    private static final String L_TO_DATE = "toDate";

    public void generateReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale());
            sdf.setLenient(false);
            Date fromDate = sdf.parse(view.getComponentByReference(L_FROM_DATE).getFieldValue().toString());
            Date toDate = sdf.parse(view.getComponentByReference(L_TO_DATE).getFieldValue().toString());
            Assert.state(!toDate.before(fromDate));

            String redirectUrl = fromPath(
                    "/productionCounting/trackingOperationProductInComponentAdditionalInformationReport.xlsx")
                            .queryParam("fromDate", formatDate(fromDate)).queryParam("toDate", formatDate(toDate)).build()
                            .toUriString();

            view.redirectTo(redirectUrl, false, false);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale());
        return sdf.format(date);
    }

}
