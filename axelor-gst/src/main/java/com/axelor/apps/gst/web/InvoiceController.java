package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.gst.service.GstInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.inject.Inject;

public class InvoiceController {

  @Inject GstInvoiceServiceImpl gstinvoiceService;

  public void changeInvoicelineCalculate(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      invoice = gstinvoiceService.calculate(invoice);
      response.setValues(invoice);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void invoiceReportPrint(ActionRequest request, ActionResponse response) {}
}
