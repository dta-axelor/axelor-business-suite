package com.axelor.apps.gst.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductService {
	
	@Inject
	ProductRepository  productRepo;
	
	TaxLineRepository TaxLineRepository;
	

	public void createNewInvoice(ActionRequest request, ActionResponse response) {
	
		
		List<InvoiceLine> invoiceLineList = (List<InvoiceLine>) request.getContext().get("_invoiceproductlist");
		response.setValue("invoiceLineList", invoiceLineList);
	}


	public void createInvoiceFromProduct(ActionRequest request, ActionResponse response) {
		@SuppressWarnings("unchecked")
		List<Integer> getids = (List<Integer>) request.getContext().get("_ids");
		List<Product> productList = productRepo.all().filter("self.id in (?1)", getids).fetch();
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for (Product product : productList) {
			InvoiceLine invoiceLine = new InvoiceLine();
			invoiceLine.setProduct(product);
			invoiceLine.setProductName(product.getFullName());
			invoiceLine.setQty(new BigDecimal(1));
			invoiceLine.setPrice(product.getSalePrice());
			invoiceLine.setExTaxTotal(invoiceLine.getQty().multiply(invoiceLine.getPrice()));
			Tax tax=Beans.get(TaxRepository.class).all().filter("self.code = 'GST'").fetchOne();
			TaxLine taxLine=tax.getActiveTaxLine();
			taxLine.setValue(invoiceLine.getProduct().getGstrate());
			invoiceLine.setTaxLine(taxLine);

			invoiceLineList.add(invoiceLine);
		}
	
		response.setView(ActionView.define("selected line from product").model(Invoice.class.getName())
				.add("form", "invoice-form").context("_operationTypeSelect",3).context("_invoiceproductlist", invoiceLineList).map());
	}
}
