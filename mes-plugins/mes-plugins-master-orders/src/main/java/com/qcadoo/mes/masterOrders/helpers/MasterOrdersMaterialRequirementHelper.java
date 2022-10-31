package com.qcadoo.mes.masterOrders.helpers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersMaterialRequirementProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class MasterOrdersMaterialRequirementHelper {

	private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private DataDefinitionService dataDefinitionService;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private NumberService numberService;

	@Autowired
	private ProductQuantitiesService productQuantitiesService;

	@Autowired
	private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

	@Autowired
	private ProductStructureTreeService productStructureTreeService;

	@Autowired
	private MaterialFlowResourcesService materialFlowResourcesService;

	@Autowired
	private DeliveriesService deliveriesService;

	@Autowired
	private TechnologyService technologyService;

	public List<Entity> generateMasterOrdersMaterialRequirementProducts(final Entity masterOrdersMaterialRequirement, final List<Entity> masterOrders) {
		List<Entity> masterOrdersMaterialRequirementProducts = Lists.newArrayList();

		boolean includeComponents = masterOrdersMaterialRequirement.getBooleanField(MasterOrdersMaterialRequirementFields.INCLUDE_COMPONENTS);

		createMasterOrdersMaterialRequirementProducts(masterOrdersMaterialRequirementProducts, masterOrders, includeComponents);

		updateMasterOrdersMaterialRequirementProducts(masterOrdersMaterialRequirementProducts);

		return masterOrdersMaterialRequirementProducts;
	}

	private void createMasterOrdersMaterialRequirementProducts(final List<Entity> masterOrdersMaterialRequirementProducts,
															   final List<Entity> masterOrders, final boolean includeComponents) {
		for (Entity masterOrder : masterOrders) {
			List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);

			for (Entity masterOrderProduct : masterOrderProducts) {
				Entity masterOrderProductTechnology = masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY);
				Entity masterOrderProductProduct = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
				BigDecimal masterOrderQuantity = masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY);

				Entity technology = getTechnology(masterOrderProductTechnology, masterOrderProductProduct);

				if (Objects.nonNull(technology)) {

					Map<OperationProductComponentHolder, BigDecimal> neededQuantities;

					if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION) && includeComponents) {
						neededQuantities = productQuantitiesWithComponentsService
								.getNeededProductQuantities(technology, masterOrderProductProduct, masterOrderQuantity);
					} else {
						neededQuantities = productQuantitiesService
								.getNeededProductQuantities(technology, masterOrderProductProduct, masterOrderQuantity);
					}

					for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
						Entity product = neededProductQuantity.getKey().getProduct();
						Entity technologyInputProductType = neededProductQuantity.getKey().getTechnologyInputProductType();
						BigDecimal neededQuantity = neededProductQuantity.getValue();

						if (Objects.nonNull(product)) {
							createMasterOrdersMaterialRequirementProduct(masterOrdersMaterialRequirementProducts, product, technologyInputProductType,
									neededQuantity);
						}
					}
				}
			}
		}
	}

	private Entity getTechnology(final Entity technology, final Entity product) {
		if (Objects.isNull(technology)) {
			String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

			if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
				Entity parent = product.getBelongsToField(ProductFields.PARENT);

				if (Objects.nonNull(parent)) {
					return productStructureTreeService.findTechnologyForProduct(parent);
				} else {
					return productStructureTreeService.findTechnologyForProduct(product);
				}
			} else {
				return productStructureTreeService.findTechnologyForProduct(product);
			}
		}

		return technology;
	}

	private Entity createMasterOrdersMaterialRequirementProduct(final List<Entity> masterOrdersMaterialRequirementProducts, final Entity product, final Entity technologyInputProductType, final BigDecimal neededQuantity) {
		Optional<Entity> mayBeMasterOrdersMaterialRequirementProduct;

		if (Objects.nonNull(technologyInputProductType)) {
			mayBeMasterOrdersMaterialRequirementProduct = masterOrdersMaterialRequirementProducts.stream()
					.filter(masterOrdersMaterialRequirementProduct ->
							filterByProduct(masterOrdersMaterialRequirementProduct, product, MasterOrdersMaterialRequirementProductFields.QUANTITY)
									&& filterByTechnologyInputProductType(masterOrdersMaterialRequirementProduct, technologyInputProductType))
					.findFirst();
		} else {
			mayBeMasterOrdersMaterialRequirementProduct = masterOrdersMaterialRequirementProducts.stream()
					.filter(masterOrdersMaterialRequirementProduct ->
							filterByProduct(masterOrdersMaterialRequirementProduct, product, MasterOrdersMaterialRequirementProductFields.QUANTITY))
					.findFirst();
		}

		Entity masterOrdersMaterialRequirementProduct;

		if (mayBeMasterOrdersMaterialRequirementProduct.isPresent()) {
			masterOrdersMaterialRequirementProduct = mayBeMasterOrdersMaterialRequirementProduct.get();

			BigDecimal quantity = masterOrdersMaterialRequirementProduct
					.getDecimalField(MasterOrdersMaterialRequirementProductFields.QUANTITY);

			quantity = quantity.add(neededQuantity, numberService.getMathContext());

			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.QUANTITY, quantity);
		} else {
			masterOrdersMaterialRequirementProduct = getMasterOrdersMaterialRequirementProductDD().create();

			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.PRODUCT, product);
			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.TECHNOLOGY_INPUT_PRODUCT_TYPE, technologyInputProductType);
			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.QUANTITY, neededQuantity);

			masterOrdersMaterialRequirementProducts.add(masterOrdersMaterialRequirementProduct);
		}

		return masterOrdersMaterialRequirementProduct;
	}

	private boolean filterByProduct(final Entity masterOrdersMaterialRequirementProduct, final Entity product, final String fieldName) {
		Entity masterOrdersMaterialRequirementProductProduct = masterOrdersMaterialRequirementProduct
				.getBelongsToField(MasterOrdersMaterialRequirementProductFields.PRODUCT);

		return Objects.nonNull(masterOrdersMaterialRequirementProductProduct)
				&& Objects.nonNull(masterOrdersMaterialRequirementProduct.getField(fieldName))
				&& Objects.nonNull(product)
				&& masterOrdersMaterialRequirementProductProduct.getId().equals(product.getId());
	}

	private boolean filterByTechnologyInputProductType(final Entity masterOrdersMaterialRequirementProduct,
													   final Entity technologyInputProductType) {
		Entity masterOrdersMaterialRequirementProductTechnologyInputProductType = masterOrdersMaterialRequirementProduct
				.getBelongsToField(MasterOrdersMaterialRequirementProductFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);

		return Objects.nonNull(masterOrdersMaterialRequirementProductTechnologyInputProductType)
				&& Objects.nonNull(technologyInputProductType)
				&& masterOrdersMaterialRequirementProductTechnologyInputProductType.getId().equals(technologyInputProductType.getId());
	}

	private void updateMasterOrdersMaterialRequirementProducts(final List<Entity> masterOrdersMaterialRequirementProducts) {
		List<Entity> products = getMasterOrdersMaterialRequirementProducts(masterOrdersMaterialRequirementProducts);

		Set<Long> parentIds = getParentIds(products);
		Set<Long> productIds = getProductIds(products);

		Map<Long, Map<Long, BigDecimal>> resourceStocks = getResourceStocks(products);
		List<Entity> companyProducts = deliveriesService.getCompanyProducts(productIds);
		List<Entity> companyProductsFamilies = deliveriesService.getCompanyProducts(parentIds);
		Map<Long, BigDecimal> neededQuantitiesFromOrders = getNeededQuantitiesFromOrders(productIds);

		for (Entity masterOrdersMaterialRequirementProduct : masterOrdersMaterialRequirementProducts) {
			Entity product = masterOrdersMaterialRequirementProduct
					.getBelongsToField(MasterOrdersMaterialRequirementProductFields.PRODUCT);

			Long productId = product.getId();
			Entity parent = product.getBelongsToField(ProductFields.PARENT);

			BigDecimal currentStock = BigDecimalUtils.convertNullToZero(getCurrentStock(resourceStocks, productId));
			BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(neededQuantitiesFromOrders.get(productId));

			if (neededQuantity.compareTo(BigDecimal.ZERO) < 0) {
				neededQuantity = BigDecimal.ZERO;
			}

			Optional<Entity> mayBeCompanyProduct = deliveriesService.getCompanyProduct(companyProducts, productId);

			Entity supplier = null;

			if (mayBeCompanyProduct.isPresent()) {
				Entity companyProduct = mayBeCompanyProduct.get();

				supplier = companyProduct.getBelongsToField(CompanyProductFields.COMPANY);
			} else {
				if (Objects.nonNull(parent)) {
					Optional<Entity> mayBeCompanyProductsFamily = deliveriesService.getCompanyProduct(companyProductsFamilies,
							parent.getId());

					if (mayBeCompanyProductsFamily.isPresent()) {
						Entity companyProductsFamily = mayBeCompanyProductsFamily.get();

						supplier = companyProductsFamily.getBelongsToField(CompanyProductFields.COMPANY);
					}
				}
			}

			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.CURRENT_STOCK, currentStock);
			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.NEEDED_QUANTITY,
					neededQuantity);
			masterOrdersMaterialRequirementProduct.setField(MasterOrdersMaterialRequirementProductFields.SUPPLIER, supplier);
		}
	}

	public List<Entity> getMasterOrdersMaterialRequirementProducts(final List<Entity> masterOrdersMaterialRequirementProducts) {
		return masterOrdersMaterialRequirementProducts.stream()
				.map(masterOrdersMaterialRequirementProduct -> masterOrdersMaterialRequirementProduct
						.getBelongsToField(MasterOrdersMaterialRequirementProductFields.PRODUCT))
				.collect(Collectors.toList());
	}

	public Set<Long> getParentIds(final List<Entity> products) {
		return products.stream().filter(product -> Objects.nonNull(product.getBelongsToField(ProductFields.PARENT)))
				.map(product -> product.getBelongsToField(ProductFields.PARENT).getId()).collect(Collectors.toSet());
	}

	public Set<Long> getProductIds(final List<Entity> products) {
		return products.stream().map(Entity::getId).collect(Collectors.toSet());
	}

	private Map<Long, Map<Long, BigDecimal>> getResourceStocks(final List<Entity> products) {
		Map<Long, Map<Long, BigDecimal>> resourceStocks = Maps.newHashMap();

		if (!products.isEmpty()) {
			List<Entity> locations = materialFlowResourcesService.getWarehouseLocationsFromDB();

			resourceStocks = materialFlowResourcesService.getQuantitiesForProductsAndLocations(products, locations);
		}

		return resourceStocks;
	}

	private BigDecimal getCurrentStock(final Map<Long, Map<Long, BigDecimal>> resourceStocks, final Long productId) {
		BigDecimal currentStock = BigDecimal.ZERO;

		for (Map.Entry<Long, Map<Long, BigDecimal>> resourceStock : resourceStocks.entrySet()) {
			currentStock = currentStock.add(BigDecimalUtils.convertNullToZero(resourceStock.getValue().get(productId)),
					numberService.getMathContext());
		}

		return currentStock;
	}

	private Map<Long, BigDecimal> getNeededQuantitiesFromOrders(final Set<Long> productIds) {
		Map<Long, BigDecimal> neededQuantitiesFromOrders = Maps.newHashMap();

		if (!productIds.isEmpty()) {
			StringBuilder queryBuilder = new StringBuilder();

			queryBuilder.append("SELECT ");
			queryBuilder.append("productid,  SUM(COALESCE(plannedquantity, 0) - COALESCE(usedquantity, 0)) AS neededQuantity ");
			queryBuilder.append("FROM basicproductioncounting_productioncountingquantitydto ");
			queryBuilder.append("WHERE productid IN (:productIds) ");
			queryBuilder.append("AND orderid IN ( ");
			queryBuilder.append("SELECT id FROM orders_order WHERE state NOT IN ('01pending', '04completed', '05declined', '07abandoned') ");
			queryBuilder.append(") ");
			queryBuilder.append("GROUP BY productid");

			Map<String, Object> params = Maps.newHashMap();

			params.put("productIds", productIds);

			try {
				List<Map<String, Object>> values = jdbcTemplate.queryForList(queryBuilder.toString(), params);

				for (Map<String, Object> value : values) {
					Long productId = Long.valueOf(value.get("productId").toString());
					BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(value.get("neededQuantity"));

					neededQuantitiesFromOrders.put(productId, neededQuantity);
				}
			} catch (EmptyResultDataAccessException e) {
				return neededQuantitiesFromOrders;
			}
		}

		return neededQuantitiesFromOrders;
	}

	private DataDefinition getMasterOrdersMaterialRequirementProductDD() {
		return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
				MasterOrdersConstants.MODEL_MASTER_ORDERS_MATERIAL_REQUIREMENT_PRODUCT);
	}

}
