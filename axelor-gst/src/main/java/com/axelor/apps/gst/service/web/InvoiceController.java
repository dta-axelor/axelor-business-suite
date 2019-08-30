package com.axelor.apps.gst.service.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.gst.service.GstInvoiceLineService;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceController {
	
	
	@Inject
	InvoiceGstService GstService;
	GstInvoiceServiceImpl gstinvoiceService;

	public void changeInvoicelineCalculate(ActionRequest request, ActionResponse response) throws AxelorException {
		try
		{
		Invoice invoice = request.getContext().asType(Invoice.class);
		List<InvoiceLine> invoicelinelist = invoice.getInvoiceLineList();
		if(invoice.getInvoiceLineList()!=null)
		{
		List<InvoiceLine> addinvoicelinelist = new ArrayList<InvoiceLine>();

		for (InvoiceLine invoiceLine : invoicelinelist) {

			InvoiceLine invoicelinenew = GstService.changeInvoicelineCalculate(invoice,invoiceLine);
			response.setValue("taxLine",invoicelinenew.getTaxLine());
			response.setValue("exTaxTotal", invoicelinenew.getQty().multiply(invoicelinenew.getPrice()));
			response.setValue("cgst", invoicelinenew.getCgst());
			response.setValue("sgst", invoicelinenew.getSgst());
			response.setValue("grossamount", invoicelinenew.getGrossamount());
			response.setValue("igst", invoicelinenew.getIgst());
			addinvoicelinelist.add(invoicelinenew);
		}
		response.setValue("invoiceLineList", addinvoicelinelist);
		}

		invoice = gstinvoiceService.compute(invoice);
		response.setValue("netamount", invoice.getNetamount());
		response.setValue("netigst", invoice.getNetigst());
		response.setValue("netcgst", invoice.getNetcgst());
		response.setValue("netsgst", invoice.getNetsgst());
		response.setValue("grossamount", invoice.getGrossamount());
	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	
	}
	
	
	