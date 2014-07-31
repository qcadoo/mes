package com.qcadoo.mes.technologies.tree.traversing;

import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.eqField;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;
import static com.qcadoo.model.api.search.SearchRestrictions.or;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;

@Service
public class MainTocOutputProductCriteriaBuilder {

    public static final class Aliases {

        private Aliases() {
        }

        public static final String OPERATION_PROD_OUT_COMPONENT = "opoc_alias";

        public static final String OPERATION_OUTPUT_PRODUCT = "opocProd_alias";

        public static final String TOC = "toc_alias";

        public static final String TECHNOLOGY = "tech_alias";

        public static final String TOC_PARENT = "tocParent_alias";

        private static final String TOC_PARENT_OPIC = "tocParentOpic_alias";

        private static final String TOC_PARENT_INPUT_PRODUCT = "tocParentOpicProduct_alias";

        private static final String TECHNOLOGY_PRODUCT = "technologyProduct_alias";
    }

    @Autowired
    private DataDefinitionService dataDefinitionService;

    // HQL form:
    // ------------------------
    // select toc from
    // #technologies_technologyOperationComponent toc -- Aliases.TOC
    // inner join toc.operationProductOutComponents opoc -- Aliases.OPERATION_PROD_OUT_COMPONENT
    // inner join opoc.product as p -- Aliases.OPERATION_OUTPUT_PRODUCT
    // inner join toc.technology t -- Aliases.TECHNOLOGY
    // left join toc.parent parentToc -- Aliases.TOC_PARENT
    // left join parentToc.operationProductInComponents opic -- Aliases.TOC_PARENT_OPIC
    // left join opic.product opicProd -- Aliases.TOC_PARENT_INPUT_PRODUCT
    // left join t.product tProduct -- Aliases.TECHNOLOGY_PRODUCT
    // where
    // (opoc.product.id = opic.product.id or (toc.parent is null and t.product.id = p.id))
    public SearchCriteriaBuilder create(final MasterOutputProductCriteria criteria) {
        SearchCriteriaBuilder tocScb = getTocDataDefinition().findWithAlias(Aliases.TOC);
        applyCriteriaIfPresent(tocScb, criteria.getTocCriteria());

        SearchCriteriaBuilder opocScb = tocScb.createCriteria(
                TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, Aliases.OPERATION_PROD_OUT_COMPONENT,
                JoinType.INNER);
        applyCriteriaIfPresent(opocScb, criteria.getOpocCriteria());

        SearchCriteriaBuilder prodScb = opocScb.createCriteria(OperationProductOutComponentFields.PRODUCT,
                Aliases.OPERATION_OUTPUT_PRODUCT, JoinType.INNER);
        applyCriteriaIfPresent(prodScb, criteria.getProdCriteria());

        SearchCriteriaBuilder techScb = tocScb.createCriteria(TechnologyOperationComponentFields.TECHNOLOGY, Aliases.TECHNOLOGY,
                JoinType.INNER);
        applyCriteriaIfPresent(techScb, criteria.getTechCriteria());

        SearchCriteriaBuilder parentTocScb = tocScb.createCriteria(TechnologyOperationComponentFields.PARENT, Aliases.TOC_PARENT,
                JoinType.LEFT);
        applyCriteriaIfPresent(parentTocScb, criteria.getParentTocCriteria());

        SearchCriteriaBuilder parentOpicScb = parentTocScb.createCriteria(
                TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, Aliases.TOC_PARENT_OPIC, JoinType.LEFT);

        SearchCriteriaBuilder parentOpicProdScb = parentOpicScb.createCriteria(OperationProductInComponentFields.PRODUCT,
                Aliases.TOC_PARENT_INPUT_PRODUCT, JoinType.LEFT);
        applyCriteriaIfPresent(parentOpicScb, criteria.getParentOpicCriteria());

        SearchCriteriaBuilder techProductScb = techScb.createCriteria(TechnologyFields.PRODUCT, Aliases.TECHNOLOGY_PRODUCT,
                JoinType.INNER);

        SearchCriterion productIsConsumedByParentOp = eqField(Aliases.OPERATION_OUTPUT_PRODUCT + ".id",
                Aliases.TOC_PARENT_INPUT_PRODUCT + ".id");
        SearchCriterion opIsRootAndItsProductMatchTechProduct = and(isNull(Aliases.TOC + "."
                + TechnologyOperationComponentFields.PARENT),
                eqField(Aliases.TECHNOLOGY_PRODUCT + ".id", Aliases.OPERATION_OUTPUT_PRODUCT + ".id"));

        tocScb.add(or(productIsConsumedByParentOp, opIsRootAndItsProductMatchTechProduct));

        return tocScb;
    }

    private void applyCriteriaIfPresent(final SearchCriteriaBuilder scb, final Optional<SearchCriterion> criteria) {
        for (SearchCriterion searchCriterion : criteria.asSet()) {
            scb.add(searchCriterion);
        }
    }

    private DataDefinition getTocDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

}
