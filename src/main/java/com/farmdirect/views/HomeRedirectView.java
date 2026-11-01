package com.farmdirect.views;

import com.farmdirect.entity.Role;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.UserService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@PageTitle("FarmDirect — Farm to Table")
@AnonymousAllowed
public class HomeRedirectView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;

    public HomeRedirectView(UserService userService) {
        this.userService = userService;
        addClassName("landing-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("🌾 FarmDirect");
        title.addClassName("landing-title");
        Paragraph subtitle = new Paragraph("Farm to Table — No Middlemen");
        subtitle.addClassName("landing-subtitle");

        FlexLayout roleOptions = new FlexLayout(
                createRoleCard("Farmer", "Manage your farm, products, and customer orders.",
                        "Continue as Farmer", "farmer"),
                createRoleCard("Customer", "Discover fresh products and shop directly from local farms.",
                        "Continue as Customer", "customer"),
                createRoleCard("Admin", "Manage the FarmDirect platform and its community.",
                        "Continue as Admin", "admin")
        );
        roleOptions.addClassName("role-options");
        roleOptions.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        roleOptions.setJustifyContentMode(JustifyContentMode.CENTER);

        Anchor registerLink = new Anchor("register", "New to FarmDirect? Create a farmer or customer account");
        registerLink.addClassName("landing-register-link");

        add(title, subtitle, roleOptions, registerLink);
    }

    private VerticalLayout createRoleCard(String role, String description, String action, String roleId) {
        H2 heading = new H2(role);
        Paragraph details = new Paragraph(description);
        Anchor loginLink = new Anchor("login?role=" + roleId, action);
        loginLink.addClassName("role-login-link");

        VerticalLayout card = new VerticalLayout(heading, details, loginLink);
        card.addClassName("role-card");
        card.setAlignItems(Alignment.CENTER);
        return card;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String username = SecurityUtils.getLoggedInUsername();
        if (username == null) {
            return;
        }

        userService.findByUsername(username).ifPresent(user -> {
            String destination = switch (user.getRole()) {
                case FARMER -> "farmer/dashboard";
                case CUSTOMER -> "customer/browse";
                case ADMIN -> "admin/dashboard";
            };
            event.forwardTo(destination);
        });
    }
}
