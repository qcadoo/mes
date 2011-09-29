package com.qcadoo.mes.materialFlow;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_COMPANY;
import static com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants.MODEL_MATERIAL_FLOW_REPORT;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.print.pdf.MaterialFlowPdfService;
import com.qcadoo.mes.materialFlow.print.utils.EntityTransferComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class MaterialFlowReportService {
	
	@Autowired
	DataDefinitionService dataDefinitionService;
	
	@Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialFlowPdfService materialFlowPdfService;
	
    @Autowired
    private MaterialFlowService materialFlowService;
    
    @Value("${reportPath}")
    private String path;
        
	public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }
	
	public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIAL_FLOW_REPORT);
    }
	
	@SuppressWarnings("unchecked")
	public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {

            Entity materialFlowReportEntity = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());
            List<Entity> stockAreaComponents = (List<Entity>) materialFlowReportEntity.getField("stockAreas");
            
            if (materialFlowReportEntity.getField("generated") == null)
                materialFlowReportEntity.setField("generated", "0");

            if (stockAreaComponents.size() == 0) {
                generateButton.setMessage("materialFlow.ribbon.message.noStockAreas");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                if ((Boolean) materialFlowReportEntity.getField("generated")) {
                    generateButton.setMessage("materialFlow.ribbon.message.recordAlreadyGenerated");
                    generateButton.setEnabled(false);
                    deleteButton.setMessage("materialFlow.ribbon.message.recordAlreadyGenerated");
                    deleteButton.setEnabled(false);
                } else {
                    generateButton.setMessage(null);
                    generateButton.setEnabled(true);
                    deleteButton.setMessage(null);
                    deleteButton.setEnabled(true);
                }
            }
        }
        generateButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }
	
	public void setGridGenerateButtonState(final ViewDefinitionState state) {
        setGridGenerateButtonState(state, state.getLocale(), MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIAL_FLOW_REPORT);
    }
	
	public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
	            final String entityName) {
	    WindowComponent window = (WindowComponent) state.getComponentByReference("window");
	    GridComponent grid = (GridComponent) state.getComponentByReference("grid");
	    RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

	    if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
	    	deleteButton.setMessage(null);
	        deleteButton.setEnabled(false);
	    } else {
	        boolean canDelete = true;
	        for (Long entityId : grid.getSelectedEntitiesIds()) {
	                Entity materialFlowReportEntity = dataDefinitionService.get(plugin, entityName).get(entityId);

	                if ((Boolean) materialFlowReportEntity.getField("generated")) {
	                    canDelete = false;
	                        break;
	                }
	            }
	            if (canDelete) {
	                deleteButton.setMessage(null);
	                deleteButton.setEnabled(true);
	            } else {
	                deleteButton.setMessage("materialFlow.ribbon.message.selectedRecordAlreadyGenerated");
	                deleteButton.setEnabled(false);
	            }
	        }

	        deleteButton.requestUpdate(true);
	        window.requestRibbonRender();
	    }
	
	public void disableFormForExistingMaterialFlowReport(final ViewDefinitionState state) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState materialFlowForDate = state.getComponentByReference("materialFlowForDate");
        ComponentState materialFlowReportComponents = state.getComponentByReference("materialFlowReportComponents");
        FieldComponent generated = (FieldComponent) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            materialFlowForDate.setEnabled(false);
            materialFlowReportComponents.setEnabled(false);
        } else {
            name.setEnabled(true);
            materialFlowForDate.setEnabled(true);
        }
    }
    
    public boolean checkMaterialFlowComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity stockAreas = entity.getBelongsToField("stockAreas");
        Entity materialFlowReport = entity.getBelongsToField("materialFlowReport");

        if (materialFlowReport == null || stockAreas == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().belongsTo("stockAreas", stockAreas.getId())
                .belongsTo("materialFlowReport", materialFlowReport.getId()).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("stockAreas"),
                    "materialFlow.validate.global.error.materialFlowReportDuplicated");
            return false;
        } else {
            return true;
        }
    }
    
    public void generateMaterialFlow(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity materialFlowReport = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIAL_FLOW_REPORT).get(
                    (Long) state.getFieldValue());

            if (materialFlowReport == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(materialFlowReport.getStringField("fileName"))) {
                String message = translationService.translate(
                        "materialFlow.materialFlowReportDetails.window.materialRequirement.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (materialFlowReport.getHasManyField("stockAreas").isEmpty()) {
                String message = translationService.translate(
                        "materialFlow.materialFlowReportDetails.window.materialRequirement.missingAssosiatedStockAreas",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            materialFlowReport = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIAL_FLOW_REPORT).get(
                    (Long) state.getFieldValue());

            try {
                generateMaterialReqDocuments(state, materialFlowReport);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
    
    private void generateMaterialReqDocuments(final ComponentState state, final Entity materialFlowReport) throws IOException,
    	DocumentException {
    	Entity materialFlowWithFileName = updateFileName(materialFlowReport,
    			getFullFileName((Date) materialFlowReport.getField("date"), materialFlowReport.getStringField("name")),
        MaterialFlowConstants.MODEL_MATERIAL_FLOW_REPORT);
    	Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_COMPANY).find().uniqueResult();
    	materialFlowPdfService.generateDocument(materialFlowWithFileName, company, state.getLocale());
    	materialFlowPdfService.generateDocument(materialFlowWithFileName, company, state.getLocale());
    }
    
    private String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date);
    }
    
    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, entityName).save(entity);
    }

    public void printMaterialFlow(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity materialFlowReport = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIAL_FLOW_REPORT).get(
                    (Long) state.getFieldValue());
            if (materialFlowReport == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(materialFlowReport.getStringField("fileName"))) {
                state.addMessage(
                        translationService.translate(
                                "materialFlow.materialFlowReportDetails.window.materialRequirement.documentsWasNotGenerated",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/materialFlow/materialFlowReport." + args[0] + "?id=" + state.getFieldValue(), false,
                        false);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }
    
    // TODO: change logic to make it more efficient
    public Map<Entity, BigDecimal> createReportData(Entity materialFlowReport) {
        DataDefinition dataDefTransfer = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);
        
        //List<Entity> stockAreas = materialFlowReport.getHasManyField("stockAreas");
        List<Entity> stockAreas = new ArrayList<Entity>(materialFlowReport.getHasManyField("stockAreas"));
        Map<Entity, BigDecimal> reportData = new HashMap<Entity, BigDecimal>();
        
        
        for (Entity component : stockAreas) {
        	Entity stockArea = (Entity) component.getField("stockAreas");
	        String stockAreaId = stockArea.getField("number").toString(); //component.getId().toString();
	        
	        //List<Entity> transfers = dataDefTransfer
	          //      .find("where stockAreasTo.id = " + Long.toString(materialFlowReport.getBelongsToField("stockAreas").getId())).list()
	            //    .getEntities();
	        List<Entity> transfers = dataDefTransfer.find("where stockAreasTo.id = " + stockAreaId).list().getEntities();
	        Collections.sort(transfers, new EntityTransferComparator());
	
	        String stockAreasNumber = stockArea.getField("number").toString(); //component.getId().toString();//materialFlowReport.getBelongsToField("stockAreas").getId().toString();
	        String forDate = ((Date) materialFlowReport.getField("materialFlowForDate")).toString();
	
	        String numberBefore = "";
	        for (Entity transfer : transfers) {
	            String numberNow = transfer.getBelongsToField("product").getStringField("number");
	            if (!numberBefore.equals(numberNow)) {
	                BigDecimal quantity = materialFlowService.calculateShouldBe(stockAreasNumber,
	                        transfer.getBelongsToField("product").getStringField("number"), forDate);
	                reportData.put(transfer, quantity);
	                numberBefore = numberNow;
	            }
	        }

        }
        
        return reportData;
    }
}
