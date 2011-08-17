package com.qcadoo.mes.costNormsForProduct;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsForProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService; 
    
	/* ****** VIEW HOOKS ******* */
	
	public void fillCostTabUnit(final ViewDefinitionState viewDefinitionState) {
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
		FieldComponent costUnit = (FieldComponent) viewDefinitionState.getComponentByReference("costTabUnit");
		if(form == null || costUnit == null) {
		    return;
		}
		
		Entity product = dataDefinitionService.get("basic", "product").get(form.getEntityId());
	    // Entity product = dataDefinitionService.get("basic", "product").get(form.getEntityId());
		if(product == null) {
			return;
		}

		costUnit.setFieldValue(product.getStringField("unit"));
		costUnit.requestComponentUpdateState();
		costUnit.setEnabled(false);
	}

	public void fillCostTabCurrency(final ViewDefinitionState viewDefinitionState) {
		for(String componentReference : Arrays.asList("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency")) {
		    FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
		    field.setEnabled(true);
		    //temporary
		    field.setFieldValue("PLN");
		    field.setEnabled(false);
		    field.requestComponentUpdateState();
		}
	}

	public void fillInProductsGrid(final ViewDefinitionState viewDefinitionState) {
	    GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("inProductsGrid");
	    Long technologyId = ((FormComponent) viewDefinitionState.getComponentByReference("form")).getEntityId();
	    if (technologyId == null || grid == null) {
	        return;
	    }

	    // get all input products used in technology...
	    Entity technology = dataDefinitionService.get("technologies", "technology").get(technologyId);
	    EntityTree operations = technology.getTreeField("operationComponents");
//	    Set<Entity> inputProductsSet = new HashSet<Entity>();
//	    
//	    for (Entity operation : operations) {
//	        inputProductsSet.addAll(operation.getHasManyField("operationProductInComponents"));
//	    }
	    
//	    List<Entity> rows = new LinkedList<Entity>(inputProductsSet);
//	    Collections.sort(rows, new Comparator<Entity>() {
//            @Override
//            public int compare(Entity e1, Entity e2) {
//                
//                return 0;
//            }
//        });
//	    
//	    // ...and put them into the grid
//	    grid.setEntities(Collections.sort(inputProductsSet));	
//	    grid.setEntities(new LinkedList<Entity>(inputProductsSet));

	    DataDefinition dd = dataDefinitionService.get("technologies", "operationProductInComponent");
	    SearchResult searchResult = dd.find().add(SearchRestrictions.in("operationComponent", operations)).addOrder(SearchOrders.asc("number")).list();
	    grid.setEntities(searchResult.getEntities());

	}
	
	/* ****** CUSTOM EVENT LISTENER ****** */
	
	
	/* ****** VALIDATORS ****** */

	
}
