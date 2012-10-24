package com.qcadoo.mes.operationTimeCalculations;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;

public class OperationWorkTimeServiceTest {

    private OperationWorkTimeService operationWorkTimeService;

    @Mock
    private NumberService numberService;

    private MathContext mc = MathContext.DECIMAL64;

    @Mock
    private Entity operComp1, operComp2, operComp3;

    private BigDecimal neededNumberOfCycles1, neededNumberOfCycles2, neededNumberOfCycles3;

    private Integer workstations1, workstations2, workstations3;

    @Mock
    private OperationWorkTime operationWorkTime;

    @Mock
    private DataDefinition dataDefinition;

    private BigDecimal lu1, lu2, lu3;

    private BigDecimal mu1, mu2, mu3;

    private Map<Entity, BigDecimal> operationsRuns = Maps.newHashMap();

    private Map<Entity, Integer> workstations = Maps.newHashMap();

    @Before
    public void init() {
        operationWorkTimeService = new OperationWorkTimeServiceImpl();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(operationWorkTimeService, "numberService", numberService);

        when(numberService.getMathContext()).thenReturn(mc);

        given(numberService.setScale(Mockito.any(BigDecimal.class))).willAnswer(new Answer<BigDecimal>() {

            @Override
            public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(5, RoundingMode.HALF_EVEN);
            }
        });

        when(operComp1.getDataDefinition()).thenReturn(dataDefinition);
        when(operComp2.getDataDefinition()).thenReturn(dataDefinition);
        when(operComp3.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getName()).thenReturn("technologyOperationComponent");

        neededNumberOfCycles1 = BigDecimal.ONE;
        neededNumberOfCycles2 = new BigDecimal(2);
        neededNumberOfCycles3 = new BigDecimal(2.3);

        when(operComp1.getField("tj")).thenReturn(new Integer(600));
        when(operComp2.getField("tj")).thenReturn(new Integer(300));
        when(operComp3.getField("tj")).thenReturn(new Integer(150));

        when(operComp1.getField("tpz")).thenReturn(new Integer(600));
        when(operComp2.getField("tpz")).thenReturn(new Integer(900));
        when(operComp3.getField("tpz")).thenReturn(new Integer(1200));

        when(operComp1.getField("timeNextOperation")).thenReturn(new Integer(600));
        when(operComp2.getField("timeNextOperation")).thenReturn(new Integer(300));
        when(operComp3.getField("timeNextOperation")).thenReturn(new Integer(450));

        lu1 = new BigDecimal(2.2);
        lu2 = new BigDecimal(0.6);
        lu3 = new BigDecimal(0.8);

        when(operComp1.getDecimalField("laborUtilization")).thenReturn(lu1);
        when(operComp2.getDecimalField("laborUtilization")).thenReturn(lu2);
        when(operComp3.getDecimalField("laborUtilization")).thenReturn(lu3);

        mu1 = new BigDecimal(0.2);
        mu2 = new BigDecimal(1.6);
        mu3 = new BigDecimal(0.7);

        when(operComp1.getDecimalField("machineUtilization")).thenReturn(mu1);
        when(operComp2.getDecimalField("machineUtilization")).thenReturn(mu2);
        when(operComp3.getDecimalField("machineUtilization")).thenReturn(mu3);

        workstations1 = Integer.valueOf(1);
        workstations2 = Integer.valueOf(2);
        workstations3 = Integer.valueOf(1);
        workstations.put(operComp1, workstations1);
        workstations.put(operComp2, workstations2);
        workstations.put(operComp3, workstations3);

        operationsRuns.put(operComp1, new BigDecimal(3));
        operationsRuns.put(operComp2, new BigDecimal(1.5));
        operationsRuns.put(operComp3, new BigDecimal(7));
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    private void assertBigDecimalEquals(final BigDecimal expected, final BigDecimal actual) {
        if (expected.compareTo(actual) != 0) {
            Assert.fail("expected " + expected + " but actual value is " + actual);
        }
    }

