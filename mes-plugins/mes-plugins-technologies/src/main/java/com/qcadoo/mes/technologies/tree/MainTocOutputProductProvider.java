package com.qcadoo.mes.technologies.tree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;

@Service
public class MainTocOutputProductProvider {

    private static final String PRODUCT_ALIAS = "product";

    private static final String TOC_MAIN_OUTPUT_PRODUCT_QUERY = "select prod as " + PRODUCT_ALIAS + "\n"
            + "from #technologies_operationProductOutComponent opoc\n left join opoc.operationComponent toc \n"
            + "left join toc.technology as tech\n left join toc.operationProductOutComponents opoc \n"
            + "left join toc.parent tocParent \n left join tocParent.operationProductInComponents parentOpic \n"
            + "left join opoc.product as prod \n where\n toc.id = :tocId\n and\n"
            + "( (tocParent is null and opoc.product = tech.product) "
            + " or (tocParent is not null and parentOpic.product = opoc.product) )";

    private final Function<Long, Optional<Entity>> findF = new Function<Long, Optional<Entity>>() {

        @Override
        public Optional<Entity> apply(final Long tocId) {
            return find(tocId);
        }
    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Returns function that transforms id of the technology operation into its main output product entity. Calling
     * #findAsFunction().apply(Long) is equivalent to calling #find(Long)
     * 
     * @return function that transforms id of the technology operation into its main output product entity.
     */
    public Function<Long, Optional<Entity>> findAsFunction() {
        return findF;
    }

    /**
     * Find main output product from a given technology operation.
     * 
     * @param tocId
     *            id of the technology operation for which we want to figure out main output product
     * @return main product
     */
    public Optional<Entity> find(final Long tocId) {
        if (tocId == null) {
            return Optional.absent();
        }
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        SearchQueryBuilder sqb = productDD.find(TOC_MAIN_OUTPUT_PRODUCT_QUERY);
        sqb.setLong("tocId", tocId);
        sqb.setMaxResults(1);
        return Optional.fromNullable(sqb.uniqueResult());
    }

}
