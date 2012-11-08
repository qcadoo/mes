package com.qcadoo.mes.deliveries.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class CompanyDetailsHooksD {

	@Autowired
	private CompanyService companyService;

	public void disabledGridWhenCompanyIsAnOwner(final ViewDefinitionState state) {

		companyService.disabledGridWhenCompanyIsAnOwner(state,
				"productsFamily", "products");

	}

}
