package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class GstInvoiceServiceImpl extends InvoiceServiceProjectImpl {

  @Inject
  public GstInvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService);
    // TODO Auto-generated constructor stub
  }

  @Inject GstInvoiceLineService gst;

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

  public Invoice calculate(Invoice invoice) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      Product product = invoiceLine.getProduct();
      invoiceLine =
          Mapper.toBean(
              InvoiceLine.class, invoiceLineService.fillProductInformation(invoice, invoiceLine));

      invoiceLine.setProduct(product);

      BigDecimal exTaxTotal;
      BigDecimal companyExTaxTotal;
      BigDecimal inTaxTotal;
      BigDecimal companyInTaxTotal;
      BigDecimal priceDiscounted =
          invoiceLineService.computeDiscount(invoiceLine, invoice.getInAti());

      invoiceLine.setQty(new BigDecimal(1));

      BigDecimal taxRate = BigDecimal.ZERO;
      if (invoiceLine.getTaxLine() != null) {
        taxRate = invoiceLine.getTaxLine().getValue();
      }

      if (!invoice.getInAti()) {
        exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
        inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
      } else {
        inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
        exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
      }

      companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
      companyInTaxTotal = invoiceLineService.getCompanyExTaxTotal(inTaxTotal, invoice);

      invoiceLine.setExTaxTotal(exTaxTotal);
      invoiceLine.setInTaxTotal(inTaxTotal);
      invoiceLine.setCompanyExTaxTotal(companyExTaxTotal);
      invoiceLine.setCompanyInTaxTotal(companyInTaxTotal);
      invoiceLineList.add(invoiceLine);
    }
    invoice.setInvoiceLineList(invoiceLineList);
    List<InvoiceLineTax> invoiceTaxLines =
        (new TaxInvoiceLine(invoice, invoice.getInvoiceLineList())).creates();
    invoice.setInvoiceLineTaxList(invoiceTaxLines);
    Invoice invoiceNew = compute(invoice);

    return invoiceNew;
  }
}
