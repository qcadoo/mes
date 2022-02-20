/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.costNorms;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.LogService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.CostNormsLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.mes.materialFlowResources.costNorms.dao.CostNormsDao;
import com.qcadoo.mes.materialFlowResources.costNorms.dao.model.CostNorm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

@Service
public class CostNormsServiceImpl implements CostNormsService {

	private static final String L_COST_NORMS = "costNorms";

	private static final String L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_INFO = "materialFlowResources.updateCostNorms.info";

	private static final String L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_START_UPDATE = "materialFlowResources.updateCostNorms.startUpdate";

	private static final String L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_STOP_UPDATE = "materialFlowResources.updateCostNorms.stopUpdate";

	@Autowired
	private MultiTenantService multiTenantService;

	@Autowired
	private TranslationService translationService;

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private LogService logService;

	@Autowired
	private CostNormsDao costNormsDao;

	@Override
	public void updateCostNormsForProductsFromWarehouses(final List<Entity> products, final List<Entity> warehouses) {
		List<Long> productIds = products.stream().map(Entity::getId).collect(Collectors.toList());
		List<Long> warehousesIds = warehouses.stream().filter(Objects::nonNull).map(Entity::getId).collect(Collectors.toList());

		List<CostNorm> lastPurchases = costNormsDao.getLastPurchaseCostsForProducts(productIds, warehousesIds);
		List<CostNorm> averageCosts = costNormsDao.getAverageCostForProducts(productIds, warehousesIds);

		costNormsDao.updateCostNormsForProducts(mergeCostNorms(lastPurchases, averageCosts));
	}

	private Collection<CostNorm> mergeCostNorms(final List<CostNorm> lastPurchases, final List<CostNorm> averageCosts) {
		Map<Long, CostNorm> costNormMap = lastPurchases.stream()
				.collect(Collectors.toMap(CostNorm::getProductId, purchase -> purchase, (p, q) -> p));

		for (CostNorm averageCost : averageCosts) {
			CostNorm purchase = costNormMap.get(averageCost.getProductId());

			if (Objects.nonNull(purchase)) {
				purchase.setAverageCost(averageCost.getAverageCost());
				costNormMap.put(averageCost.getProductId(), purchase);
			} else {
				costNormMap.put(averageCost.getProductId(), averageCost);
			}
		}

		return costNormMap.values();
	}

	public void automaticCostNormsTrigger() {
		multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

			@Override
			public void invoke() {
				automaticCostNorms();
			}

		});
	}

	private void automaticCostNorms() {
		Entity parameter = parameterService.getParameter();

		if (parameter.getBooleanField(ParameterFieldsMFR.AUTOMATIC_UPDATE_COST_NORMS)) {
			logService.add(LogService.Builder
					.info(L_COST_NORMS, translationService.translate(L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_INFO,
							LocaleContextHolder.getLocale())).withMessage(
							translationService.translate(L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_START_UPDATE,
									LocaleContextHolder.getLocale())));

			updateCostNorms(parameter);

			logService.add(LogService.Builder
					.info(L_COST_NORMS, translationService.translate(L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_INFO,
							LocaleContextHolder.getLocale())).withMessage(
							translationService.translate(L_MATERIAL_FLOW_RESOURCES_UPDATE_COST_NORMS_STOP_UPDATE,
									LocaleContextHolder.getLocale())));
		}
	}

	private void updateCostNorms(final Entity parameter) {
		String costsSource = parameter.getStringField(ParameterFieldsMFR.COSTS_SOURCE);

		if ("01mes".equals(costsSource)) {
			List<Entity> products = Lists.newArrayList();
			List<Entity> warehouses = parameter.getHasManyField(ParameterFieldsMFR.WAREHOUSES).stream()
					.map(warehouse -> warehouse.getBelongsToField(CostNormsLocationFields.LOCATION))
					.collect(Collectors.toList());

			updateCostNormsForProductsFromWarehouses(products, warehouses);
		}
	}

}
