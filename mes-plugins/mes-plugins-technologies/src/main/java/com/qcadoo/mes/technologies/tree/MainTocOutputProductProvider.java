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

    private static final String TOC_MAIN_OUTPUT_PRODUCT_QUERY = "select \n" + "opoc.product as " + PRODUCT_ALIAS + "\n"
            + "from #technologies_operationProductOutComponent opoc,\n" + "#technologies_operationProductInComponent opic\n"
            + "inner join opoc.operationComponent toc\n" + "inner join toc.parent parentToc\n" + "where \n" + "toc.id=:tocId\n"
            + "and \n" + "opoc.product.id = opic.product.id\n" + "and opic.operationComponent.id = parentToc.id";

    private final Function<Long, Optional<Entity>> findF;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public MainTocOutputProductProvider() {
        this.findF = new Function<Long, Optional<Entity>>() {

            @Override
            public Optional<Entity> apply(final Long tocId) {
                if (tocId == null) {
                    return Optional.absent();
                }
                return find(tocId);
            }
        };
    }

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
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        SearchQueryBuilder sqb = productDD.find(TOC_MAIN_OUTPUT_PRODUCT_QUERY);
        sqb.setLong("tocId", tocId);
        sqb.setMaxResults(1);
        return Optional.fromNullable(sqb.uniqueResult());
    }

}
