package com.qcadoo.mes.orderGroups;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orderGroups.constants.OrderGroupsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderGroupsService {

	@Autowired
	private DataDefinitionService dataDefinitionService;

	@Autowired
	private NumberGeneratorService numberGeneratorService;

	@Autowired
	private TranslationService translationService;

	/* ****** HOOKS ******* */
	
	public void generateNumberAndName(final ViewDefinitionState viewDefinitionState) {
		FieldComponent number = (FieldComponent) viewDefinitionState.getComponentByReference("number");

		if (StringUtils.hasText((String) number.getFieldValue())) {
			return;
		}

		numberGeneratorService.generateAndInsertNumber(viewDefinitionState, OrderGroupsConstants.PLUGIN_IDENTIFIER, OrderGroupsConstants.MODEL_ORDERGROUP, "form", "number");
		FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference("name");

		if (!StringUtils.hasText((String) name.getFieldValue())) {
			name.setFieldValue(translationService.translate("orderGroups.orderGroup.name.default", viewDefinitionState.getLocale(), (String) number.getFieldValue()));
		}
	}

	public void toggleRibbonAddOrderButtons(final ViewDefinitionState viewDefinitionState) {
		WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
		
		boolean value = (form.getEntityId() != null);
		
		Set<String> buttonReferences = new HashSet<String>(Arrays.asList("addManyOrders"));
		
		for(String reference : buttonReferences) {
			window.getRibbon().getGroupByName("ordersActions").getItemByName(reference).setEnabled(value);
		}
	}
		
	/* ****** CUSTOM EVENT LISTENER ****** */
	
	public void removeOrderFromGroup(final ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) {
		if ("orderGroupOrders".equals(componentState.getName())) {
			DataDefinition orderDataDefinition = dataDefinitionService.get("orders", "order");
			GridComponent grid = (GridComponent) componentState;	

			for (Long orderId : grid.getSelectedEntitiesIds()) {
				Entity order = orderDataDefinition.get(orderId);
				order.setField("orderGroup", null);
				orderDataDefinition.save(order);
			}
		}
	}
	
	public void showOrderGroupOrdersTable(final ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) { 
		String groupDateFrom = viewDefinitionState.getComponentByReference("groupDateFrom").getFieldValue().toString();
		String groupDateTo = viewDefinitionState.getComponentByReference("groupDateTo").getFieldValue().toString();
		
		JSONObject context = new JSONObject();
		try {
			context.put("orderGroup.id", componentState.getFieldValue());
			context.put("orderGroup.name", viewDefinitionState.getComponentByReference("name").getFieldValue());
			context.put("orderGroup.dateFrom", groupDateFrom.isEmpty() ? "..." : groupDateFrom);
			context.put("orderGroup.dateTo", groupDateTo.isEmpty() ? "..." : groupDateTo);
			context.put("orderGroup.TEST", viewDefinitionState.getComponentByReference("form").getFieldValue());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		StringBuilder url = new StringBuilder();
		url.append( "../page/orderGroups/orderGroupOrdersTable.html?context=");
		url.append(context.toString());

		viewDefinitionState.redirectTo(url.toString(), false, true);
	}
	
	public void addOrdersToGroup(final ViewDefinitionState viewDefinitionState, final ComponentState componentState, final String[] args) {
		Set<Long> orderIds = new HashSet<Long>();
		Set<Entity> orderEntities = new HashSet<Entity>();

		Long groupId = (Long) viewDefinitionState.getComponentByReference("orderGroup").getFieldValue();
		ComponentState orders = viewDefinitionState.getComponentByReference("orders");
		DataDefinition dd = dataDefinitionService.get("orders", "order");
		
		if(orders instanceof GridComponent) {
			orderIds = ((GridComponent)orders).getSelectedEntitiesIds();
		} else {
			orderIds.add((Long) viewDefinitionState.getComponentByReference("orders").getFieldValue());
		}
		
		// get orders entities
		for(Long orderId : orderIds) {
			Entity order = dd.get(orderId);
			order.setField("orderGroup", groupId);
			orderEntities.add(order);
		}

		// validate data ranges
		Entity group = dataDefinitionService.get(OrderGroupsConstants.PLUGIN_IDENTIFIER, OrderGroupsConstants.MODEL_ORDERGROUP).get(groupId);
		if(!checkOrderGroupComponentDateRange(group, orderEntities)) {
			viewDefinitionState.getComponentByReference("form")
				.addMessage(
					translationService.translate(
						OrderGroupsConstants.DATE_RANGE_ERROR, 
						viewDefinitionState.getLocale()
					), 
					MessageType.FAILURE
				);
			return;
		}

		// Save if validation pass
		for(Entity entity : orderEntities) {
			dd.save(entity);
		}
		
		// Redirect to 'successful' view (perform action like window.goBack)
		viewDefinitionState.redirectTo("/page/orderGroups/orderGroupDetails.html?context={\"form.id\":\"" + groupId + "\"}", false, false);		
	}

	
	/* ****** VALIDATORS ****** */

	public boolean validateDates(final DataDefinition dataDefinition,
			final Entity orderGroup) {
		Date dateFrom = (Date) orderGroup.getField("dateFrom");
		Date dateTo = (Date) orderGroup.getField("dateTo");
		if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
			orderGroup.addError(dataDefinition.getField("dateTo"),
					"orderGroups.validate.error.badDatesOrder");
			return false;
		}

		EntityList orders = orderGroup.getHasManyField("orders");
		if(orders != null && orders.size() > 0) {
			if(!checkOrderGroupComponentDateRange(orderGroup, orders)) {
				orderGroup.addError(dataDefinition.getField("dateFrom"), OrderGroupsConstants.DATE_RANGE_ERROR);
				orderGroup.addError(dataDefinition.getField("dateTo"), OrderGroupsConstants.DATE_RANGE_ERROR);
				return false;
			}
		}
		return true;
	}
	
	public boolean validateOrderDate(final DataDefinition dataDefinition, final Entity order) {
		Entity group = order.getBelongsToField("orderGroup");
		if(!checkOrderGroupComponentDateRange(group, Arrays.asList(order))) {
			order.addError(dataDefinition.getField("orderGroup"), OrderGroupsConstants.DATE_RANGE_ERROR);
		}
		return true;
	}
	
	public boolean checkOrderGroupComponentDateRange(final Entity group, final Iterable<Entity> orders) {
		Date groupDateTo = (Date) group.getField("dateTo");
		Date groupDateFrom = (Date) group.getField("dateFrom");		
		Date orderDateFrom;
		Date orderDateTo;
		
		if(groupDateTo != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date) group.getField("dateTo"));
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.MILLISECOND, -1); 
			groupDateTo = cal.getTime(); // important! change time to 23:59:59.99
		}
		
		for (Entity order : orders) {
			orderDateFrom = (Date) order.getField("dateFrom");
			orderDateTo = (Date) order.getField("dateTo");
			
			if ((groupDateFrom != null && orderDateFrom.compareTo(groupDateFrom) < 0) || 
					(groupDateTo != null && groupDateTo.compareTo(orderDateTo) < 0)) {
				return false;
			}
		}
		return true;
	}

}
