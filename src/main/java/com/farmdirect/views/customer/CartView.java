package com.farmdirect.views.customer;

import com.farmdirect.entity.*;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.*;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = "customer/cart", layout = MainLayout.class)
@PageTitle("My Cart")
@RolesAllowed("CUSTOMER")
public class CartView extends VerticalLayout {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;

    public CartView(CartService cartService, OrderService orderService,
                    PaymentService paymentService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.userService = userService;

        addClassName("cart-view");
        setPadding(true);
        buildCartView();
    }

    private void buildCartView() {
        removeAll();

        H2 title = new H2("My Cart");
        add(title);

        if (cartService.isEmpty()) {
            add(new Paragraph("Your cart is empty. Start browsing!"));
            Button browse = new Button("Browse Products", e ->
                    getUI().ifPresent(ui -> ui.navigate("customer/browse")));
            browse.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add(browse);
            return;
        }

        Map<Long, Map<Product, Double>> cart = cartService.getCart();

        for (Map.Entry<Long, Map<Product, Double>> farmerEntry : cart.entrySet()) {
            Map<Product, Double> items = farmerEntry.getValue();
            if (items.isEmpty()) continue;

            Product firstProduct = items.keySet().iterator().next();
            User farmer = firstProduct.getFarmer();

            Div farmerSection = new Div();
            farmerSection.addClassName("cart-farmer-section");

            H3 farmerName = new H3(farmer.getFullName() + " — " + farmer.getCity());
            farmerSection.add(farmerName);

            double farmerTotal = 0;
            for (Map.Entry<Product, Double> itemEntry : items.entrySet()) {
                Product p = itemEntry.getKey();
                double qty = itemEntry.getValue();
                double subtotal = p.getPricePerUnit() * qty;
                farmerTotal += subtotal;

                Div itemRow = new Div();
                itemRow.addClassName("cart-item");

                Span nameSpan = new Span(p.getName() + " — ₹" + String.format("%.0f", p.getPricePerUnit()) + "/" + p.getUnit());
                Span qtySpan = new Span(" × " + String.format("%.0f", qty));
                Span subtotalSpan = new Span(" = ₹" + String.format("%.0f", subtotal));
                subtotalSpan.addClassName("cart-subtotal");

                Button removeBtn = new Button("Remove", e -> {
                    cartService.removeFromCart(p);
                    buildCartView();
                });
                removeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

                itemRow.add(nameSpan, qtySpan, subtotalSpan, removeBtn);
                farmerSection.add(itemRow);
            }

            Paragraph farmerTotalP = new Paragraph("Subtotal: ₹" + String.format("%.0f", farmerTotal));
            farmerTotalP.addClassName("farmer-total");

            double finalFarmerTotal = farmerTotal;
            Button checkoutBtn = new Button("Checkout from " + farmer.getFullName(), e -> {
                openCheckoutDialog(farmer, items, finalFarmerTotal);
            });
            checkoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            farmerSection.add(farmerTotalP, checkoutBtn);
            add(farmerSection);
        }

        // Grand total
        H3 grandTotal = new H3("Grand Total: ₹" + String.format("%.0f", cartService.getGrandTotal()));
        grandTotal.addClassName("grand-total");
        add(grandTotal);
    }

    private void openCheckoutDialog(User farmer, Map<Product, Double> items, double total) {
        User customer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();

        Dialog dialog = new Dialog("Checkout");
        dialog.setWidth("500px");

        ComboBox<String> deliveryType = new ComboBox<>("Delivery Type");
        deliveryType.setItems("DELIVERY", "PICKUP");
        deliveryType.setValue("DELIVERY");

        TextField address = new TextField("Delivery Address");
        address.setWidthFull();
        address.setValue(customer.getAddress() != null ? customer.getAddress() :
                customer.getCity() + ", " + customer.getDistrict());

        TextArea notes = new TextArea("Notes (optional)");
        notes.setWidthFull();

        double deliveryCharge = 40.0;
        Paragraph summary = new Paragraph(
                "Items: ₹" + String.format("%.0f", total)
                + " | Delivery: ₹" + String.format("%.0f", deliveryCharge)
                + " | Total: ₹" + String.format("%.0f", total + deliveryCharge));

        deliveryType.addValueChangeListener(e -> {
            double charge = "DELIVERY".equals(e.getValue()) ? 40.0 : 0.0;
            address.setVisible("DELIVERY".equals(e.getValue()));
            summary.setText("Items: ₹" + String.format("%.0f", total)
                    + " | Delivery: ₹" + String.format("%.0f", charge)
                    + " | Total: ₹" + String.format("%.0f", total + charge));
        });

        ComboBox<String> paymentMethod = new ComboBox<>("Payment Method");
        paymentMethod.setItems("UPI", "CARD", "COD", "NETBANKING");
        paymentMethod.setValue("UPI");

        Button payBtn = new Button("Pay ₹" + String.format("%.0f", total + deliveryCharge), e -> {
            try {
                Order order = orderService.createOrder(
                        customer, farmer, items,
                        deliveryType.getValue(),
                        address.getValue(),
                        notes.getValue()
                );

                if (!"COD".equals(paymentMethod.getValue())) {
                    Payment payment = paymentService.processPayment(order, paymentMethod.getValue());
                    showPaymentSuccess(payment, order);
                } else {
                    Notification.show("Order placed! Pay on delivery.", 3000,
                            Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                cartService.clearFarmerCart(farmer.getId());
                dialog.close();
                buildCartView();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 4000,
                        Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        payBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(deliveryType, address, notes, summary, paymentMethod);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), payBtn);
        dialog.open();
    }

    private void showPaymentSuccess(Payment payment, Order order) {
        Dialog successDialog = new Dialog();
        successDialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(Alignment.CENTER);

        H3 successTitle = new H3("Payment Successful!");
        Paragraph txnId = new Paragraph("Transaction ID: " + payment.getTransactionId());
        Paragraph amount = new Paragraph("Amount: ₹" + String.format("%.2f", payment.getAmount()));
        Paragraph method = new Paragraph("Method: " + payment.getPaymentMethod());

        Button viewOrder = new Button("View Order", e -> {
            successDialog.close();
            getUI().ifPresent(ui -> ui.navigate("customer/orders"));
        });
        viewOrder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        content.add(successTitle, txnId, amount, method, viewOrder);
        successDialog.add(content);
        successDialog.open();
    }
}
