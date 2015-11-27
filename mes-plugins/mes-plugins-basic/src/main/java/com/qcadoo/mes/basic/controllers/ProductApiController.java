package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.controllers.dataProvider.ProductsDataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public final class ProductApiController {

    @Autowired
    private ProductsDataProvider dataProvider;

    @ResponseBody @RequestMapping(value = "/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDTO> getProducts() {
        return dataProvider.getProducts();
    }
}
