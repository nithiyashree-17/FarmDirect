package com.farmdirect.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Locale;
import java.util.Set;

@Route("login")
@PageTitle("Login — FarmDirect")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final Paragraph accountType = new Paragraph();
    private final Paragraph roleMismatch = new Paragraph();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        H1 title = new H1("🌾 FarmDirect");
        title.addClassName("login-title");

        Paragraph subtitle = new Paragraph("Farm to Table — No Middlemen");
        subtitle.addClassName("login-subtitle");
        accountType.addClassName("login-account-type");
        roleMismatch.addClassName("login-role-error");
        roleMismatch.setVisible(false);

        RouterLink registerLink = new RouterLink("New here? Register now", RegisterView.class);
        registerLink.addClassName("register-link");
        RouterLink homeLink = new RouterLink("Choose another account type", HomeRedirectView.class);
        homeLink.addClassName("register-link");

        add(title, subtitle, accountType, loginForm, roleMismatch, registerLink, homeLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String role = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("role", java.util.List.of()).stream()
                .findFirst()
                .orElse("")
                .toLowerCase(Locale.ROOT);
        loginForm.setAction(
                Set.of("farmer", "customer", "admin").contains(role)
                        ? "login?role=" + role
                        : "login"
        );
        String roleLabel = switch (role) {
            case "farmer" -> "Farmer";
            case "customer" -> "Customer";
            case "admin" -> "Admin";
            default -> "";
        };
        accountType.setText(roleLabel.isEmpty() ? "" : "Sign in as " + roleLabel);
        boolean roleMismatchFound = event.getLocation().getQueryParameters().getParameters()
                .containsKey("roleMismatch");
        roleMismatch.setText("This account is not a " + roleLabel
                + " account. Please use the correct username or choose another account type.");
        roleMismatch.setVisible(roleMismatchFound && !roleLabel.isEmpty());

        if (event.getLocation().getQueryParameters().getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
