package com.farmdirect.views.farmer;

import com.farmdirect.entity.Order;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.OrderService;
import com.farmdirect.service.ProductService;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "farmer/dashboard", layout = MainLayout.class)
@PageTitle("Farmer Dashboard")
@RolesAllowed("FARMER")
public class FarmerDashboardView extends VerticalLayout {

    public FarmerDashboardView(UserService userService, ProductService productService, OrderService orderService) {
        addClassName("dashboard-view");
        setPadding(true);
        setSpacing(true);

        User farmer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();

        H2 welcome = new H2("Welcome, " + farmer.getFullName());
        add(welcome);

        long productCount = productService.countByFarmer(farmer);
        long orderCount = orderService.countByFarmer(farmer);
        Double earnings = orderService.totalEarnings(farmer);

        FlexLayout stats = new FlexLayout();
        stats.addClassName("stats-row");
        stats.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        stats.add(createStatCard("Products", String.valueOf(productCount), "stat-card", "green"));
        stats.add(createStatCard("Orders", String.valueOf(orderCount), "stat-card", "blue"));
        stats.add(createStatCard("Earnings", "₹" + String.format("%.0f", earnings), "stat-card", "gold"));

        add(stats);

        // Recent orders
        H3 recentTitle = new H3("Recent Orders");
        add(recentTitle);

        List<Order> recentOrders = orderService.findByFarmer(farmer);
        if (recentOrders.isEmpty()) {
            add(new Paragraph("No orders yet. Add products to start selling!"));
        } else {
            for (Order order : recentOrders.stream().limit(5).toList()) {
                Div orderCard = new Div();
                orderCard.addClassName("order-card");
                orderCard.add(
                        new Span("Order #" + order.getId() + " — " + order.getCustomer().getFullName()),
                        new Span(" | ₹" + String.format("%.0f", order.getGrandTotal())),
                        new Span(" | " + order.getStatus().name())
                );
                add(orderCard);
            }
        }
    }

    private Div createStatCard(String label, String value, String... classNames) {
        Div card = new Div();
        card.addClassNames(classNames);
        H3 val = new H3(value);
        val.addClassName("stat-value");
        Paragraph lbl = new Paragraph(label);
        lbl.addClassName("stat-label");
        card.add(val, lbl);
        return card;
    }
}
