/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologiesGenerator;

import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GeneratorSettings {

    private final boolean fetchTechnologiesForComponents;

    private final boolean createAndSwitchProducts;

    private final boolean copyProductSize;

    private final boolean copyProductAttributes;

    private final Entity generationContext;

    public GeneratorSettings(final boolean fetchTechnologiesForComponents, final boolean createAndSwitchProducts,
                             final boolean copyProductSize, final boolean copyProductAttributes, final Entity generationContext) {
        this.fetchTechnologiesForComponents = fetchTechnologiesForComponents;
        this.createAndSwitchProducts = createAndSwitchProducts;
        this.copyProductSize = copyProductSize;
        this.copyProductAttributes = copyProductAttributes;
        this.generationContext = generationContext;
    }

    public boolean shouldFetchTechnologiesForComponents() {
        return fetchTechnologiesForComponents;
    }

    public boolean shouldCreateAndSwitchProducts() {
        return createAndSwitchProducts;
    }

    public boolean shouldCopyProductSize() {
        return copyProductSize;
    }

    public boolean shouldCopyProductAttributes() {
        return copyProductAttributes;
    }

    public Entity getGenerationContext() {
        return generationContext;
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

        GeneratorSettings rhs = (GeneratorSettings) obj;

        return new EqualsBuilder().append(this.fetchTechnologiesForComponents, rhs.fetchTechnologiesForComponents)
                .append(this.createAndSwitchProducts, rhs.createAndSwitchProducts)
                .append(this.copyProductSize, rhs.copyProductSize)
                .append(this.copyProductAttributes, rhs.copyProductAttributes).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fetchTechnologiesForComponents).append(createAndSwitchProducts)
                .append(copyProductSize).append(copyProductAttributes).toHashCode();
    }

    public static GeneratorSettings from(final Entity generationContextEntity, final Entity parameters) {
        boolean createAndSwitchProd = generationContextEntity.getBooleanField(GeneratorContextFields.CREATE_AND_SWAP_PRODUCTS);
        boolean fetchTechForComponents = generationContextEntity
                .getBooleanField(GeneratorContextFields.FETCH_TECHNOLOGIES_FOR_COMPONENTS);

        boolean technologiesGeneratorCopyProductSizes = parameters.getBooleanField(ParameterFieldsT.TECHNOLOGIES_GENERATOR_COPY_PRODUCT_SIZE);
        boolean technologiesGeneratorCopyProductAttributes = parameters.getBooleanField(ParameterFieldsT.TECHNOLOGIES_GENERATOR_COPY_PRODUCT_ATTRIBUTES);

        return new GeneratorSettings(fetchTechForComponents, createAndSwitchProd,
                technologiesGeneratorCopyProductSizes, technologiesGeneratorCopyProductAttributes,
                generationContextEntity);
    }

}
