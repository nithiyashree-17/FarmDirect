package com.farmdirect.views.farmer;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.ProductService;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;

@Route(value = "farmer/products", layout = MainLayout.class)
@PageTitle("My Products")
@RolesAllowed("FARMER")
public class FarmerProductsView extends VerticalLayout {

    private final ProductService productService;
    private final UserService userService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private User farmer;

    public FarmerProductsView(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
        this.farmer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();

        addClassName("products-view");
        setPadding(true);

        H2 title = new H2("My Products");
        Button addBtn = new Button("+ Add Product", e -> openProductDialog(null));
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
        grid.addColumn(Product::getName).setHeader("Product").setSortable(true);
        grid.addColumn(Product::getCategory).setHeader("Category").setSortable(true);
        grid.addColumn(p -> "₹" + String.format("%.0f", p.getPricePerUnit()) + "/" + p.getUnit())
                .setHeader("Price");
        grid.addColumn(p -> String.format("%.1f", p.getStockQuantity()) + " " + p.getUnit())
                .setHeader("Stock");
        grid.addComponentColumn(p -> {
            Span badge = new Span(p.getFreshnessStatus().name());
            badge.addClassName("freshness-badge");
            badge.addClassName("freshness-" + p.getFreshnessStatus().name().toLowerCase());
            return badge;
        }).setHeader("Freshness");
        grid.addColumn(p -> p.isOrganic() ? "Organic" : "Regular").setHeader("Type");
        grid.addComponentColumn(p -> {
            Button edit = new Button("Edit", e -> openProductDialog(p));
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button toggle = new Button(p.isAvailable() ? "Hide" : "Show", e -> {
                p.setAvailable(!p.isAvailable());
                productService.save(p);
                refreshGrid();
            });
            toggle.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            return new HorizontalLayout(edit, toggle);
        }).setHeader("Actions");

        grid.setAllRowsVisible(true);
    }

    private void openProductDialog(Product existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Add Product" : "Edit Product");
        dialog.setWidth("500px");

        TextField name = new TextField("Product Name");
        TextField nameTamil = new TextField("Tamil Name");
        ComboBox<String> category = new ComboBox<>("Category");
        category.setItems("Vegetables", "Fruits", "Grains", "Dairy", "Spices", "Flowers", "Pulses");
        NumberField price = new NumberField("Price (₹)");
        price.setMin(1);
        ComboBox<String> unit = new ComboBox<>("Unit");
        unit.setItems("kg", "litre", "dozen", "piece", "bundle", "gram");
        NumberField stock = new NumberField("Stock Quantity");
        stock.setMin(0);
        TextArea description = new TextArea("Description");
        DatePicker harvestDate = new DatePicker("Harvest Date");
        DatePicker expiryDate = new DatePicker("Expiry Date");
        Checkbox organic = new Checkbox("Organic Product");

        if (existing != null) {
            name.setValue(existing.getName());
            if (existing.getNameTamil() != null) nameTamil.setValue(existing.getNameTamil());
            category.setValue(existing.getCategory());
            price.setValue(existing.getPricePerUnit());
            unit.setValue(existing.getUnit());
            stock.setValue(existing.getStockQuantity());
            if (existing.getDescription() != null) description.setValue(existing.getDescription());
            harvestDate.setValue(existing.getHarvestDate());
            expiryDate.setValue(existing.getExpiryDate());
            organic.setValue(existing.isOrganic());
        } else {
            harvestDate.setValue(LocalDate.now());
            expiryDate.setValue(LocalDate.now().plusDays(7));
        }

        FormLayout form = new FormLayout(name, nameTamil, category, price, unit, stock,
                description, harvestDate, expiryDate, organic);

        Button saveBtn = new Button("Save", e -> {
            Product p = existing != null ? existing : new Product();
            p.setFarmer(farmer);
            p.setName(name.getValue());
            p.setNameTamil(nameTamil.getValue());
            p.setCategory(category.getValue());
            p.setPricePerUnit(price.getValue() != null ? price.getValue() : 0);
            p.setUnit(unit.getValue());
            p.setStockQuantity(stock.getValue() != null ? stock.getValue() : 0);
            p.setDescription(description.getValue());
            p.setHarvestDate(harvestDate.getValue());
            p.setExpiryDate(expiryDate.getValue());
            p.setOrganic(organic.getValue());
            p.setAvailable(true);

            productService.save(p);
            dialog.close();
            refreshGrid();
            Notification.show("Product saved!", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void refreshGrid() {
        List<Product> products = productService.findByFarmer(farmer);
        grid.setItems(products);
    }
}
