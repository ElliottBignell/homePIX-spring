package org.springframework.samples.homepix.portfolio.sales;

import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.paypal.core.PayPalHttpClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/paypal")
public class PayPalPaymentController {

    private final PayPalHttpClient client;

    public PayPalPaymentController(PayPalHttpClient client) {
        this.client = client;
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder() throws Exception {

        OrderRequest order = new OrderRequest();
        order.checkoutPaymentIntent("CAPTURE");

        order.purchaseUnits(List.of(
                new PurchaseUnitRequest()
                    .amountWithBreakdown(
                        new AmountWithBreakdown().currencyCode("USD").value("19.99"))
        ));

        OrdersCreateRequest request = new OrdersCreateRequest()
                .requestBody(order);

        HttpResponse<Order> response = client.execute(request);

        return Map.of("id", response.result().id());
    }

    @PostMapping("/capture/{orderId}")
    public String captureOrder(@PathVariable String orderId) throws Exception {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        client.execute(request);
        return "OK";
    }
}
