package com.farmdirect.views.admin;

import com.farmdirect.entity.Order;
import com.farmdirect.service.OrderService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "admin/orders", layout = MainLayout.class)
@PageTitle("All Orders")
@RolesAllowed("ADMIN")
public class AdminOrdersView extends VerticalLayout {

    public AdminOrdersView(OrderService orderService) {
        addClassName("admin-orders-view");
        setPadding(true);

        H2 title = new H2("All Orders");
        add(title);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        Grid<Order> grid = new Grid<>(Order.class, false);
        grid.addColumn(o -> "#" + o.getId()).setHeader("ID");
        grid.addColumn(o -> o.getCustomer().getFullName()).setHeader("Customer").setSortable(true);
        grid.addColumn(o -> o.getFarmer().getFullName()).setHeader("Farmer").setSortable(true);
        grid.addColumn(o -> o.getOrderDate().format(fmt)).setHeader("Date").setSortable(true);
        grid.addColumn(o -> "₹" + String.format("%.0f", o.getGrandTotal())).setHeader("Total");
        grid.addColumn(o -> o.getDeliveryType()).setHeader("Type");
        grid.addComponentColumn(o -> {
            Span badge = new Span(o.getStatus().name());
            badge.addClassName("status-badge");
            badge.addClassName("status-" + o.getStatus().name().toLowerCase());
            return badge;
        }).setHeader("Status");

        grid.setItems(orderService.findAll());
        grid.setAllRowsVisible(true);
        add(grid);
    }
}
