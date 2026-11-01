package com.farmdirect.views.admin;

import com.farmdirect.entity.Product;
import com.farmdirect.service.ProductService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/products", layout = MainLayout.class)
@PageTitle("All Products")
@RolesAllowed("ADMIN")
public class AdminProductsView extends VerticalLayout {

    public AdminProductsView(ProductService productService) {
        addClassName("admin-products-view");
        setPadding(true);

        H2 title = new H2("All Products");
        add(title);

        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addColumn(Product::getName).setHeader("Product").setSortable(true);
        grid.addColumn(p -> p.getFarmer().getFullName()).setHeader("Farmer").setSortable(true);
        grid.addColumn(Product::getCategory).setHeader("Category").setSortable(true);
        grid.addColumn(p -> "₹" + String.format("%.0f", p.getPricePerUnit())).setHeader("Price");
        grid.addColumn(p -> String.format("%.0f", p.getStockQuantity()) + " " + p.getUnit()).setHeader("Stock");
        grid.addComponentColumn(p -> {
            Span badge = new Span(p.getFreshnessStatus().name());
            badge.addClassName("freshness-badge");
            badge.addClassName("freshness-" + p.getFreshnessStatus().name().toLowerCase());
            return badge;
        }).setHeader("Freshness");
        grid.addColumn(p -> p.isOrganic() ? "Organic" : "Regular").setHeader("Type");
        grid.addColumn(p -> p.isAvailable() ? "Yes" : "No").setHeader("Available");

        grid.setItems(productService.findAll());
        grid.setAllRowsVisible(true);
        add(grid);
    }
}
