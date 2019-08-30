package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class GstInvoiceServiceImpl extends InvoiceServiceProjectImpl {

	@Inject
	public GstInvoiceServiceImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory,
			CancelFactory cancelFactory, AlarmEngineService<Invoice> alarmEngineService, InvoiceRepository invoiceRepo,
			AppAccountService appAccountService, PartnerService partnerService, InvoiceLineService invoiceLineService,
			AccountConfigService accountConfigService) {
		super(validateFactory, ventilateFactory, cancelFactory, alarmEngineService, invoiceRepo, appAccountService,
				partnerService, invoiceLineService, accountConfigService);
		// TODO Auto-generated constructor stub
	}

	@Inject
	GstInvoiceLineService gst;

	@Override
	public Invoice compute(Invoice invoice) throws AxelorException {
		super.compute(invoice);
		{
			BigDecimal netamount = BigDecimal.ZERO;
			BigDecimal invoiceigst = BigDecimal.ZERO;
			BigDecimal invoicecgst = BigDecimal.ZERO;
			BigDecimal invoicesgst = BigDecimal.ZERO;
			BigDecimal invoicegross = BigDecimal.ZERO;

			for (InvoiceLine line : invoice.getInvoiceLineList()) {
				netamount = netamount.add(line.getNetamount());
				invoiceigst = invoiceigst.add(line.getIgst());
				invoicecgst = invoicecgst.add(line.getCgst());
				invoicesgst = invoicesgst.add(line.getSgst());
				invoicegross = invoicegross.add(line.getGrossamount());
			}
			invoice.setNetamount(netamount);
			invoice.setNetigst(invoiceigst);
			invoice.setNetcgst(invoicecgst);
			invoice.setNetsgst(invoicesgst);
			invoice.setGrossamount(invoicegross);
		}
		return invoice;
	}

}
