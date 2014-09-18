package com.qcadoo.mes.technologies.tree.dataProvider;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;

public class TechnologyRawInputProductComponentsCriteria {

    public static final String PRODUCT_ALIAS = "product_alias";

    private final Long technologyId;

    private Optional<SearchCriterion> productCriteria = Optional.absent();

    private Optional<SearchProjection> searchProjection = Optional.absent();

    private Optional<SearchOrder> searchOrder = Optional.absent();

    public Optional<SearchCriterion> getProductCriteria() {
        return productCriteria;
    }

    public static TechnologyRawInputProductComponentsCriteria forTechnology(final Long technologyId) {
        return new TechnologyRawInputProductComponentsCriteria(technologyId);
    }

    private TechnologyRawInputProductComponentsCriteria(final Long technologyId) {
        Preconditions.checkArgument(technologyId != null, "Technology id cannot be null.");
        this.technologyId = technologyId;
    }

    public Long getTechnologyId() {
        return technologyId;
    }

    public TechnologyRawInputProductComponentsCriteria setProductCriteria(final SearchCriterion productCriteria) {
        this.productCriteria = Optional.fromNullable(productCriteria);
        return this;
    }

    public Optional<SearchProjection> getSearchProjection() {
        return searchProjection;
    }

    public TechnologyRawInputProductComponentsCriteria setSearchProjection(final SearchProjection searchProjection) {
        this.searchProjection = Optional.fromNullable(searchProjection);
        return this;
    }

    public Optional<SearchOrder> getSearchOrder() {
        return searchOrder;
    }

    public TechnologyRawInputProductComponentsCriteria setSearchOrder(final SearchOrder searchOrder) {
        this.searchOrder = Optional.fromNullable(searchOrder);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        TechnologyRawInputProductComponentsCriteria rhs = (TechnologyRawInputProductComponentsCriteria) obj;
        return new EqualsBuilder().append(this.technologyId, rhs.technologyId).append(this.productCriteria, rhs.productCriteria)
                .append(this.searchProjection, rhs.searchProjection).append(this.searchOrder, rhs.searchOrder).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(technologyId).append(productCriteria).append(searchProjection).append(searchOrder)
                .toHashCode();
    }
}
