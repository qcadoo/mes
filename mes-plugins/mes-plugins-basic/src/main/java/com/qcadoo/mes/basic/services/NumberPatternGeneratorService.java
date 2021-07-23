package com.qcadoo.mes.basic.services;

import com.qcadoo.mes.basic.constants.NumberPatternElement;
import com.qcadoo.mes.basic.constants.NumberPatternElementFields;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.Entity;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class NumberPatternGeneratorService {

    private static final DecimalFormat threeCharacterNumber = new DecimalFormat("000");

    private static final DecimalFormat fourCharacterNumber = new DecimalFormat("0000");

    private static final DecimalFormat fiveCharacterNumber = new DecimalFormat("00000");

    public static final String L_MONTHLY = "01monthly";

    public static final String L_ANNUAL = "02annual";

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
                sequenceNumber = getSequenceNumber(element);
                if (sequenceNumber <= 999) {
                    number.append(threeCharacterNumber.format(sequenceNumber));
                } else {
                    number.append(sequenceNumber);
                }
                break;
            case N9999:
                sequenceNumber = getSequenceNumber(element);
                if (sequenceNumber <= 9999) {
                    number.append(fourCharacterNumber.format(sequenceNumber));
                } else {
                    number.append(sequenceNumber);
                }
                break;
            case N99999:
                sequenceNumber = getSequenceNumber(element);
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

    private long getSequenceNumber(Entity element) {
        Entity numberPattern = element.getBelongsToField(NumberPatternElementFields.NUMBER_PATTERN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("patternNumber", numberPattern.getStringField(NumberPatternFields.NUMBER));
        parameters.put("monthly", isMonthly(element));
        parameters.put("annual", isAnnual(element));

        Long value = jdbcTemplate.queryForObject("select get_sequence_value_for_pattern(:patternNumber, :monthly, :annual)",
                parameters, Long.class);

        return value;
    }

    private Boolean isMonthly(Entity element) {
        String sequenceCycle = element.getStringField(NumberPatternElementFields.SEQUENCE_CYCLE);
        if(StringUtils.isNoneEmpty(sequenceCycle)) {
            if(L_MONTHLY.equals(sequenceCycle)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    private Boolean isAnnual(Entity element) {
        String sequenceCycle = element.getStringField(NumberPatternElementFields.SEQUENCE_CYCLE);
        if(StringUtils.isNoneEmpty(sequenceCycle)) {
            if(L_ANNUAL.equals(sequenceCycle)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }
}
