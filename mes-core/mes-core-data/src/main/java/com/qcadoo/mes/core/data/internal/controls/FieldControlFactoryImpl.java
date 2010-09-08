package com.qcadoo.mes.core.data.internal.controls;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.controls.FieldControl;
import com.qcadoo.mes.core.data.controls.FieldControlFactory;

@Service
public class FieldControlFactoryImpl implements FieldControlFactory {

    // int NUMERIC_TYPE_BOOLEAN = 1;
    //
    // int NUMERIC_TYPE_DATE = 2;
    //
    // int NUMERIC_TYPE_DATE_TIME = 3;
    //
    // int NUMERIC_TYPE_DICTIONARY = 4;
    //
    // int NUMERIC_TYPE_INTEGER = 6;
    //
    // int NUMERIC_TYPE_DECIMAL = 7;
    //
    // int NUMERIC_TYPE_STRING = 8;
    //
    // int NUMERIC_TYPE_TEXT = 9;
    //
    // int NUMERIC_TYPE_BELONGS_TO = 10;
    //
    // int NUMERIC_TYPE_PASSWORD = 11;
    //
    // int NUMERIC_TYPE_PRIORITY = 12;

    @Override
    public FieldControl selectControl() {
        return new StringControl(4);
    }

    @Override
    public FieldControl textControl() {
        return new StringControl(9);
    }

    @Override
    public FieldControl stringControl() {
        return new StringControl(8);
    }

    @Override
    public FieldControl displayControl() {
        return new StringControl(8);
    }

    @Override
    public FieldControl dateControl() {
        return new StringControl(2);
    }

    @Override
    public FieldControl decimalControl() {
        return new StringControl(7);
    }

    @Override
    public FieldControl passwordControl() {
        return new StringControl(11);
    }

    @Override
    public FieldControl dateTimeControl() {
        return new StringControl(3);
    }

}
