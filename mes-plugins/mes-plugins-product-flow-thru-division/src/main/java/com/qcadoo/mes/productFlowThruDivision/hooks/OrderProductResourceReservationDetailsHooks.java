package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ResourceCriteriaModifiersPFTD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderProductResourceReservationDetailsHooks {

    @Autowired
    private NumberService numberService;

    public void onResourceChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity orderProductResourceReservation = form.getPersistedEntityWithIncludedFormValues();
        Entity pcq = orderProductResourceReservation.getBelongsToField("productionCountingQuantity");

        LookupComponent resourceLookup = (LookupComponent) view.getComponentByReference("resource");
        if(!resourceLookup.isEmpty()) {
            Entity resource = resourceLookup.getEntity();
            if(Objects.isNull(orderProductResourceReservation.getId())) {
                BigDecimal plannedQuantityFromResources = pcq.getHasManyField("orderProductResourceReservations")
                        .stream()
                        .map(rr -> rr.getDecimalField("planedQuantity"))
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

                setPlannedQuantity(form, orderProductResourceReservation, pcq, resource, plannedQuantityFromResources);
            } else {
                List<Entity> orderProductResourceReservations = pcq.getHasManyField("orderProductResourceReservations");

                if(Objects.nonNull(orderProductResourceReservation.getId())) {
                    orderProductResourceReservations = orderProductResourceReservations
                            .stream()
                            .filter(oprr -> !oprr.getId().equals(orderProductResourceReservation.getId()))
                            .collect(Collectors.toList());
                }
                BigDecimal plannedQuantityFromResources = orderProductResourceReservations
                        .stream()
                        .map(rr -> rr.getDecimalField("planedQuantity"))
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

                setPlannedQuantity(form, orderProductResourceReservation, pcq, resource, plannedQuantityFromResources);

            }

        }

    }

    private void setPlannedQuantity(FormComponent form, Entity orderProductResourceReservation, Entity pcq, Entity resource, BigDecimal plannedQuantityFromResources) {
        BigDecimal productPlannedQuantity = pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
        BigDecimal remainingQuantityToReservation = productPlannedQuantity.subtract(plannedQuantityFromResources, numberService.getMathContext());
        BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);

        BigDecimal reservedQuantity;
        if (remainingQuantityToReservation.compareTo(resourceQuantity) >= 0) {
            reservedQuantity = resourceQuantity;
        } else {
            reservedQuantity = remainingQuantityToReservation;
        }

        orderProductResourceReservation.setField("planedQuantity",reservedQuantity);
        form.setEntity(orderProductResourceReservation);
    }


    public void onBeforeRender(final ViewDefinitionState view) {


        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity orderProductResourceReservation = form.getPersistedEntityWithIncludedFormValues();


        Entity pcq = orderProductResourceReservation.getBelongsToField("productionCountingQuantity");
        Entity product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        Entity componentsLocation = pcq.getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

        List<Entity> orderProductResourceReservations = pcq.getHasManyField("orderProductResourceReservations");

        List<Long> alreadyAddedResourceIds = orderProductResourceReservations
                .stream()
                .map(opr -> opr.getBelongsToField("resource").getId())
                .collect(Collectors.toList());

        fillFilterValue(view, product, componentsLocation, alreadyAddedResourceIds);

        ComponentState resourcePlannedQuantityUnit = view.getComponentByReference("planedQuantityUnit");
        LookupComponent resourceLookup = (LookupComponent) view.getComponentByReference("resource");
        if(resourceLookup.isEmpty()) {
            resourcePlannedQuantityUnit.setFieldValue(null);
        } else {
            resourcePlannedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));
        }
    }

    private void fillFilterValue(ViewDefinitionState view, Entity product, Entity componentsLocation, List<Long> alreadyAddedResourceIds) {
        LookupComponent resourceLookup = (LookupComponent) view.getComponentByReference("resource");
        FilterValueHolder filterValueHolder = resourceLookup.getFilterValue();

        if (Objects.nonNull(componentsLocation)) {
            filterValueHolder.put(ResourceCriteriaModifiersPFTD.L_LOCATION_ID, componentsLocation.getId());
            filterValueHolder.put(ResourceCriteriaModifiersPFTD.L_PRODUCT_ID, product.getId());
            filterValueHolder.put(ResourceCriteriaModifiersPFTD.L_RESOURCES_IDS, alreadyAddedResourceIds);
        } else {
            filterValueHolder.remove(ResourceCriteriaModifiersPFTD.L_LOCATION_ID);
            filterValueHolder.remove(ResourceCriteriaModifiersPFTD.L_PRODUCT_ID);
            filterValueHolder.remove(ResourceCriteriaModifiersPFTD.L_RESOURCES_IDS);
        }

        resourceLookup.setFilterValue(filterValueHolder);
        resourceLookup.requestComponentUpdateState();
    }
}
