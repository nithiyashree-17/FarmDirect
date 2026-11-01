package com.farmdirect.views.farmer;

import com.farmdirect.entity.*;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.DeliveryAgentService;
import com.farmdirect.service.OrderService;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "farmer/orders", layout = MainLayout.class)
@PageTitle("Farmer Orders")
@RolesAllowed("FARMER")
public class FarmerOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final UserService userService;
    private final DeliveryAgentService agentService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);

    public FarmerOrdersView(OrderService orderService, UserService userService,
                            DeliveryAgentService agentService) {
        this.orderService = orderService;
        this.userService = userService;
        this.agentService = agentService;

        addClassName("orders-view");
        setPadding(true);

        H2 title = new H2("Orders Received");
        add(title);

        setupGrid();
        add(grid);
        refreshGrid();
    }

    private void setupGrid() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        grid.addColumn(o -> "#" + o.getId()).setHeader("Order ID").setWidth("80px");
        grid.addColumn(o -> o.getCustomer().getFullName()).setHeader("Customer");
        grid.addColumn(o -> o.getOrderDate().format(fmt)).setHeader("Date");
        grid.addColumn(o -> o.getItems().size() + " items").setHeader("Items");
        grid.addColumn(o -> "₹" + String.format("%.0f", o.getGrandTotal())).setHeader("Total");
        grid.addComponentColumn(o -> {
            Span badge = new Span(o.getStatus().name());
            badge.addClassName("status-badge");
            badge.addClassName("status-" + o.getStatus().name().toLowerCase());
            return badge;
        }).setHeader("Status");
        grid.addComponentColumn(o -> {
            Button updateBtn = new Button("Update", e -> openUpdateDialog(o));
            updateBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            Button viewBtn = new Button("Track", e -> openTrackingDialog(o));
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            return new HorizontalLayout(updateBtn, viewBtn);
        }).setHeader("Actions");

        grid.setAllRowsVisible(true);
    }

    private void openUpdateDialog(Order order) {
        Dialog dialog = new Dialog("Update Order #" + order.getId());
        dialog.setWidth("400px");

        ComboBox<OrderStatus> statusSelect = new ComboBox<>("New Status");
        // Show only valid next statuses
        OrderStatus current = order.getStatus();
        List<OrderStatus> nextStatuses = switch (current) {
            case PLACED -> List.of(OrderStatus.CANCELLED);
            case PAID -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> List.of(OrderStatus.PACKED);
            case PACKED -> List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.READY_FOR_PICKUP);
            case OUT_FOR_DELIVERY -> List.of(OrderStatus.DELIVERED);
            case READY_FOR_PICKUP -> List.of(OrderStatus.DELIVERED);
            default -> List.of();
        };
        statusSelect.setItems(nextStatuses);

        Button save = new Button("Update", e -> {
            if (statusSelect.getValue() != null) {
                OrderStatus newStatus = statusSelect.getValue();
                String desc = "Status updated to " + newStatus.name() + " by farmer.";

                if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
                    DeliveryAgent agent = agentService.assignAvailableAgent();
                    if (agent != null) {
                        order.setDeliveryAgent(agent);
                        desc += " Delivery agent: " + agent.getName() + " (" + agent.getVehicleNumber() + ")";
                    }
                }

                orderService.updateStatus(order.getId(), newStatus, desc);
                dialog.close();
                refreshGrid();
                Notification.show("Order updated!", 2000, Notification.Position.TOP_CENTER);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new Paragraph("Current: " + current.name()), statusSelect);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), save);
        dialog.open();
    }

    private void openTrackingDialog(Order order) {
        Dialog dialog = new Dialog("Tracking — Order #" + order.getId());
        dialog.setWidth("500px");

        VerticalLayout timeline = new VerticalLayout();
        timeline.addClassName("tracking-timeline");

        List<OrderTrackingEvent> events = orderService.getTrackingEvents(order);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

        for (int i = 0; i < events.size(); i++) {
            OrderTrackingEvent event = events.get(i);
            Div step = new Div();
            step.addClassName("timeline-step");
            step.addClassName(i == events.size() - 1 ? "timeline-current" : "timeline-done");

            Span statusSpan = new Span(event.getStatus().name());
            statusSpan.addClassName("timeline-status");
            Span descSpan = new Span(event.getDescription());
            descSpan.addClassName("timeline-desc");
            Span timeSpan = new Span(event.getTimestamp().format(fmt));
            timeSpan.addClassName("timeline-time");

            step.add(statusSpan, descSpan, timeSpan);
            timeline.add(step);
        }

        dialog.add(timeline);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void refreshGrid() {
        User farmer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();
        grid.setItems(orderService.findByFarmer(farmer));
    }
}
