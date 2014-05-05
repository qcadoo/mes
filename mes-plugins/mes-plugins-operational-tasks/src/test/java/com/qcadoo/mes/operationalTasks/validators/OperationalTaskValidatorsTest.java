package com.qcadoo.mes.operationalTasks.validators;

import static com.qcadoo.testing.model.EntityTestUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationalTaskValidatorsTest {

    private static final String OTHER_TASK_TYPE = "01otherCase";

    private static final String YET_ANOTHER_TASK_TYPE = "yetAnother" + OTHER_TASK_TYPE;

    private static final Date EARLIER_DATE = new DateTime(2014, 5, 2, 15, 30).toDate();

    private static final Date LATER_DATE = new DateTime(2014, 5, 2, 21, 30).toDate();

    private OperationalTaskValidators operationalTaskValidators;

    @Mock
    private DataDefinition operationalTaskDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationalTaskValidators = new OperationalTaskValidators();
    }

    private Entity mockOperationalTask(final String type, final String name, final Date startDate, final Date finishDate) {
        Entity operationalTask = mockEntity(operationalTaskDD);
        stubStringField(operationalTask, OperationalTaskFields.TYPE_TASK, type);
        stubStringField(operationalTask, OperationalTaskFields.NAME, name);
        stubDateField(operationalTask, OperationalTaskFields.START_DATE, startDate);
        stubDateField(operationalTask, OperationalTaskFields.FINISH_DATE, finishDate);
        return operationalTask;
    }

    @Test
    public final void shouldPass() {
        // given
        Entity operationalTask = mockOperationalTask(OTHER_TASK_TYPE, "some name", EARLIER_DATE, LATER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldPassIfTypeIsNotTheOtherTask() {
        // given
        Entity operationalTask = mockOperationalTask(YET_ANOTHER_TASK_TYPE, null, EARLIER_DATE, LATER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldPassIfDatesAreTheSame() {
        // given
        Entity operationalTask = mockOperationalTask(OTHER_TASK_TYPE, "some name", EARLIER_DATE, EARLIER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldFailBecauseOfMissingName() {
        // given
        Entity operationalTask = mockOperationalTask(OTHER_TASK_TYPE, null, EARLIER_DATE, LATER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldFailBecauseOfBlankName() {
        // given
        Entity operationalTask = mockOperationalTask(OTHER_TASK_TYPE, "  ", EARLIER_DATE, LATER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldFailBecauseOfWrongOrderDates() {
        // given
        Entity operationalTask = mockOperationalTask(OTHER_TASK_TYPE, "some name", LATER_DATE, EARLIER_DATE);

        // when
        boolean isValid = operationalTaskValidators.onValidate(operationalTaskDD, operationalTask);

        // then
        assertFalse(isValid);
    }
}
