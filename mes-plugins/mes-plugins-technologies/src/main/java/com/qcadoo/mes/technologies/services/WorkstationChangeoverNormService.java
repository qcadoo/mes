package com.qcadoo.mes.technologies.services;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormDtoFields;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WorkstationChangeoverNormService {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_COUNT = "count";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean hasWorkstationChangeoverNorms(final Entity workstation) {
        SearchCriteriaBuilder searchCriteriaBuilder = getWorkstationChangeoverNormDD().find();

        addWorkstationSearchRestrictions(searchCriteriaBuilder, workstation);

        Entity workstationChangeoverNorm = searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.ACTIVE, true))
                .setProjection(SearchProjections.alias(SearchProjections.countDistinct(L_ID), L_COUNT))
                .addOrder(SearchOrders.desc(L_COUNT)).setMaxResults(1).uniqueResult();

        Long countValue = (Long) workstationChangeoverNorm.getField(L_COUNT);

        return countValue > 0;
    }

    public List<Entity> findWorkstationChangeoverNorms(final Entity workstation, final Entity attribute) {
        SearchCriteriaBuilder searchCriteriaBuilder = getWorkstationChangeoverNormDD().find();

        addWorkstationSearchRestrictions(searchCriteriaBuilder, workstation);
        addAttributeSearchRestrictions(searchCriteriaBuilder, attribute);

        List<Entity> norms = searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.ACTIVE, true))
                .list().getEntities();
        return filterNorms(norms);
    }

    private List<Entity> filterNorms(List<Entity> norms) {
        if (norms.size() > 1) {
            List<Entity> workstationNorms = norms.stream().filter(e -> e.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION) != null).collect(Collectors.toList());
            if (workstationNorms.isEmpty()) {
                norms = norms.stream().filter(e -> e.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION_TYPE) != null).collect(Collectors.toList());
            } else {
                norms = workstationNorms;
            }
        }
        return norms;
    }

    private void addWorkstationSearchRestrictions(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                  final Entity workstation) {
        if (Objects.nonNull(workstation)) {
            Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);

            if (Objects.nonNull(workstationType)) {
                searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.WORKSTATION, WorkstationChangeoverNormFields.WORKSTATION, JoinType.LEFT);
                searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.WORKSTATION_TYPE, WorkstationChangeoverNormFields.WORKSTATION_TYPE, JoinType.LEFT);
                searchCriteriaBuilder.add(SearchRestrictions.or(
                        SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION + L_DOT + L_ID, workstation.getId()),
                        SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION_TYPE + L_DOT + L_ID, workstationType.getId())
                ));
            } else {
                searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.WORKSTATION, WorkstationChangeoverNormFields.WORKSTATION, JoinType.LEFT);
                searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION + L_DOT + L_ID, workstation.getId()));
            }
        }
    }

    private void addAttributeSearchRestrictions(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                final Entity attribute) {
        if (Objects.nonNull(attribute)) {
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.ATTRIBUTE, WorkstationChangeoverNormFields.ATTRIBUTE, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.ATTRIBUTE + L_DOT + L_ID, attribute.getId()));
        }
    }

    public List<Entity> getWorkstationChangeoverNormDtos(final Entity workstation) {
        Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);

        if (Objects.nonNull(workstationType)) {
            return getWorkstationChangeoverNormDtoDD().find().add(
                    SearchRestrictions.or(
                            SearchRestrictions.eq(WorkstationChangeoverNormDtoFields.WORKSTATION_ID, workstation.getId().intValue()),
                            SearchRestrictions.eq(WorkstationChangeoverNormDtoFields.WORKSTATION_TYPE_ID, workstationType.getId().intValue())
                    )
            ).list().getEntities();
        } else {
            return getWorkstationChangeoverNormDtoDD().find().add(
                    SearchRestrictions.eq(WorkstationChangeoverNormDtoFields.WORKSTATION_ID, workstation.getId().intValue())
            ).list().getEntities();
        }
    }

    public DataDefinition getWorkstationChangeoverNormDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_WORKSTATION_CHANGEOVER_NORM);
    }

    public DataDefinition getWorkstationChangeoverNormDtoDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_WORKSTATION_CHANGEOVER_NORM_DTO);
    }

}
