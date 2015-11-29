package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public final class BasicApiController {

    @Autowired
    private DataProvider dataProvider;

    @ResponseBody @RequestMapping(value = "/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDTO> getProductsByQuery(@RequestParam("query") String query) {
        return dataProvider.getProductsByQuery(query);
    }

    @ResponseBody @RequestMapping(value = "/additionalcodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdditionalCodeDTO> getAdditionalCodesByQuery(@RequestParam("query") String query) {
        return dataProvider.getAdditionalCodesByQuery(query);
    }

    @ResponseBody @RequestMapping(value = "/palletnumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PalletNumberDTO> getPalletNumbersByQuery(@RequestParam("query") String query) {
        return dataProvider.getPalletNumbersByQuery(query);
    }
}
