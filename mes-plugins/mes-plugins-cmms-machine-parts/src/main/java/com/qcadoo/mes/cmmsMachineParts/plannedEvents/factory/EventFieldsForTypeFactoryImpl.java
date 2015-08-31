package com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForAdditionalWork;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForAfterReview;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForExternalService;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForManual;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForMeterReading;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForRepairs;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForReview;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForUdtReview;

@Service
public class EventFieldsForTypeFactoryImpl implements EventFieldsForTypeFactory {

    @Override
    public FieldsForType createFieldsForType(PlannedEventType type) {
        if (type.compareTo(PlannedEventType.REVIEW) == 0) {
            return new FieldsForReview();
        } else if (type.compareTo(PlannedEventType.REPAIRS) == 0) {
            return new FieldsForRepairs();
        } else if (type.compareTo(PlannedEventType.EXTERNAL_SERVICE) == 0) {
            return new FieldsForExternalService();
        } else if (type.compareTo(PlannedEventType.ADDITIONAL_WORK) == 0) {
            return new FieldsForAdditionalWork();
        } else if (type.compareTo(PlannedEventType.MANUAL) == 0) {
            return new FieldsForManual();
        } else if (type.compareTo(PlannedEventType.METER_READING) == 0) {
            return new FieldsForMeterReading();
        } else if (type.compareTo(PlannedEventType.UDT_REVIEW) == 0) {
            return new FieldsForUdtReview();
        } else if (type.compareTo(PlannedEventType.AFTER_REVIEW) == 0) {
            return new FieldsForAfterReview();
        }
        return null;
    }
}
