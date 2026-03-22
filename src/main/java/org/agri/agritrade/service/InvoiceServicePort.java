package org.agri.agritrade.service;

import org.agri.agritrade.entity.Order;

public interface InvoiceServicePort {

    public byte[] generateInvoice(Long orderId);
}
