package com.qcadoo.mes.materialFlowResources.controllers;

import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.ResourceDTO;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/rest/documentPositions")
public class DocumentPositionsController {

    private static final String L_ATTRS_PREFIX = "attrs.";

    @Autowired
    private DocumentPositionService documentPositionService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<DocumentPositionDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord,
            @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage, DocumentPositionDTO positionDTO,HttpServletRequest request) {
        Map<String, String> attributeFilters = extractAttributesFilters(request);
        return documentPositionService.findAll(id, sidx, sord, page, perPage, positionDTO, attributeFilters);
    }



    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "units/{number}")
    public Map<String, Object> getUnitsForProduct(@PathVariable String number) throws UnsupportedEncodingException {
        String decodedNumber = new String(BaseEncoding.base64Url().decode(number), "utf-8");

        return documentPositionService.unitsOfProduct(decodedNumber);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "product/{number}")
    public ProductDTO getProductForProductNumber(@PathVariable String number) throws UnsupportedEncodingException {
        String decodedNumber = new String(BaseEncoding.base64Url().decode(number), "utf-8");

        return documentPositionService.getProductForProductNumber(decodedNumber);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public void create(@RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionService.create(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(value = "{ids}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String ids) {
        documentPositionService.deletePositions(ids);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public void update(@RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionService.update(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storagelocations")
    public DataResponse getStorageLocations(@RequestParam("query") String query, @RequestParam("product") String product,
            @RequestParam("location") String location) {
        return documentPositionService.getStorageLocationsResponse(query, product, location);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "batch")
    public DataResponse getBatches(@RequestParam("query") String query, @RequestParam("product") String product) {
        return documentPositionService.getBatchesResponse(query, product);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation//{document}")
    public StorageLocationDTO getStorageLocationForEmptyProduct(@PathVariable String document) {
        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation/{product}/{document}")
    public StorageLocationDTO getStorageLocationForProductAndWarehouse(@PathVariable String product,
            @PathVariable String document) throws UnsupportedEncodingException {
        String decodedProduct = new String(BaseEncoding.base64Url().decode(product), "utf-8");
        String decodedDocument = new String(BaseEncoding.base64Url().decode(document), "utf-8");

        return documentPositionService.getStorageLocation(decodedProduct, decodedDocument);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resource")
    public ResourceDTO getResourceForProduct(@RequestParam("context") Long document, @RequestParam("product") String product,
            @RequestParam("conversion") BigDecimal conversion, @RequestParam("batchId") Long batchId) {
        if(Objects.nonNull(batchId) && batchId == 0) {
            batchId = null;
        }
        return documentPositionService.getResource(document, product, conversion, batchId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resources")
    public DataResponse getResources(@RequestParam("query") String query, @RequestParam("product") String product,
            @RequestParam("conversion") BigDecimal conversion, @RequestParam("context") Long document, @RequestParam("batchId") Long batchId) {
        if(Objects.nonNull(batchId) && batchId == 0) {
            batchId = null;
        }
        return documentPositionService.getResourcesResponse(document, query, product, conversion, batchId,true);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resourceByNumber/{document}/{resource}")
    public ResourceDTO getBatchForResource(@PathVariable Long document, @PathVariable String resource)
            throws UnsupportedEncodingException {
        String decodedResource = new String(BaseEncoding.base64Url().decode(resource), "utf-8");

        return documentPositionService.getResourceByNumber(decodedResource);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "productFromLocation")
    public ProductDTO getProductFromLocation(@RequestParam String location, @RequestParam Long document) {
        return documentPositionService.getProductFromLocation(location, document);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        return documentPositionService.getGridConfig(id);
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder, Locale locale, HttpServletRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    private Map<String, String> extractAttributesFilters(HttpServletRequest request) {
        Enumeration enumeration = request.getParameterNames();
        Map<String, String> attributeFilters = Maps.newHashMap();
        while(enumeration.hasMoreElements()){
            String parameterName = (String) enumeration.nextElement();
            if(parameterName.startsWith(L_ATTRS_PREFIX)){
                attributeFilters.put(parameterName.replace(L_ATTRS_PREFIX, ""), request.getParameter(parameterName));
            }
        }
        return attributeFilters;
    }

}
