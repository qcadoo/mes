package com.qcadoo.mes.core.data.controls;

public interface FieldControlFactory {

    FieldControl selectControl();

    FieldControl textControl();

    FieldControl stringControl();

    FieldControl displayControl();

    FieldControl dateControl();

    FieldControl decimalControl();

    FieldControl passwordControl();

    FieldControl dateTimeControl();

}
