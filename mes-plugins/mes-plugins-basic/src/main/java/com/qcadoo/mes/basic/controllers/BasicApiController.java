package com.qcadoo.mes.basic.controllers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.requests.ProductRequest;
import com.qcadoo.mes.basic.controllers.dataProvider.requests.WorkstationRequest;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.ProductResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.ProductsGridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationTypesResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationsGridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationsResponse;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class BasicApiController {

    @Autowired
    private DataProvider dataProvider;

    @Autowired
    private ProductService productService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @ResponseBody
    @RequestMapping(value = "/workstations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkstationsResponse getWorkstations(@RequestParam("query") String query) {
        return dataProvider.getWorkstations(query);
    }

    @ResponseBody
    @RequestMapping(value = "/workstationTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkstationTypesResponse getWorkstationTypes() {
        return dataProvider.getWorkstationTypes();
    }

    @ResponseBody
    @RequestMapping(value = "/workstation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkstationResponse saveWorkstation(@RequestBody WorkstationRequest workstation) {

        Entity workstationEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION).create();
        workstationEntity.setField(WorkstationFields.NUMBER, workstation.getNumber());
        workstationEntity.setField(WorkstationFields.NAME, workstation.getName());
        workstationEntity.setField(WorkstationFields.WORKSTATION_TYPE, workstation.getType());

        workstationEntity = workstationEntity.getDataDefinition().save(workstationEntity);
        if(workstationEntity.isValid()) {
            WorkstationResponse workstationResponse = new WorkstationResponse(WorkstationResponse.StatusCode.OK);
            workstationResponse.setId(workstationEntity.getId());
            workstationResponse.setNumber(workstation.getNumber());
            workstationResponse.setName(workstation.getName());
            return workstationResponse;
        } else {
            //
            ErrorMessage numberError = workstationEntity.getError(WorkstationFields.NUMBER);
            if(Objects.nonNull(numberError) && numberError.getMessage().equals("qcadooView.validate.field.error.duplicated")) {
                WorkstationResponse response = new WorkstationResponse(WorkstationResponse.StatusCode.ERROR);
                response.setMessage(translationService.translate("basic.dashboard.operationalTasksDefinitionWizard.error.validationError.workstationDuplicated",
                        LocaleContextHolder.getLocale()));
                return response;
            }

        }
        WorkstationResponse response = new WorkstationResponse(WorkstationResponse.StatusCode.ERROR);
        response.setMessage(translationService.translate("basic.dashboard.operationalTasksDefinitionWizard.error.validationError.workstationErrors",
                LocaleContextHolder.getLocale()));
        return response;
    }


    @ResponseBody
    @RequestMapping(value = "/workstationsByPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkstationsGridResponse getWorkstations(@RequestParam(value = "limit") int limit, @RequestParam(value = "offset") int offset,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "search", required = false) String search) {
        return dataProvider.getWorkstations(limit, offset, sort, order, search);
    }

    @ResponseBody
    @RequestMapping(value = "/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getProductsByQuery(@RequestParam("query") String query) {
        return dataProvider.getProductsResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(value = "/productsTypeahead", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getProductsTypeahead(@RequestParam("query") String query) {
        return dataProvider.getProductsTypeahead(query);
    }

    @ResponseBody
    @RequestMapping(value = "/productsByPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsGridResponse getProducts(@RequestParam(value = "limit") int limit, @RequestParam(value = "offset") int offset,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "search", required = false) String search) {
        return dataProvider.getProductsResponse(limit, offset, sort, order, search);
    }

    @ResponseBody
    @RequestMapping(value = "/product", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse saveProduct(@RequestBody ProductRequest product) {

        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).create();
        productEntity.setField(ProductFields.NUMBER, product.getNumber());
        productEntity.setField(ProductFields.NAME, product.getName());
        productEntity.setField(ProductFields.UNIT, product.getUnit());
        productEntity.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        productEntity.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL, product.getGlobalTypeOfMaterial());
        productEntity = productEntity.getDataDefinition().save(productEntity);
        if(productEntity.isValid()) {
            ProductResponse productResponse = new ProductResponse(ProductResponse.StatusCode.OK);
            productResponse.setId(productEntity.getId());
            productResponse.setNumber(product.getNumber());
            productResponse.setName(product.getName());
            productResponse.setUnit(product.getUnit());
            return productResponse;
        } else {
            //
            ErrorMessage numberError = productEntity.getError(ProductFields.NUMBER);
            if(Objects.nonNull(numberError) && numberError.getMessage().equals("qcadooView.validate.field.error.duplicated")) {
                ProductResponse response = new ProductResponse(ProductResponse.StatusCode.ERROR);
                response.setMessage(translationService.translate("basic.dashboard.orderDefinitionWizard.error.validationError.productDuplicated",
                        LocaleContextHolder.getLocale()));
                return response;
            }

        }
        ProductResponse response = new ProductResponse(ProductResponse.StatusCode.ERROR);
        response.setMessage(translationService.translate("basic.dashboard.orderDefinitionWizard.error.validationError.productErrors",
                LocaleContextHolder.getLocale()));
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/additionalcodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getAdditionalCodesByQuery(@RequestParam("query") String query,
            @RequestParam(required = false, value = "productnumber") String productnumber) {
        return dataProvider.getAdditionalCodesResponseByQuery(query, productnumber);
    }

    @ResponseBody
    @RequestMapping(value = "/palletnumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getPalletNumbersByQuery(@RequestParam("query") String query) {
        return dataProvider.getPalletNumbersResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(value = "/attribute/{attr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getAttributesByQuery(@PathVariable String attr, @RequestParam("query") String query,
            HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        String requestURI = httpServletRequest.getRequestURI();
        URI uri = URI.create(requestURI);
        Path path = Paths.get(uri.getPath());
        String last = path.getFileName().toString();
        return dataProvider.getAttributesByQuery(last, query);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/units")
    public List<Map<String, String>> getUnits() {
        return dataProvider.getUnits();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/typeOfPallets")
    public List<Map<String, String>> getTypeOfPallets() {
        return dataProvider.getTypeOfPallets();
    }
}
