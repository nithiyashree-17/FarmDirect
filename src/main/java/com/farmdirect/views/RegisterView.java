package com.farmdirect.views;

import com.farmdirect.entity.Role;
import com.farmdirect.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Register — FarmDirect")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserService userService) {
        addClassName("register-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("🌾 Join FarmDirect");

        TextField username = new TextField("Username");
        username.setRequired(true);
        username.setMinLength(3);

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);
        password.setMinLength(4);

        TextField fullName = new TextField("Full Name");
        fullName.setRequired(true);

        EmailField email = new EmailField("Email");
        TextField phone = new TextField("Phone");

        ComboBox<Role> roleSelect = new ComboBox<>("I am a...");
        roleSelect.setItems(Role.FARMER, Role.CUSTOMER);
        roleSelect.setItemLabelGenerator(r -> r == Role.FARMER ? "Farmer" : "Customer");
        roleSelect.setRequired(true);

        TextField city = new TextField("City / Village");
        TextField district = new TextField("District");
        district.setValue("Coimbatore");
        TextField pincode = new TextField("Pincode");

        FormLayout form = new FormLayout();
        form.add(username, password, fullName, email, phone, roleSelect, city, district, pincode);
        form.setMaxWidth("500px");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        Button registerBtn = new Button("Register", e -> {
            try {
                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || roleSelect.isEmpty()) {
                    Notification.show("Please fill all required fields", 3000,
                            Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                userService.register(
                        username.getValue(), password.getValue(), fullName.getValue(),
                        email.getValue(), phone.getValue(), roleSelect.getValue(),
                        "", city.getValue(), district.getValue(), pincode.getValue(),
                        11.0168 + Math.random() * 0.1,  // Random lat near Coimbatore
                        76.9558 + Math.random() * 0.1   // Random lon near Coimbatore
                );
                Notification.show("Registration successful! Please log in.", 3000,
                        Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 4000,
                        Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();

        RouterLink loginLink = new RouterLink("Already registered? Log in", LoginView.class);

        VerticalLayout card = new VerticalLayout(title, form, registerBtn, loginLink);
        card.addClassName("register-card");
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setMaxWidth("550px");

        add(card);
    }
}
