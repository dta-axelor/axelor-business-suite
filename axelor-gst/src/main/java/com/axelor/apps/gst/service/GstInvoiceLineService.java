package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

public class GstInvoiceLineService extends InvoiceLineSupplychainService {
	@Inject
	public GstInvoiceLineService(CurrencyService currencyService, PriceListService priceListService,
			AppAccountService appAccountService, AnalyticMoveLineService analyticMoveLineService,
			AccountManagementAccountService accountManagementAccountService,
			PurchaseProductService purchaseProductService) {
		super(currencyService, priceListService, appAccountService, analyticMoveLineService,
				accountManagementAccountService, purchaseProductService);
		// TODO Auto-generated constructor stub
	}

	@Inject
	TaxLineRepository TaxLineRepository;

	@Override
	public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {

		Map<String, Object> productInformation = new HashMap<>();
		productInformation = super.fillProductInformation(invoice, invoiceLine);

		invoiceLine.setPrice((BigDecimal) productInformation.get("price"));
		invoiceLine.setGstrate(invoiceLine.getProduct().getGstrate());

		BigDecimal netamount = BigDecimal.ZERO;
		BigDecimal gst = BigDecimal.ZERO;
		BigDecimal igst = BigDecimal.ZERO;
		BigDecimal sgst = BigDecimal.ZERO;
		BigDecimal cgst = BigDecimal.ZERO;
		BigDecimal gross = BigDecimal.ZERO;
		BigDecimal taxRate = BigDecimal.ZERO;
		BigDecimal bg1 = new BigDecimal("200");

		productInformation.remove("error");

		TaxLine taxLine = TaxLineRepository.all().filter("self.value = :value")
				.bind("value", invoiceLine.getProduct().getGstrate()).fetchOne();
		invoiceLine.setTaxLine(taxLine);
		productInformation.put("taxLine", taxLine);

		Address invoiceAddress = invoice.getAddress();
		Address companyAddress = invoice.getCompany().getAddress();

		if (companyAddress.getState() != null && invoiceAddress.getState() != null) {
			BigDecimal qty = invoiceLine.getQty();
			BigDecimal price = invoiceLine.getPrice();
			netamount = qty.multiply(price);
			invoiceLine.setNetamount(netamount);

			BigDecimal gstrate = invoiceLine.getProduct().getGstrate();
			taxRate = invoiceLine.getTaxLine().getValue();
			System.err.println("taxrate......" + taxRate);

			BigDecimal valueigst = netamount;
			BigDecimal gstvalue = taxRate;

			if (companyAddress.getState().equals(invoiceAddress.getState())) {

				gst = gst.add(gstvalue.multiply(valueigst));
				BigDecimal dividevalue = gst.divide(bg1);
				cgst = cgst.add(dividevalue);
				sgst = sgst.add(dividevalue);
				invoiceLine.setSgst(sgst);
				invoiceLine.setCgst(cgst);

				valueigst = valueigst.add(cgst);
				gross = sgst.add(valueigst);
			} else {
				gst = gst.add(gstvalue.multiply(valueigst).divide(new BigDecimal(100)));
				igst = igst.add(gst);
				invoiceLine.setIgst(igst);
				valueigst = valueigst.add(igst);
				gross = gross.add(valueigst);
			}
			productInformation.put("igst", igst);
			productInformation.put("gstrate", gstrate);
			productInformation.put("cgst", cgst);
			productInformation.put("sgst", sgst);
			productInformation.put("grossamount", gross);
		}
		return productInformation;
	}


}
