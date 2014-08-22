package com.qcadoo.mes.deviationCausesReporting.domain;

import java.text.Collator;
import java.util.Comparator;

import org.springframework.context.i18n.LocaleContextHolder;

public final class DeviationCauseHolderComparators {

    private DeviationCauseHolderComparators() {
    }

    public static final Comparator<DeviationCauseHolder> BY_REASON_ASC = new Comparator<DeviationCauseHolder>() {

        @Override
        public int compare(final DeviationCauseHolder o1, final DeviationCauseHolder o2) {
            Collator collator = Collator.getInstance(LocaleContextHolder.getLocale());
            return collator.compare(o1.getDeviationCause(), o2.getDeviationCause());
        }
    };

}
