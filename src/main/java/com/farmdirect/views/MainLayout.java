package com.farmdirect.views;

import com.farmdirect.entity.Role;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.UserService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final UserService userService;

    public MainLayout(UserService userService) {
        this.userService = userService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("FarmDirect");
        logo.addClassName("app-logo");

        String username = SecurityUtils.getLoggedInUsername();
        Span userSpan = new Span(username != null ? "Hi, " + username : "");
        userSpan.addClassName("user-greeting");

        Button logoutBtn = new Button("Logout", e -> {
            SecurityUtils.logout();
            getUI().ifPresent(ui -> ui.getPage().setLocation("login"));
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(), logo, userSpan, logoutBtn
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassName("app-header");

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        String username = SecurityUtils.getLoggedInUsername();
        if (username == null) return;

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return;

        if (user.getRole() == Role.FARMER) {
            nav.addItem(new SideNavItem("Dashboard", "farmer/dashboard", VaadinIcon.DASHBOARD.create()));
            nav.addItem(new SideNavItem("My Products", "farmer/products", VaadinIcon.PACKAGE.create()));
            nav.addItem(new SideNavItem("Orders", "farmer/orders", VaadinIcon.CART.create()));
            nav.addItem(new SideNavItem("Profile", "farmer/profile", VaadinIcon.USER.create()));
        } else if (user.getRole() == Role.CUSTOMER) {
            nav.addItem(new SideNavItem("Browse", "customer/browse", VaadinIcon.SHOP.create()));
            nav.addItem(new SideNavItem("My Cart", "customer/cart", VaadinIcon.CART.create()));
            nav.addItem(new SideNavItem("My Orders", "customer/orders", VaadinIcon.LIST.create()));
            nav.addItem(new SideNavItem("Favourites", "customer/favourites", VaadinIcon.HEART.create()));
            nav.addItem(new SideNavItem("Nearby Farmers", "customer/nearby", VaadinIcon.MAP_MARKER.create()));
        } else if (user.getRole() == Role.ADMIN) {
            nav.addItem(new SideNavItem("Dashboard", "admin/dashboard", VaadinIcon.DASHBOARD.create()));
            nav.addItem(new SideNavItem("Users", "admin/users", VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("All Orders", "admin/orders", VaadinIcon.CART.create()));
            nav.addItem(new SideNavItem("Products", "admin/products", VaadinIcon.PACKAGE.create()));
            nav.addItem(new SideNavItem("Delivery Agents", "admin/delivery", VaadinIcon.TRUCK.create()));
        }

        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String username = SecurityUtils.getLoggedInUsername();
        if (username == null) return;

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return;

        // Redirect root to role-specific dashboard
        String path = event.getLocation().getPath();
        if (path.isEmpty() || path.equals("/")) {
            switch (user.getRole()) {
                case FARMER -> event.forwardTo("farmer/dashboard");
                case CUSTOMER -> event.forwardTo("customer/browse");
                case ADMIN -> event.forwardTo("admin/dashboard");
            }
        }
    }
}
