package com.qcadoo.mes.workPlans;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.print.ReportDataService;
import com.qcadoo.mes.workPlans.print.WorkPlanReportDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;

public class WorkPlanReportDataServiceTest {

    private WorkPlanReportDataService workPlanReportDataService = null;

    private final ReportDataService reportDataService = mock(ReportDataService.class);

    private final DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

    private final List<Entity> entityTreeList = new ArrayList<Entity>();

    private final List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    private final EntityTree entityTree = new EntityTreeImpl(dataDefinition, "technology", new Long(1));

    @Before
    public void init() {
        workPlanReportDataService = new WorkPlanReportDataService();
        ReflectionTestUtils.setField(workPlanReportDataService, "reportDataService", reportDataService);

        EntityTree entityTreeSubTechnology = new EntityTreeImpl(dataDefinition, "technology", new Long(2));
        EntityList componentsList1 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(1));
        EntityList componentsList2 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(2));
        EntityList componentsList3 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(3));
        EntityList componentsOutList1 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(11));
        EntityList componentsOutList2 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(12));
        EntityList componentsOutList3 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(13));

        Entity machine1 = new DefaultEntity(dataDefinition);
        machine1.setId(new Long(1));
        Entity machine2 = new DefaultEntity(dataDefinition);
        machine2.setId(new Long(2));

        Entity worker1 = new DefaultEntity(dataDefinition);
        worker1.setId(new Long(1));
        Entity worker2 = new DefaultEntity(dataDefinition);
        worker2.setId(new Long(2));

        Entity operation1 = new DefaultEntity(dataDefinition);
        operation1.setField("machine", machine1);
        operation1.setField("staff", null);
        Entity operationComponentRoot = new DefaultEntity(dataDefinition);
        operationComponentRoot.setField("entityType", "operation");
        operationComponentRoot.setField("parent", null);
        operationComponentRoot.setId(new Long(1));
        operationComponentRoot.setField("operation", operation1);
        operationComponentRoot.setField("operationProductInComponents", componentsList1);
        operationComponentRoot.setField("operationProductOutComponents", componentsOutList1);

        Entity operation2 = new DefaultEntity(dataDefinition);
        operation2.setField("machine", machine2);
        operation2.setField("staff", worker2);
        Entity operationComponent2 = new DefaultEntity(dataDefinition);
        operationComponent2.setField("entityType", "operation");
        operationComponent2.setField("parent", operationComponentRoot);
        operationComponent2.setId(new Long(2));
        operationComponent2.setField("operation", operation2);
        operationComponent2.setField("operationProductInComponents", componentsList2);
        operationComponent2.setField("operationProductOutComponents", componentsOutList2);

        Entity operation3 = new DefaultEntity(dataDefinition);
        operation3.setField("machine", null);
        operation3.setField("staff", worker1);
        Entity operationComponent3 = new DefaultEntity(dataDefinition);
        operationComponent3.setField("entityType", "operation");
        operationComponent3.setField("parent", operationComponentRoot);
        operationComponent3.setId(new Long(3));
        operationComponent3.setField("operation", operation3);
        operationComponent3.setField("operationProductInComponents", componentsList3);
        operationComponent3.setField("operationProductOutComponents", componentsOutList3);
        entityTreeList.add(operationComponentRoot);
        entityTreeList.add(operationComponent2);
        entityTreeList.add(operationComponent3);

        Entity technology = new DefaultEntity(dataDefinition);
        technology.setField("operationComponents", entityTreeSubTechnology);
        technology.setField("componentQuantityAlgorithm", "02perTechnology");
        Entity operationTechnology = new DefaultEntity(dataDefinition);
        operationTechnology.setField("entityType", "technology");
        operationTechnology.setField("parent", operationComponentRoot);
        operationTechnology.setId(new Long(4));
        operationTechnology.setField("referenceTechnology", technology);

        entityTreeListWithoutTechnology.addAll(entityTreeList);
        entityTreeList.add(operationTechnology);
    }

    @Test
    public void shouldReturnValidList() {
        // given
        List<Entity> operationComponents = new ArrayList<Entity>();

        given(
                dataDefinition.find().addRestriction(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .setOrderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().addRestriction(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .setOrderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        // when
        workPlanReportDataService.addOperationsFromSubtechnologiesToList(entityTree, operationComponents);

        // then
        assertEquals(operationComponents.size(), 6);
    }

}
