package org.agri.agritrade.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.entity.Order;
import org.agri.agritrade.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final OrderRepository orderRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String html = buildInvoiceHtml(order);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error generating invoice for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private String buildInvoiceHtml(Order order) {
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().format(FMT) : "N/A";
        String deliveryDate = order.getDeliveryDate() != null ? order.getDeliveryDate().format(FMT) : "Pending";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                <style>
                  body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
                  .header { text-align: center; border-bottom: 3px solid #16a34a; padding-bottom: 20px; margin-bottom: 30px; }
                  .header h1 { color: #16a34a; margin: 0; font-size: 28px; }
                  .header p { color: #666; margin: 5px 0 0; }
                  .invoice-title { font-size: 22px; font-weight: bold; text-align: right; color: #333; margin-bottom: 20px; }
                  .details-grid { display: flex; justify-content: space-between; margin-bottom: 30px; }
                  .detail-box { width: 45%%; }
                  .detail-box h3 { color: #16a34a; font-size: 14px; text-transform: uppercase; margin-bottom: 8px; border-bottom: 1px solid #e5e7eb; padding-bottom: 4px; }
                  .detail-box p { margin: 4px 0; font-size: 13px; }
                  table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                  th { background-color: #16a34a; color: white; padding: 10px 12px; text-align: left; font-size: 13px; }
                  td { padding: 10px 12px; border-bottom: 1px solid #e5e7eb; font-size: 13px; }
                  .total-row td { font-weight: bold; font-size: 15px; border-top: 2px solid #16a34a; }
                  .footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #999; font-size: 12px; }
                  .status { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: bold; }
                  .status-completed { background: #dcfce7; color: #16a34a; }
                  .status-pending { background: #fef9c3; color: #a16207; }
                </style>
                </head>
                <body>
                  <div class="header">
                    <h1>Krishi Setu</h1>
                    <p>Agricultural Trading Platform</p>
                  </div>
                  <div class="invoice-title">INVOICE #INV-%05d</div>
                  <div style="display: flex; justify-content: space-between;">
                    <div style="width: 45%%;">
                      <h3 style="color: #16a34a; font-size: 14px; text-transform: uppercase; margin-bottom: 8px; border-bottom: 1px solid #e5e7eb; padding-bottom: 4px;">Farmer Details</h3>
                      <p style="margin: 4px 0; font-size: 13px;"><strong>%s</strong></p>
                      <p style="margin: 4px 0; font-size: 13px;">%s</p>
                    </div>
                    <div style="width: 45%%;">
                      <h3 style="color: #16a34a; font-size: 14px; text-transform: uppercase; margin-bottom: 8px; border-bottom: 1px solid #e5e7eb; padding-bottom: 4px;">Retailer Details</h3>
                      <p style="margin: 4px 0; font-size: 13px;"><strong>%s</strong></p>
                      <p style="margin: 4px 0; font-size: 13px;">%s</p>
                    </div>
                  </div>
                  <table>
                    <tr>
                      <th>Item</th>
                      <th>Quantity</th>
                      <th>Unit Price</th>
                      <th>Total</th>
                    </tr>
                    <tr>
                      <td>%s</td>
                      <td>%s</td>
                      <td>₹%s</td>
                      <td>₹%s</td>
                    </tr>
                    <tr class="total-row">
                      <td colspan="3" style="text-align: right;">Grand Total</td>
                      <td>₹%s</td>
                    </tr>
                  </table>
                  <div style="margin-top: 20px;">
                    <p style="font-size: 13px;"><strong>Order Date:</strong> %s</p>
                    <p style="font-size: 13px;"><strong>Delivery Date:</strong> %s</p>
                    <p style="font-size: 13px;"><strong>Delivery Address:</strong> %s</p>
                    <p style="font-size: 13px;"><strong>Payment Status:</strong> <span class="status %s">%s</span></p>
                    <p style="font-size: 13px;"><strong>Order Status:</strong> <span class="status %s">%s</span></p>
                  </div>
                  <div class="footer">
                    <p>Thank you for trading with Krishi Setu!</p>
                    <p>This is a computer-generated invoice and does not require a signature.</p>
                  </div>
                </body>
                </html>
                """.formatted(
                order.getId(),
                order.getFarmer().getFullName(),
                order.getFarmer().getEmail(),
                order.getRetailer().getFullName(),
                order.getRetailer().getEmail(),
                order.getCropBatch().getCropName(),
                order.getQuantity(),
                order.getBid().getBidAmount(),
                order.getFinalAmount(),
                order.getFinalAmount(),
                orderDate,
                deliveryDate,
                order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A",
                order.getPaymentStatus().name().equals("COMPLETED") ? "status-completed" : "status-pending",
                order.getPaymentStatus().name(),
                order.getOrderStatus().name().equals("DELIVERED") ? "status-completed" : "status-pending",
                order.getOrderStatus().name()
        );
    }
}
