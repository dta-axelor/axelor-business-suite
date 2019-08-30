package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.gst.service.GstInvoiceLineService;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(InvoiceLineSupplychainService.class).to(GstInvoiceLineService.class);
    bind(InvoiceServiceProjectImpl.class).to(GstInvoiceServiceImpl.class);
  }
}
