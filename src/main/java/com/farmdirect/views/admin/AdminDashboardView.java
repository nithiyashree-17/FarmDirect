package com.farmdirect.views.admin;

import com.farmdirect.entity.Role;
import com.farmdirect.service.*;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Admin Dashboard")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    public AdminDashboardView(UserService userService, ProductService productService,
                               OrderService orderService, DeliveryAgentService agentService) {
        addClassName("dashboard-view");
        setPadding(true);

        H2 title = new H2("Admin Dashboard");
        add(title);

        long farmers = userService.countByRole(Role.FARMER);
        long customers = userService.countByRole(Role.CUSTOMER);
        long totalProducts = productService.findAll().size();
        long totalOrders = orderService.findAll().size();
        long agents = agentService.findAll().size();

        FlexLayout stats = new FlexLayout();
        stats.addClassName("stats-row");
        stats.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        stats.add(createStatCard("Farmers", String.valueOf(farmers), "stat-card", "green"));
        stats.add(createStatCard("Customers", String.valueOf(customers), "stat-card", "blue"));
        stats.add(createStatCard("Products", String.valueOf(totalProducts), "stat-card", "teal"));
        stats.add(createStatCard("Orders", String.valueOf(totalOrders), "stat-card", "gold"));
        stats.add(createStatCard("Agents", String.valueOf(agents), "stat-card", "orange"));

        add(stats);

        // Platform summary
        H3 summaryTitle = new H3("Platform Overview");
        Paragraph summary = new Paragraph(
                "FarmDirect is connecting " + farmers + " farmers with " + customers
                        + " customers across Tamil Nadu. " + totalProducts + " products are listed "
                        + "and " + totalOrders + " orders have been placed."
        );
        add(summaryTitle, summary);
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
