package com.qcadoo.mes.lineChangeoverNormsForOrders;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public interface LineChangeoverNormsForOrdersService {

    void fillOrderForm(final ViewDefinitionState view, final List<String> orderFields);

    boolean checkIfOrderHasCorrectStateAndIsPrevious(final Entity previousOrder, final Entity order);

    Entity getProductionLineFromDB(final Long productionLineId);

    Entity getOrderFromDB(final Long orderId);

    Entity getTechnologyFromDB(final Long technologyId);

    Entity getTechnologyByNumberFromDB(final String number);

    Entity getTechnologyGroupByNumberFromDB(final String number);

    Entity getPreviousOrderFromDB(final Entity order);

}
