package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.reservation.ReservationsServiceForProductsToIssue;
import com.qcadoo.mes.productFlowThruDivision.service.WarehouseIssueService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.CreationDocumentResponse;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.UpdateIssuesLocationsQuantityStatusHolder;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.validators.IssueValidators;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WarehouseIssueStateService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WarehouseIssueService warehouseIssueService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private IssueValidators issueValidators;

    @Autowired
    private ReservationsServiceForProductsToIssue reservationsServiceForProductsToIssue;

    public boolean checkIfAnyNotIssuedPositionsExists(final StateChangeContext stateChangeContext) {
        Entity warehouseIssue = stateChangeContext.getOwner();
        List<Entity> issues = warehouseIssue.getHasManyField(WarehouseIssueFields.ISSUES);
        if (!issues.stream().filter(e -> !e.getBooleanField(IssueFields.ISSUED)).collect(Collectors.toList()).isEmpty()) {
            stateChangeContext.addValidationError("productFlowThruDivision.issue.state.complete.error.existsNotIssuedPositions");
            return true;
        }
        return false;
    }

    public void onIssueValidate(final StateChangeContext stateChangeContext) {
        Entity warehouseIssue = stateChangeContext.getOwner();
        List<Entity> issues = warehouseIssue.getHasManyField(WarehouseIssueFields.ISSUES);

        if (!warehouseIssueService.checkIfAllIssueQuantityGraterThanZero(issues)) {
            stateChangeContext.addValidationError("productFlowThruDivision.issue.state.accept.error.issueForZero");
            return;
        }
        if (issues.stream().filter(i -> !i.getBooleanField(IssueFields.ISSUED)).collect(Collectors.toList()).isEmpty()) {
            stateChangeContext.addValidationError("productFlowThruDivision.issue.state.accept.error.noPositionsToIssue");
            return;
        }
        if (!issueValidators.checkIfCanIssueQuantity(issues)) {
            stateChangeContext.addValidationError("productFlowThruDivision.issue.state.accept.error.issuedToExtentNecessary");
            return;
        }
    }

    public void onIssue(final StateChangeContext stateChangeContext) {
        stateChangeContext.getOwner().setField(WarehouseIssueFields.DATE_OF_ISSUED, new Date());
        Entity warehouseIssue = stateChangeContext.getOwner();

        List<Entity> issues = getNotIssuedPositions(warehouseIssue);
        Entity location = warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);

        UpdateIssuesLocationsQuantityStatusHolder updateIssuesStatus = warehouseIssueService.tryUpdateIssuesLocationsQuantity(
                location, issues);

        if (!updateIssuesStatus.isUpdated()) {
            stateChangeContext.addValidationError("productFlowThruDivision.issue.state.accept.error.noProductsOnLocation",
                    updateIssuesStatus.getMessage(), location.getStringField(LocationFields.NUMBER));
        } else {
            createWarehouseDocuments(stateChangeContext, warehouseIssue, issues);
        }
    }

    private void createWarehouseDocuments(final StateChangeContext stateChangeContext, final Entity warehouseIssue,
            final List<Entity> issues) {
        MultiMap warehouseIssuesMap = new MultiHashMap();
        for (Entity issue : issues) {
            warehouseIssuesMap.put(issue.getBelongsToField(IssueFields.LOCATION).getId(), issue);
        }
        for (Object key : warehouseIssuesMap.keySet()) {
            Long id = (Long) key;

            Entity locationTo = getLocationDD().get(id);
            Entity locationFrom = warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);
            Entity order = null;
            if (warehouseIssueParameterService.issueForOrder()) {
                order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                        warehouseIssue.getBelongsToField(WarehouseIssueFields.ORDER).getId());
            }
            Collection coll = (Collection) warehouseIssuesMap.get(key);

            List<Entity> _issues = Lists.newArrayList(coll);

            CreationDocumentResponse response = warehouseIssueService.createWarehouseDocument(locationFrom, locationTo, order,
                    coll);

            if (!response.isValid()) {
                stateChangeContext.addValidationError("productFlowThruDivision.issue.state.accept.error.documentsNotCreated");

                if (Objects.nonNull(response.getErrors())) {
                    response.getErrors().forEach(er -> stateChangeContext.addValidationError(er.getMessage(), er.getVars()));
                }

                stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            } else {
                warehouseIssueService.updateIssuePosition(coll, response.getDocument());
                warehouseIssueService.updateProductsToIssues(Sets.newHashSet(warehouseIssue), _issues);
            }
        }
    }

    private List<Entity> getNotIssuedPositions(final Entity warehouseIssue) {
        List<Entity> issues = warehouseIssue.getHasManyField(WarehouseIssueFields.ISSUES);
        issues = issues.stream().filter(e -> !e.getBooleanField(IssueFields.ISSUED)).collect(Collectors.toList());
        return issues;
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private List<Entity> getUniqueProductsFromIssues(final List<Entity> issues) {
        List<Entity> products = issues.stream().map(issue -> issue.getBelongsToField("product")).collect(Collectors.toList());
        Map<Long, Entity> distinctProducts = products.stream().collect(Collectors.toMap(p -> p.getId(), p -> p, (p, q) -> p));
        return Lists.newArrayList(distinctProducts.values());
    }

}
