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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "customer/orders", layout = MainLayout.class)
@PageTitle("My Orders")
@RolesAllowed("CUSTOMER")
public class CustomerOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final PaymentService paymentService;

    public CustomerOrdersView(OrderService orderService, ReviewService reviewService,
                               UserService userService, PaymentService paymentService) {
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.paymentService = paymentService;

        addClassName("customer-orders-view");
        setPadding(true);

        H2 title = new H2("My Orders");
        add(title);

        User customer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();
        List<Order> orders = orderService.findByCustomer(customer);

        if (orders.isEmpty()) {
            add(new Paragraph("No orders yet. Start shopping!"));
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        for (Order order : orders) {
            Div card = new Div();
            card.addClassName("order-detail-card");

            // Header
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            Span orderId = new Span("Order #" + order.getId());
            orderId.addClassName("order-id");
            Span date = new Span(order.getOrderDate().format(fmt));
            Span statusBadge = new Span(order.getStatus().name());
            statusBadge.addClassName("status-badge");
            statusBadge.addClassName("status-" + order.getStatus().name().toLowerCase());
            header.add(orderId, date, statusBadge);

            // Items
            VerticalLayout itemsList = new VerticalLayout();
            itemsList.setPadding(false);
            itemsList.setSpacing(false);
            for (OrderItem item : order.getItems()) {
                itemsList.add(new Span(
                        item.getProduct().getName()
                                + " × " + String.format("%.0f", item.getQuantity())
                                + " = ₹" + String.format("%.0f", item.getSubtotal())
                ));
            }

            Paragraph totalP = new Paragraph("Total: ₹" + String.format("%.0f", order.getGrandTotal())
                    + " (" + order.getDeliveryType() + ")");
            totalP.addClassName("order-total");

            // Farmer info
            Paragraph farmerInfo = new Paragraph("From: " + order.getFarmer().getFullName()
                    + " — " + order.getFarmer().getCity());

            // Tracking button
            Button trackBtn = new Button("Track Order", e -> openTrackingDialog(order));
            trackBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            // Review button (only if delivered)
            HorizontalLayout actions = new HorizontalLayout(trackBtn);
            if (order.getStatus() == OrderStatus.DELIVERED) {
                Button reviewBtn = new Button("Write Review", e -> openReviewDialog(order, customer));
                reviewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                actions.add(reviewBtn);
            }

            card.add(header, itemsList, totalP, farmerInfo, actions);
            add(card);
        }
    }

    private void openTrackingDialog(Order order) {
        Dialog dialog = new Dialog("Order Tracking — #" + order.getId());
        dialog.setWidth("500px");

        // Amazon-style timeline
        VerticalLayout timeline = new VerticalLayout();
        timeline.addClassName("tracking-timeline");

        OrderStatus[] allStatuses = {
                OrderStatus.PLACED, OrderStatus.PAID, OrderStatus.CONFIRMED,
                OrderStatus.PACKED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED
        };

        List<OrderTrackingEvent> events = orderService.getTrackingEvents(order);

        for (OrderStatus status : allStatuses) {
            Div step = new Div();
            step.addClassName("timeline-step");

            boolean reached = events.stream().anyMatch(e -> e.getStatus() == status);
            boolean isCurrent = order.getStatus() == status;

            if (reached) {
                step.addClassName("timeline-done");
            }
            if (isCurrent) {
                step.addClassName("timeline-current");
            }

            Span icon = new Span(reached ? "●" : "○");
            icon.addClassName("timeline-icon");
            Span label = new Span(formatStatus(status));
            label.addClassName("timeline-label");

            step.add(icon, label);

            // Show event details
            events.stream()
                    .filter(e -> e.getStatus() == status)
                    .findFirst()
                    .ifPresent(e -> {
                        Span desc = new Span(e.getDescription());
                        desc.addClassName("timeline-desc");
                        Span time = new Span(e.getTimestamp().format(
                                DateTimeFormatter.ofPattern("dd MMM, hh:mm a")));
                        time.addClassName("timeline-time");
                        step.add(desc, time);
                    });

            timeline.add(step);
        }

        // Delivery agent info
        if (order.getDeliveryAgent() != null) {
            DeliveryAgent agent = order.getDeliveryAgent();
            Paragraph agentInfo = new Paragraph(
                    "Agent: " + agent.getName()
                            + " | " + agent.getPhone()
                            + " | " + agent.getVehicleType() + " " + agent.getVehicleNumber());
            timeline.add(agentInfo);
        }

        dialog.add(timeline);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void openReviewDialog(Order order, User customer) {
        Dialog dialog = new Dialog("Review Order #" + order.getId());
        dialog.setWidth("400px");

        ComboBox<Integer> rating = new ComboBox<>("Rating");
        rating.setItems(1, 2, 3, 4, 5);
        rating.setValue(5);
        rating.setItemLabelGenerator(r -> r + " / 5");

        TextArea comment = new TextArea("Your Review");
        comment.setWidthFull();

        Button submit = new Button("Submit Review", e -> {
            for (OrderItem item : order.getItems()) {
                reviewService.addReview(customer, item.getProduct(), order,
                        rating.getValue(), comment.getValue());
            }
            Notification.show("Review submitted! Thank you.", 3000,
                    Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(rating, comment);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), submit);
        dialog.open();
    }

    private String formatStatus(OrderStatus status) {
        return switch (status) {
            case PLACED -> "Order Placed";
            case PAID -> "Payment Confirmed";
            case CONFIRMED -> "Farmer Confirmed";
            case PACKED -> "Packed & Ready";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case READY_FOR_PICKUP -> "Ready for Pickup";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }
}
