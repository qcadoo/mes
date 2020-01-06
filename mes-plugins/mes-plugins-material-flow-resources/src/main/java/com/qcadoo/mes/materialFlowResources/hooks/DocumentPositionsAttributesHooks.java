package com.qcadoo.mes.materialFlowResources.hooks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class DocumentPositionsAttributesHooks {

    @Autowired
    private ParameterService parameterService;

    public void initializeDates(final ViewDefinitionState view) {
        FieldComponent dateFrom = (FieldComponent) view.getComponentByReference("dateFrom");
        FieldComponent dateTo = (FieldComponent) view.getComponentByReference("dateTo");
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.MONTH,
                -parameterService.getParameter().getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS)
                        .getIntegerField("numberOfMonthsForPositionsData"));
        Date dateAgo = calendar.getTime();
        dateFrom.setFieldValue(setDateField(dateAgo));
        dateTo.setFieldValue(setDateField(now));
    }

    private Object setDateField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_FORMAT, Locale.getDefault()).format(date);
    }
}
