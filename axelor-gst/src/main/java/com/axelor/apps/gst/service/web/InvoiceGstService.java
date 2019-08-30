package com.axelor.apps.gst.service.web;

import java.math.BigDecimal;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.base.db.Address;
import com.axelor.exception.AxelorException;

public class InvoiceGstService {
	
	@Inject
	TaxLineRepository TaxLineRepository;
	
	public InvoiceLine changeInvoicelineCalculate(Invoice invoice,InvoiceLine invoiceLine) throws AxelorException
	{
		BigDecimal netamount = BigDecimal.ZERO;
		BigDecimal gst = BigDecimal.ZERO;
		BigDecimal igst = BigDecimal.ZERO;
		BigDecimal sgst = BigDecimal.ZERO;
		BigDecimal cgst = BigDecimal.ZERO;
		BigDecimal gross = BigDecimal.ZERO;
		BigDecimal taxRate = BigDecimal.ZERO;
		BigDecimal bg1 = new BigDecimal("200");
		
		invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
		
		//invoiceLine.setPrice((BigDecimal) productInformation.get("price"));
		invoiceLine.setGstrate(invoiceLine.getProduct().getGstrate());
		
		TaxLine taxLine = TaxLineRepository.all().filter("self.value = :value").bind("value", invoiceLine.getProduct().getGstrate())
				.fetchOne();
		invoiceLine.setTaxLine(taxLine);
		
		
		
		
		Address invoiceAddress = invoice.getAddress();
		Address companyAddress = invoice.getCompany().getAddress();

		if (companyAddress.getState() != null && invoiceAddress.getState() != null) {
			BigDecimal qty = invoiceLine.getQty();
			BigDecimal price = invoiceLine.getPrice();
			netamount = qty.multiply(price);
			invoiceLine.setNetamount(netamount);

			BigDecimal gstrate = invoiceLine.getProduct().getGstrate();
			taxRate = invoiceLine.getTaxLine().getValue();
			System.err.println("taxrate......"+taxRate);
			
			BigDecimal valueigst = netamount;
			BigDecimal gstvalue = taxRate;

			if (companyAddress.getState().equals(invoiceAddress.getState())) {

				gst = gst.add(gstvalue.multiply(valueigst));
				BigDecimal dividevalue = gst.divide(bg1);
				cgst = cgst.add(dividevalue);
				sgst = sgst.add(dividevalue);
				valueigst = valueigst.add(cgst);
				gross = sgst.add(valueigst);
				invoiceLine.setCgst(cgst);
				invoiceLine.setSgst(sgst);
				invoiceLine.setGrossamount(gross);
				invoiceLine.setIgst(igst);
				
			} else {
				gst = gst.add(gstvalue.multiply(valueigst).divide(new BigDecimal(100)));
				igst = igst.add(gst);
				valueigst = valueigst.add(igst);
				
				gross = gross.add(valueigst);
				invoiceLine.setIgst(igst);
				System.err.println("state is diff..."+igst);
				invoiceLine.setCgst(cgst);
				invoiceLine.setSgst(sgst);
				invoiceLine.setGrossamount(gross);	
			}
			
	}
		return invoiceLine;

	}
}
