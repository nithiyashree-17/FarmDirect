package com.farmdirect.views.admin;

import com.farmdirect.entity.User;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Manage Users")
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public AdminUsersView(UserService userService) {
        this.userService = userService;
        addClassName("admin-users-view");
        setPadding(true);

        H2 title = new H2("Manage Users");
        add(title);

        grid.addColumn(User::getId).setHeader("ID").setWidth("60px");
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Name").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true);
        grid.addColumn(User::getCity).setHeader("City");
        grid.addColumn(User::getPhone).setHeader("Phone");
        grid.addComponentColumn(u -> {
            Span status = new Span(u.isActive() ? "Active" : "Inactive");
            status.addClassName(u.isActive() ? "badge-active" : "badge-inactive");
            return status;
        }).setHeader("Status");
        grid.addComponentColumn(u -> {
            Button toggleBtn = new Button(u.isActive() ? "Deactivate" : "Activate", e -> {
                userService.toggleActive(u.getId());
                refreshGrid();
            });
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL,
                    u.isActive() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
            return toggleBtn;
        }).setHeader("Action");

        grid.setAllRowsVisible(true);
        add(grid);
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(userService.findAll());
    }
}
