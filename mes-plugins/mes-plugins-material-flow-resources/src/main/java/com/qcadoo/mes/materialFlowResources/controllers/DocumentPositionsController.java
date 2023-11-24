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
import java.nio.charset.StandardCharsets;
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
    public GridResponse<DocumentPositionDTO> findAll(@PathVariable final Long id, @RequestParam final String sidx, @RequestParam final String sord,
                                                     @RequestParam(defaultValue = "1", required = false, value = "page") final Integer page,
                                                     @RequestParam(value = "rows") final int perPage,
                                                     final DocumentPositionDTO positionDTO, final HttpServletRequest request) {
        Map<String, String> attributeFilters = extractAttributesFilters(request);

        return documentPositionService.findAll(id, sidx, sord, page, perPage, positionDTO, attributeFilters);
    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "units/{number}")
    public Map<String, Object> getUnitsForProduct(@PathVariable final String number) throws UnsupportedEncodingException {
        String decodedNumber = new String(BaseEncoding.base64Url().decode(number), StandardCharsets.UTF_8);

        return documentPositionService.unitsOfProduct(decodedNumber);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "product/{number}")
    public ProductDTO getProductForProductNumber(@PathVariable final String number) throws UnsupportedEncodingException {
        String decodedNumber = new String(BaseEncoding.base64Url().decode(number), StandardCharsets.UTF_8);

        return documentPositionService.getProductForProductNumber(decodedNumber);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public void create(@RequestBody final DocumentPositionDTO documentPositionVO) {
        documentPositionService.create(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(value = "{ids}", method = RequestMethod.DELETE)
    public void delete(@PathVariable final String ids) {
        documentPositionService.deletePositions(ids);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public void update(@RequestBody final DocumentPositionDTO documentPositionVO) {
        documentPositionService.update(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storagelocations")
    public DataResponse getStorageLocations(@RequestParam("query") final String query, @RequestParam("product") final String product,
                                            @RequestParam("location") final String location) {
        return documentPositionService.getStorageLocationsResponse(query, product, location);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "batch")
    public DataResponse getBatches(@RequestParam("query") final String query, @RequestParam("product") final String product) {
        return documentPositionService.getBatchesResponse(query, product);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation//{document}")
    public StorageLocationDTO getStorageLocationForEmptyProduct(@PathVariable final String document) {
        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storageLocation/{product}/{document}")
    public StorageLocationDTO getStorageLocationForProductAndWarehouse(@PathVariable final String product,
                                                                       @PathVariable final String document) throws UnsupportedEncodingException {
        String decodedProduct = new String(BaseEncoding.base64Url().decode(product), StandardCharsets.UTF_8);
        String decodedDocument = new String(BaseEncoding.base64Url().decode(document), StandardCharsets.UTF_8);

        return documentPositionService.getStorageLocation(decodedProduct, decodedDocument);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resource")
    public ResourceDTO getResourceForProduct(@RequestParam("context") final Long document, @RequestParam("product") final String product,
                                             @RequestParam("conversion") final BigDecimal conversion, @RequestParam("batchId") Long batchId) {
        if (Objects.nonNull(batchId) && batchId == 0) {
            batchId = null;
        }

        return documentPositionService.getResource(document, product, conversion, batchId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resources")
    public DataResponse getResources(@RequestParam("query") final String query, @RequestParam("product") final String product,
                                     @RequestParam("conversion") final BigDecimal conversion,
                                     @RequestParam("context") final Long document, @RequestParam("batchId") Long batchId) {
        if (Objects.nonNull(batchId) && batchId == 0) {
            batchId = null;
        }

        return documentPositionService.getResourcesResponse(document, query, product, conversion, batchId, true);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "resourceByNumber/{document}/{resource}")
    public ResourceDTO getBatchForResource(@PathVariable final Long document, @PathVariable final String resource)
            throws UnsupportedEncodingException {
        String decodedResource = new String(BaseEncoding.base64Url().decode(resource), StandardCharsets.UTF_8);

        return documentPositionService.getResourceByNumber(decodedResource);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "typeOfPalletByPallet/{document}/{pallet}")
    public String getTypeOfPalletForPallet(@PathVariable final Long document, @PathVariable final String pallet)
            throws UnsupportedEncodingException {
        String decodedPallet = new String(BaseEncoding.base64Url().decode(pallet), StandardCharsets.UTF_8);

        return documentPositionService.getTypeOfPalletByPalletNumber(document, decodedPallet);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "productFromLocation")
    public ProductDTO getProductFromLocation(@RequestParam final String location, @RequestParam final Long document) {
        return documentPositionService.getProductFromLocation(location, document);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable final Long id) {
        return documentPositionService.getGridConfig(id);
    }

    @InitBinder
    public void initBinder(final WebDataBinder dataBinder, final Locale locale, final HttpServletRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));

        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    private Map<String, String> extractAttributesFilters(final HttpServletRequest request) {
        Enumeration enumeration = request.getParameterNames();

        Map<String, String> attributeFilters = Maps.newHashMap();

        while (enumeration.hasMoreElements()) {
            String parameterName = (String) enumeration.nextElement();

            if (parameterName.startsWith(L_ATTRS_PREFIX)) {
                attributeFilters.put(parameterName.replace(L_ATTRS_PREFIX, ""), request.getParameter(parameterName));
            }
        }

        return attributeFilters;
    }

}
