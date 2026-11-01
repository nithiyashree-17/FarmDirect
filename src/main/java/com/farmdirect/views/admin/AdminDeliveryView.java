package com.farmdirect.views.admin;

import com.farmdirect.entity.DeliveryAgent;
import com.farmdirect.service.DeliveryAgentService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/delivery", layout = MainLayout.class)
@PageTitle("Delivery Agents")
@RolesAllowed("ADMIN")
public class AdminDeliveryView extends VerticalLayout {

    private final DeliveryAgentService agentService;
    private final Grid<DeliveryAgent> grid = new Grid<>(DeliveryAgent.class, false);

    public AdminDeliveryView(DeliveryAgentService agentService) {
        this.agentService = agentService;
        addClassName("admin-delivery-view");
        setPadding(true);

        H2 title = new H2("Delivery Agents");
        Button addBtn = new Button("+ Add Agent", e -> openDialog(null));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(title, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        header.setWidthFull();

        setupGrid();
        add(header, grid);
        refreshGrid();
    }

    private void setupGrid() {
        grid.addColumn(DeliveryAgent::getName).setHeader("Name").setSortable(true);
        grid.addColumn(DeliveryAgent::getPhone).setHeader("Phone");
        grid.addColumn(DeliveryAgent::getVehicleType).setHeader("Vehicle");
        grid.addColumn(DeliveryAgent::getVehicleNumber).setHeader("Number");
        grid.addColumn(DeliveryAgent::getArea).setHeader("Area");
        grid.addComponentColumn(a -> {
            Span badge = new Span(a.isAvailable() ? "Available" : "Busy");
            badge.addClassName(a.isAvailable() ? "badge-active" : "badge-inactive");
            return badge;
        }).setHeader("Status");
        grid.addColumn(a -> String.format("%.1f", a.getRating())).setHeader("Rating");
        grid.addColumn(DeliveryAgent::getCompletedDeliveries).setHeader("Deliveries");
        grid.addComponentColumn(a -> {
            Button edit = new Button("Edit", e -> openDialog(a));
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button toggle = new Button(a.isAvailable() ? "Set Busy" : "Set Free", e -> {
                a.setAvailable(!a.isAvailable());
                agentService.save(a);
                refreshGrid();
            });
            toggle.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            return new HorizontalLayout(edit, toggle);
        }).setHeader("Actions");

        grid.setAllRowsVisible(true);
    }

    private void openDialog(DeliveryAgent existing) {
        Dialog dialog = new Dialog(existing == null ? "Add Agent" : "Edit Agent");
        dialog.setWidth("450px");

        TextField name = new TextField("Name");
        TextField phone = new TextField("Phone");
        ComboBox<String> vehicle = new ComboBox<>("Vehicle Type");
        vehicle.setItems("Bike", "Auto", "Van", "Truck");
        TextField vehicleNumber = new TextField("Vehicle Number");
        TextField area = new TextField("Coverage Area");
        NumberField rating = new NumberField("Rating");
        rating.setMin(0); rating.setMax(5); rating.setStep(0.1);

        if (existing != null) {
            name.setValue(existing.getName());
            phone.setValue(existing.getPhone() != null ? existing.getPhone() : "");
            vehicle.setValue(existing.getVehicleType());
            vehicleNumber.setValue(existing.getVehicleNumber() != null ? existing.getVehicleNumber() : "");
            area.setValue(existing.getArea() != null ? existing.getArea() : "");
            rating.setValue(existing.getRating());
        }

        FormLayout form = new FormLayout(name, phone, vehicle, vehicleNumber, area, rating);

        Button saveBtn = new Button("Save", e -> {
            DeliveryAgent agent = existing != null ? existing : new DeliveryAgent();
            agent.setName(name.getValue());
            agent.setPhone(phone.getValue());
            agent.setVehicleType(vehicle.getValue());
            agent.setVehicleNumber(vehicleNumber.getValue());
            agent.setArea(area.getValue());
            agent.setRating(rating.getValue() != null ? rating.getValue() : 4.0);
            agentService.save(agent);
            dialog.close();
            refreshGrid();
            Notification.show("Agent saved!", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(form);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(agentService.findAll());
    }
}
