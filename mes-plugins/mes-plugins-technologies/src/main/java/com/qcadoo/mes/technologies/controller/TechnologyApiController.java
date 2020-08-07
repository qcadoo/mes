package com.qcadoo.mes.technologies.controller;

import com.qcadoo.mes.technologies.controller.dataProvider.DataProviderForTechnology;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;
import com.qcadoo.mes.technologies.controller.dataProvider.TechnologiesGridResponse;
import com.qcadoo.mes.technologies.controller.dataProvider.TechnologiesResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class TechnologyApiController {

    @Autowired
    private DataProviderForTechnology dataProvider;

    @ResponseBody
    @RequestMapping(value = "/technologies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnologiesResponse getTechnologies(@RequestParam("query") String query, @RequestParam("productId") Long productId) {
        return dataProvider.getTechnologies(query, productId);
    }

    @ResponseBody
    @RequestMapping(value = "/technology/{technologyId}/materials", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MaterialDto> getTechnologyMaterials(@PathVariable Long technologyId) {
        return dataProvider.getTechnologyMaterials(technologyId);
    }

    @ResponseBody
    @RequestMapping(value = "/technologiesByPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnologiesGridResponse getProducts(@RequestParam(value = "limit") int limit, @RequestParam(value = "offset") int offset,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "productId") Long productId) {
        return dataProvider.getTechnologiesResponse(limit, offset, sort, order, search, productId);
    }

}