    private void assertIntegerEquals(final Integer expected, final Integer actual) {
        if (expected.compareTo(actual) != 0) {
            Assert.fail("expected " + expected + " but actual value is " + actual);
        }
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithoutTpzAndAdditionalTimeForOperComp1() throws Exception {
        // given
        BigDecimal abstractOperationWorkTime = BigDecimal.ZERO;
        when(numberService.setScale(abstractOperationWorkTime)).thenReturn(abstractOperationWorkTime);

        abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp1, neededNumberOfCycles1,
                false, false, workstations1);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(600), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithoutTpzAndAdditionalTimeForOperComp2() throws Exception {
        // given
        BigDecimal abstractOperationWorkTime = BigDecimal.ZERO;
        when(numberService.setScale(abstractOperationWorkTime)).thenReturn(abstractOperationWorkTime);
        // when
        abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp2, neededNumberOfCycles2,
                false, false, workstations2);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(600), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithoutTpzAndAdditionalTimeForOperComp3() throws Exception {
        // given
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp3,
                neededNumberOfCycles3, false, false, workstations3);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(345), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndAdditionalTimeForOperComp1() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp1,
                neededNumberOfCycles1, true, true, workstations1);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(1800), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndAdditionalTimeForOperComp2() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp2,
                neededNumberOfCycles2, true, true, workstations2);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(3000), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndAdditionalTimeForOperComp3() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp3,
                neededNumberOfCycles3, true, true, workstations3);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(1995), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndWithoutAdditionalTimeForOperComp1() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp1,
                neededNumberOfCycles1, true, false, workstations1);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(1200), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndWithoutAdditionalTimeForOperComp2() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp2,
                neededNumberOfCycles2, true, false, workstations2);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(2400), abstractOperationWorkTime);
    }

    @Test
    public void shouldEstimateAbstractOperationWorkTimeWithTpzAndWithoutAdditionalTimeForOperComp3() throws Exception {
        // when
        BigDecimal abstractOperationWorkTime = operationWorkTimeService.estimateAbstractOperationWorkTime(operComp3,
                neededNumberOfCycles3, true, false, workstations3);
        // then
        Assert.assertNotNull(abstractOperationWorkTime);
        assertBigDecimalEquals(new BigDecimal(1545), abstractOperationWorkTime);
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeForOper1() throws Exception {
        // when

        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp1, neededNumberOfCycles1, true, true,
                workstations1, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(3960));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(360));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeForOper2() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp2, neededNumberOfCycles2, true, true,
                workstations2, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(1800));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(4800));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeForOper3() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp3, neededNumberOfCycles3, true, true,
                workstations3, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(1596));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new BigDecimal(1396.5).intValue());
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzForOper1() throws Exception {
        // when

        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp1, neededNumberOfCycles1, false, true,
                workstations1, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(2640));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(240));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzForOper2() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp2, neededNumberOfCycles2, false, true,
                workstations2, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(720));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(1920));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzForOper3() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp3, neededNumberOfCycles3, false, true,
                workstations3, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(636));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new BigDecimal(556.5).intValue());
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutAddTimeForOper1() throws Exception {
        // when

        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp1, neededNumberOfCycles1, true, false,
                workstations1, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(2640));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(240));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutAddTimeForOper2() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp2, neededNumberOfCycles2, true, false,
                workstations2, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(1440));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(3840));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutAddTimeForOper3() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp3, neededNumberOfCycles3, true, false,
                workstations3, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(1236));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new BigDecimal(1081).intValue());
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzAndAddTimeForOper1() throws Exception {
        // when

        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp1, neededNumberOfCycles1, false, false,
                workstations1, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(1320));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(120));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzAndAddTimeForOper2() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp2, neededNumberOfCycles2, false, false,
                workstations2, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(360));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new Integer(960));
    }

    @Test
    public void shouldReturnEstimatesOperationWorkTimeWithoutTpzAndAddTimeForOper3() throws Exception {
        // when
        operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(operComp3, neededNumberOfCycles3, false, false,
                workstations3, false);
        // then
        assertIntegerEquals(operationWorkTime.getLaborWorkTime(), new Integer(276));
        assertIntegerEquals(operationWorkTime.getMachineWorkTime(), new BigDecimal(241.5).intValue());
    }

    @Test
    public void shouldReturnEstimateOperationsMapIncludedTpzAndAdditionalTime() throws Exception {
        // given
        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        Map<Entity, OperationWorkTime> operationDurations = operationWorkTimeService.estimateOperationsWorkTime(
                operationComponents, operationsRuns, true, true, workstations, false);
        // then
        assertEquals(3, operationDurations.values().size());
        assertEquals(new Integer(6600), operationDurations.get(operComp1).getLaborWorkTime());
        assertEquals(new Integer(600), operationDurations.get(operComp1).getMachineWorkTime());

        assertEquals(new Integer(1710), operationDurations.get(operComp2).getLaborWorkTime());
        assertEquals(new Integer(4560), operationDurations.get(operComp2).getMachineWorkTime());

        assertEquals(new Integer(2160), operationDurations.get(operComp3).getLaborWorkTime());
        assertEquals(new Integer(1890), operationDurations.get(operComp3).getMachineWorkTime());
    }

    @Test
    public void shouldReturnEstimateOperationsMapIncludedTpz() throws Exception {
        // given
        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        Map<Entity, OperationWorkTime> operationDurations = operationWorkTimeService.estimateOperationsWorkTime(
                operationComponents, operationsRuns, true, false, workstations, false);
        // then
        assertEquals(3, operationDurations.values().size());
        assertEquals(new Integer(5280), operationDurations.get(operComp1).getLaborWorkTime());
        assertEquals(new Integer(480), operationDurations.get(operComp1).getMachineWorkTime());

        assertEquals(new Integer(1350), operationDurations.get(operComp2).getLaborWorkTime());
        assertEquals(new Integer(3600), operationDurations.get(operComp2).getMachineWorkTime());

        assertEquals(new Integer(1800), operationDurations.get(operComp3).getLaborWorkTime());
        assertEquals(new Integer(1575), operationDurations.get(operComp3).getMachineWorkTime());
    }

    @Test
    public void shouldReturnEstimateOperationsMapIncludedAdditionalTime() throws Exception {
        // given
        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        Map<Entity, OperationWorkTime> operationDurations = operationWorkTimeService.estimateOperationsWorkTime(
                operationComponents, operationsRuns, false, true, workstations, false);
        // then
        assertEquals(3, operationDurations.values().size());
        assertEquals(new Integer(5280), operationDurations.get(operComp1).getLaborWorkTime());
        assertEquals(new Integer(480), operationDurations.get(operComp1).getMachineWorkTime());

        assertEquals(new Integer(630), operationDurations.get(operComp2).getLaborWorkTime());
        assertEquals(new Integer(1680), operationDurations.get(operComp2).getMachineWorkTime());

        assertEquals(new Integer(1200), operationDurations.get(operComp3).getLaborWorkTime());
        assertEquals(new Integer(1050), operationDurations.get(operComp3).getMachineWorkTime());
    }

    @Test
    public void shouldEstimateTotalOrderTimeWithTpzAndAdditionalTime() throws Exception {
        // given

        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        operationWorkTime = operationWorkTimeService.estimateTotalWorkTime(operationComponents, operationsRuns, true, true,
                workstations, false);
        // then
        assertEquals(operationWorkTime.getLaborWorkTime(), new Integer(10470));
        assertEquals(operationWorkTime.getMachineWorkTime(), new Integer(7050));
    }

    @Test
    public void shouldEstimateTotalOrderTimeWithTpz() throws Exception {
        // given

        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        operationWorkTime = operationWorkTimeService.estimateTotalWorkTime(operationComponents, operationsRuns, true, false,
                workstations, false);
        // then
        assertEquals(operationWorkTime.getLaborWorkTime(), new Integer(8430));
        assertEquals(operationWorkTime.getMachineWorkTime(), new Integer(5655));
    }

    @Test
    public void shouldEstimateTotalOrderTimeWithAdditionalTime() throws Exception {
        // given

        EntityList operationComponents = mockEntityList(Arrays.asList(operComp1, operComp2, operComp3));
        // when
        operationWorkTime = operationWorkTimeService.estimateTotalWorkTime(operationComponents, operationsRuns, false, true,
                workstations, false);
        // then
        assertEquals(operationWorkTime.getLaborWorkTime(), new Integer(7110));
        assertEquals(operationWorkTime.getMachineWorkTime(), new Integer(3210));
    }

}
