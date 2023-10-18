package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationHistoryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StorageLocationHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition storageLocationDD, final Entity storageLocation) {
        clearMaxNumberOfPallets(storageLocationDD, storageLocation);
        updateDefaultStorageLocation(storageLocationDD, storageLocation);
        addAuditChanges(storageLocationDD, storageLocation);
    }

    private void clearMaxNumberOfPallets(final DataDefinition storageLocationDD, final Entity storageLocation) {
        boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

        if (!placeStorageLocation) {
            storageLocation.setField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS, null);
        }
    }

    private void updateDefaultStorageLocation(final DataDefinition storageLocationDD, final Entity storageLocation) {
        Long storageLocationId = storageLocation.getId();
        Entity product = storageLocation.getBelongsToField(StorageLocationFields.PRODUCT);

        if (Objects.isNull(storageLocationId)) {
            if (Objects.nonNull(product)) {
                storageLocation.setField(StorageLocationFields.PRODUCTS, Lists.newArrayList(product));
            }
        } else {
            List<Entity> products = Lists.newArrayList(storageLocation.getHasManyField(StorageLocationFields.PRODUCTS));

            Entity storageLocationFromDB = storageLocation.getDataDefinition().get(storageLocation.getId());
            Entity productFromDB = storageLocationFromDB.getBelongsToField(StorageLocationFields.PRODUCT);

            if (Objects.nonNull(product)) {
                if (Objects.nonNull(productFromDB) && !product.getId().equals(productFromDB.getId())) {
                    products = products.stream().filter(p -> !p.getId().equals(productFromDB.getId())).collect(Collectors.toList());
                }

                if (products.stream().noneMatch(p -> p.getId().equals(product.getId()))) {
                    products.add(product);
                }
            } else {
                if (Objects.nonNull(productFromDB)) {
                    products = products.stream().filter(p -> !p.getId().equals(productFromDB.getId())).collect(Collectors.toList());
                }
            }

            storageLocation.setField(StorageLocationFields.PRODUCTS, products);
        }
    }

    private void addAuditChanges(DataDefinition storageLocationDD, Entity storageLocation) {
        Entity oldProduct;
        Entity newProduct = storageLocation.getBelongsToField(StorageLocationFields.PRODUCT);

        if (Objects.isNull(storageLocation.getId())) {
            oldProduct = null;
        } else {
            Entity storageLocationFromDb = storageLocationDD.get(storageLocation.getId());

            oldProduct = storageLocationFromDb.getBelongsToField(StorageLocationFields.PRODUCT);
        }

        if (Objects.isNull(oldProduct)) {
            if (Objects.nonNull(newProduct)) {
                auditChanges(storageLocation, null, newProduct);
            }
        } else {
            if (Objects.isNull(newProduct) || !oldProduct.getId().equals(newProduct.getId())) {
                auditChanges(storageLocation, oldProduct, newProduct);
            }
        }
    }

    private void auditChanges(final Entity storageLocation, final Entity oldProduct, final Entity newProduct) {
        Entity storageLocationHistory = getStorageLocationHistoryDD().create();

        storageLocationHistory.setField(StorageLocationHistoryFields.STORAGE_LOCATION, storageLocation);
        storageLocationHistory.setField(StorageLocationHistoryFields.PRODUCT_FROM, oldProduct);
        storageLocationHistory.setField(StorageLocationHistoryFields.PRODUCT_TO, newProduct);

        List<Entity> existingHistory = storageLocation.getHasManyField(StorageLocationFields.HISTORY);

        if (Objects.isNull(existingHistory)) {
            existingHistory = Lists.newArrayList();
        } else {
            existingHistory = Lists.newArrayList(existingHistory);
        }

        existingHistory.add(storageLocationHistory.getDataDefinition().save(storageLocationHistory));

        storageLocation.setField(StorageLocationFields.HISTORY, existingHistory);

        if (Objects.isNull(oldProduct) || (Objects.nonNull(newProduct) && !oldProduct.getId().equals(newProduct.getId()))) {
            Entity productToHistory = getProductStorageLocationHistoryDD().create();

            List<Entity> existingProductHistory = storageLocation.getHasManyField(StorageLocationFields.PRODUCT_TO_HISTORY);

            if (Objects.isNull(existingProductHistory)) {
                existingProductHistory = Lists.newArrayList();
            } else {
                existingProductHistory = Lists.newArrayList(existingProductHistory);
            }
            productToHistory.setField(StorageLocationHistoryFields.STORAGE_LOCATION_TO, storageLocation);
            productToHistory.setField(StorageLocationHistoryFields.PRODUCT, newProduct);
            productToHistory.setField(StorageLocationHistoryFields.LOCATION,
                    storageLocation.getBelongsToField(StorageLocationFields.LOCATION));

            existingProductHistory.add(productToHistory.getDataDefinition().save(productToHistory));

            storageLocation.setField(StorageLocationFields.PRODUCT_TO_HISTORY, existingProductHistory);
        }

        if (Objects.isNull(newProduct) || (Objects.nonNull(oldProduct) && !oldProduct.getId().equals(newProduct.getId()))) {
            Entity productFromHistory = getProductStorageLocationHistoryDD().create();

            List<Entity> existingProductHistory = storageLocation.getHasManyField(StorageLocationFields.PRODUCT_FROM_HISTORY);

            if (Objects.isNull(existingProductHistory)) {
                existingProductHistory = Lists.newArrayList();
            } else {
                existingProductHistory = Lists.newArrayList(existingProductHistory);
            }

            productFromHistory.setField(StorageLocationHistoryFields.STORAGE_LOCATION_FROM, storageLocation);
            productFromHistory.setField(StorageLocationHistoryFields.PRODUCT, oldProduct);
            productFromHistory.setField(StorageLocationHistoryFields.LOCATION,
                    storageLocation.getBelongsToField(StorageLocationFields.LOCATION));

            existingProductHistory.add(productFromHistory.getDataDefinition().save(productFromHistory));

            storageLocation.setField(StorageLocationFields.PRODUCT_FROM_HISTORY, existingProductHistory);
        }
    }

    private DataDefinition getStorageLocationHistoryDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION_HISTORY);
    }

    private DataDefinition getProductStorageLocationHistoryDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_PRODUCT_STORAGE_LOCATION_HISTORY);
    }

}
