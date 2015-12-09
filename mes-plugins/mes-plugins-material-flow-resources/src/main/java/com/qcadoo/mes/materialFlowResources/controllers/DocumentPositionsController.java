package com.qcadoo.mes.materialFlowResources.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.StorageLocationDTO;

@Controller
@RequestMapping("/integration/rest/documentPositions")
public class DocumentPositionsController {

    @Autowired
    private DocumentPositionService documentPositionRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public List<DocumentPositionDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord) {
        return documentPositionRepository.findAll(id, sidx, sord);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "unit/{number}")
    public String getUnitForProduct(@PathVariable String number) {
        return documentPositionRepository.unitOfProduct(number);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public void create(@RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionRepository.create(documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        documentPositionRepository.delete(id);
    }

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Long id, @RequestBody DocumentPositionDTO documentPositionVO) {
        documentPositionRepository.update(id, documentPositionVO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "storagelocations")
    public List<StorageLocationDTO> getStorageLocations(@RequestParam("query") String query) {
        return documentPositionRepository.getStorageLocations(query);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig")
    public Map<String, String> gridConfig(){
        return documentPositionRepository.getGridConfig();
    }
}
