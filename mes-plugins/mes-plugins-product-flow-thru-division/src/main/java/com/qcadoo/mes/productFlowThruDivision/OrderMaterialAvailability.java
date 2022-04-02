package com.qcadoo.mes.productFlowThruDivision;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.AvailabilityOfMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderMaterialAvailability {

	private static final int REQUIRED_QUANTITY_SCALE = 5;

	@Autowired
	private DataDefinitionService dataDefinitionService;

	@Autowired
	private NumberService numberService;

	@Autowired
	private BasicProductionCountingService basicProductionCountingService;

	@Autowired
	private MaterialFlowResourcesService materialFlowResourcesService;

	public List<Entity> generateMaterialAvailabilityForOrder(final Entity order) {
		Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

		if (Objects.isNull(order.getId()) || Objects.isNull(technology)) {
			return Collections.EMPTY_LIST;
		}

		List<Entity> materialsAvailability = createMaterialAvailability(order);

		updateQuantityAndAvailability(materialsAvailability);

		return materialsAvailability;
	}

	public List<Entity> generateAndSaveMaterialAvailabilityForOrder(final Entity order) {
		Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

		if (Objects.isNull(order.getId()) || Objects.isNull(technology)) {
			return Lists.newArrayList();
		}

		deleteMaterialAvailability(order);

		List<Entity> materialsAvailability = createMaterialAvailability(order);

		updateQuantityAndAvailability(materialsAvailability);

		saveMaterialAvailability(materialsAvailability);

		return materialsAvailability;
	}

	private void deleteMaterialAvailability(final Entity order) {
		EntityList orderMaterialAvailability = order.getHasManyField(OrderFieldsPFTD.MATERIAL_AVAILABILITY);

		if (!orderMaterialAvailability.isEmpty()) {
			DataDefinition materialAvailabilityDD = getOrderMaterialAvailabilityDD();

			for (Entity materialAvailability : orderMaterialAvailability) {
				materialAvailabilityDD.delete(materialAvailability.getId());
			}
		}
	}

	private List<Entity> createMaterialAvailability(final Entity order) {
		List<Entity> materialsAvailability;

		materialsAvailability = createMaterialAvailabilityFromProductionCountingQuantities(order);

		order.setField(OrderFieldsPFTD.MATERIAL_AVAILABILITY, materialsAvailability);

		return materialsAvailability;
	}

	private List<Entity> createMaterialAvailabilityFromProductionCountingQuantities(final Entity order) {
		List<Entity> newOrderMaterialAvailability = Lists.newArrayList();

		List<Entity> usedMaterials = basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order);

		DataDefinition orderMaterialAvailabilityDD = getOrderMaterialAvailabilityDD();

		usedMaterials = usedMaterials
				.stream()
				.filter(productionCountingQuantity -> productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE).equals(
						ProductionCountingQuantityRole.USED.getStringValue())
						&& productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
						ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()))
				.collect(Collectors.toList());

		Map<Long, Map<Long, List<Entity>>> groupedMaterials = usedMaterials.stream().collect(
				Collectors.groupingBy(
						productionCountingQuantity -> ((Entity) productionCountingQuantity).getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
						Collectors.groupingBy(productionCountingQuantity -> ((Entity) productionCountingQuantity).getBelongsToField(
								ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION).getId())));

		for (Map.Entry<Long, Map<Long, List<Entity>>> productEntry : groupedMaterials.entrySet()) {
			Map<Long, List<Entity>> materialsForProduct = productEntry.getValue();

			for (Map.Entry<Long, List<Entity>> warehouseEntry : materialsForProduct.entrySet()) {
				if (!warehouseEntry.getValue().isEmpty()) {
					Entity baseUsedMaterial = warehouseEntry.getValue().get(0);
					BigDecimal totalQuantity = warehouseEntry.getValue().stream()
							.map(productionCountingQuantity -> productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY))
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					Entity product = baseUsedMaterial.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
					Entity location = baseUsedMaterial
							.getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

					Map<Long, Entity> batchesById = Maps.newHashMap();

					for (Entity productionCountingQuantity : warehouseEntry.getValue()) {
						for (Entity batch : productionCountingQuantity.getHasManyField(ProductionCountingQuantityFields.BATCHES)) {
							batchesById.put(batch.getId(), batch);
						}
					}

					newOrderMaterialAvailability.add(createMaterialAvailability(orderMaterialAvailabilityDD, product,
							order, totalQuantity, location, batchesById.values()));
				}
			}
		}

		return newOrderMaterialAvailability;
	}

	private Entity createMaterialAvailability(final DataDefinition orderMaterialAvailabilityDD, final Entity product,
											  final Entity order, final BigDecimal requiredQuantity, final Entity location, final Collection<Entity> batchesList) {
		Entity materialAvailability = orderMaterialAvailabilityDD.create();

		List<Entity> replacements = product.getDataDefinition().get(product.getId())
				.getHasManyField(ProductFields.SUBSTITUTE_COMPONENTS);

		String batches = batchesList.stream()
				.map(batch -> batch.getStringField(BatchFields.NUMBER))
				.collect(Collectors.joining(", "));
		String batchesId = batchesList.stream()
				.map(batch -> batch.getId().toString())
				.collect(Collectors.joining(","));

		materialAvailability.setField(MaterialAvailabilityFields.ORDER, order);
		materialAvailability.setField(MaterialAvailabilityFields.PRODUCT, product);
		materialAvailability.setField(MaterialAvailabilityFields.UNIT, product.getField(ProductFields.UNIT));
		materialAvailability.setField(MaterialAvailabilityFields.REQUIRED_QUANTITY,
				numberService.setScaleWithDefaultMathContext(requiredQuantity, REQUIRED_QUANTITY_SCALE));
		materialAvailability.setField(MaterialAvailabilityFields.LOCATION, location);

		if (!replacements.isEmpty()) {
			materialAvailability.setField(MaterialAvailabilityFields.REPLACEMENT, true);
		}

		materialAvailability.setField(MaterialAvailabilityFields.BATCHES, batches);
		materialAvailability.setField(MaterialAvailabilityFields.BATCHES_ID, batchesId);
		materialAvailability.setField(MaterialAvailabilityFields.BATCHES_QUANTITY, getBatchesQuantity(batchesList, product, location));

		return materialAvailability;
	}

	private BigDecimal getBatchesQuantity(final Collection<Entity> batches, final Entity product, final Entity location) {
		BigDecimal batchesQuantity = BigDecimal.ZERO;

		if (!batches.isEmpty()) {
			SearchCriteriaBuilder searchCriteriaBuilder =
					getResourceDD().find()
							.createAlias(ResourceFields.PRODUCT, ResourceFields.PRODUCT, JoinType.LEFT)
							.createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.LEFT)
							.createAlias(ResourceFields.BATCH, ResourceFields.BATCH, JoinType.LEFT)
							.add(SearchRestrictions.eq(ResourceFields.PRODUCT + "." + "id", product.getId()))
							.add(SearchRestrictions.eq(ResourceFields.LOCATION + "." + "id", location.getId()))
							.add(SearchRestrictions.in(ResourceFields.BATCH + "." + "id", batches.stream().map(Entity::getId).collect(Collectors.toList())))
							.setProjection(list().add(alias(sum(ResourceFields.AVAILABLE_QUANTITY), "sum")).add(rowCount()))
							.addOrder(asc("sum"));

			Entity resource = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

			if (Objects.nonNull(resource)) {
				batchesQuantity = resource.getDecimalField("sum");
			}
		}

		return batchesQuantity;
	}

	private void updateQuantityAndAvailability(final List<Entity> materialsAvailability) {
		Map<Long, Map<Long, BigDecimal>> availableComponents = prepareAvailableComponents(materialsAvailability);

		for (Entity materialAvailability : materialsAvailability) {
			Entity location = materialAvailability.getBelongsToField(MaterialAvailabilityFields.LOCATION);

			if (availableComponents.containsKey(location.getId())) {
				Entity product = materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT);

				Map<Long, BigDecimal> availableComponentsInLocation = availableComponents.get(location.getId());

				if (availableComponentsInLocation.containsKey(product.getId())) {
					BigDecimal availableQuantity = availableComponentsInLocation.get(product.getId());

					if (availableQuantity.compareTo(materialAvailability
							.getDecimalField(MaterialAvailabilityFields.REQUIRED_QUANTITY)) >= 0) {
						materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
								AvailabilityOfMaterialAvailability.FULL.getStrValue());
					} else if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
						materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
								AvailabilityOfMaterialAvailability.NONE.getStrValue());
					} else {
						materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
								AvailabilityOfMaterialAvailability.PARTIAL.getStrValue());
					}

					materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, availableQuantity);
				} else {
					materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
							AvailabilityOfMaterialAvailability.NONE.getStrValue());
					materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
				}
			} else {
				materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
						AvailabilityOfMaterialAvailability.NONE.getStrValue());
				materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
			}
		}
	}

	private void saveMaterialAvailability(final List<Entity> materialsAvailability) {
		DataDefinition orderMaterialAvailabilityDD = getOrderMaterialAvailabilityDD();

		for (Entity materialAvailability : materialsAvailability) {
			orderMaterialAvailabilityDD.save(materialAvailability);
		}
	}

	private Map<Long, Map<Long, BigDecimal>> prepareAvailableComponents(final List<Entity> materialAvailabilities) {
		Map<Entity, Set<Entity>> groupedMaterialAvailabilities = Maps.newHashMap();

		materialAvailabilities.forEach(materialAvailability -> {
			Entity location = materialAvailability.getBelongsToField(MaterialAvailabilityFields.LOCATION);

			if (groupedMaterialAvailabilities.containsKey(location)) {
				groupedMaterialAvailabilities.get(location).add(
						materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT));
			} else {
				groupedMaterialAvailabilities.put(location,
						Sets.newHashSet(materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT)));
			}
		});

		Map<Long, Map<Long, BigDecimal>> availability = Maps.newHashMap();

		for (Map.Entry<Entity, Set<Entity>> entry : groupedMaterialAvailabilities.entrySet()) {
			if (Objects.nonNull(entry.getKey())) {
				Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
						Lists.newArrayList(entry.getValue()), entry.getKey());

				availability.put(entry.getKey().getId(), availableQuantities);
			}
		}

		return availability;
	}

	private DataDefinition getResourceDD() {
		return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
	}

	private DataDefinition getOrderMaterialAvailabilityDD() {
		return dataDefinitionService.get(
				ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);
	}

}
