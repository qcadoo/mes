package com.qcadoo.mes.basic.services;

import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;

@Service
public class NumberPatternGeneratorService {

    private static final DecimalFormat threeCharacterNumber = new DecimalFormat("000");

    private static final DecimalFormat fourCharacterNumber = new DecimalFormat("0000");

    private static final DecimalFormat fiveCharacterNumber = new DecimalFormat("00000");

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public String generateNumber(Entity numberPattern) {
        StringBuilder number = new StringBuilder();
        for (Entity element : numberPattern.getHasManyField(NumberPatternFields.NUMBER_PATTERN_ELEMENTS)) {
            addElementToNumber(number, element);
        }
        return number.toString();
    }

    private void addElementToNumber(StringBuilder number, Entity element) {
        Date now = new Date();
        long sequenceNumber;
        switch (NumberPatternElement.parseString(element.getStringField(NumberPatternElementFields.ELEMENT))) {
            case DD:
                number.append(DateFormatUtils.format(now, "dd"));
                break;
            case MM:
                number.append(DateFormatUtils.format(now, "MM"));
                break;
            case RR:
                number.append(DateFormatUtils.format(now, "yy"));
                break;
            case RRRR:
                number.append(DateFormatUtils.format(now, "yyyy"));
                break;
            case N999:
                sequenceNumber = getSequenceNumber(element.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN));
                if (sequenceNumber <= 999) {
                    number.append(threeCharacterNumber.format(sequenceNumber));
                } else {
                    number.append(sequenceNumber);
                }
                break;
            case N9999:
                sequenceNumber = getSequenceNumber(element.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN));
                if (sequenceNumber <= 9999) {
                    number.append(fourCharacterNumber.format(sequenceNumber));
                } else {
                    number.append(sequenceNumber);
                }
                break;
            case N99999:
                sequenceNumber = getSequenceNumber(element.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN));
                if (sequenceNumber <= 99999) {
                    number.append(fiveCharacterNumber.format(sequenceNumber));
                } else {
                    number.append(sequenceNumber);
                }
                break;
            case XX:
                number.append(element.getStringField(NumberPatternElementFields.VALUE));
        }
    }

    private long getSequenceNumber(Entity numberPattern) {
        return jdbcTemplate.queryForObject(
                "select nextval('number_pattern_" + numberPattern.getStringField(NumberPatternFields.NUMBER) + "_seq')",
                Collections.emptyMap(), Long.class);
    }
}
