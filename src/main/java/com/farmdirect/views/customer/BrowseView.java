package com.farmdirect.views.customer;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.*;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "customer/browse", layout = MainLayout.class)
@PageTitle("Browse Products")
@RolesAllowed("CUSTOMER")
public class BrowseView extends VerticalLayout {

    private final ProductService productService;
    private final CartService cartService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final FlexLayout productGrid = new FlexLayout();

    public BrowseView(ProductService productService, CartService cartService,
                      FavoriteService favoriteService, UserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.favoriteService = favoriteService;
        this.userService = userService;

        addClassName("browse-view");
        setPadding(true);

        H2 title = new H2("Fresh from the Farm");

        // Search & filter bar
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search products...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");

        ComboBox<String> categoryFilter = new ComboBox<>("Category");
        List<String> cats = productService.findAllCategories();
        cats.add(0, "All");
        categoryFilter.setItems(cats);
        categoryFilter.setValue("All");

        Button searchBtn = new Button("Search", e -> {
            String keyword = searchField.getValue();
            String cat = categoryFilter.getValue();
            List<Product> results;
            if (keyword != null && !keyword.isBlank()) {
                results = productService.searchProducts(keyword);
            } else if (cat != null && !"All".equals(cat)) {
                results = productService.findByCategory(cat);
            } else {
                results = productService.findAllAvailable();
            }
            displayProducts(results);
        });
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        categoryFilter.addValueChangeListener(e -> {
            if ("All".equals(e.getValue())) {
                displayProducts(productService.findAllAvailable());
            } else {
                displayProducts(productService.findByCategory(e.getValue()));
            }
        });

        HorizontalLayout filters = new HorizontalLayout(searchField, categoryFilter, searchBtn);
        filters.setDefaultVerticalComponentAlignment(Alignment.END);

        productGrid.addClassName("product-grid");
        productGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        add(title, filters, productGrid);
        displayProducts(productService.findAllAvailable());
    }

    private void displayProducts(List<Product> products) {
        productGrid.removeAll();

        if (products.isEmpty()) {
            productGrid.add(new Paragraph("No products found."));
            return;
        }

        User customer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();

        for (Product p : products) {
            Div card = new Div();
            card.addClassName("product-card");

            // Freshness badge
            Span freshBadge = new Span(p.getFreshnessStatus().name());
            freshBadge.addClassName("freshness-badge");
            freshBadge.addClassName("freshness-" + p.getFreshnessStatus().name().toLowerCase());

            // Organic badge
            Span organicBadge = new Span(p.isOrganic() ? "Organic" : "");
            organicBadge.addClassName("organic-badge");

            H3 name = new H3(p.getName());
            if (p.getNameTamil() != null && !p.getNameTamil().isBlank()) {
                name.add(new Span(" (" + p.getNameTamil() + ")"));
            }

            Paragraph category = new Paragraph(p.getCategory());
            category.addClassName("product-category");

            H3 price = new H3("₹" + String.format("%.0f", p.getPricePerUnit()) + "/" + p.getUnit());
            price.addClassName("product-price");

            Paragraph farmer = new Paragraph(p.getFarmer().getFullName()
                    + " — " + p.getFarmer().getCity());
            farmer.addClassName("product-farmer");

            Paragraph stock = new Paragraph("Stock: " + String.format("%.0f", p.getStockQuantity()) + " " + p.getUnit());

            Paragraph desc = new Paragraph(p.getDescription() != null ? p.getDescription() : "");
            desc.addClassName("product-desc");

            // Rating
            Span ratingSpan = new Span("Rating: " + String.format("%.1f", p.getRating())
                    + " (" + p.getReviewCount() + " reviews)");

            // Favourite button
            boolean isFav = favoriteService.isFavorite(customer, p);
            Button favBtn = new Button(VaadinIcon.HEART.create(), e -> {
                favoriteService.toggleFavorite(customer, p);
                // Refresh page
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            favBtn.addClassName("fav-btn");
            favBtn.setAriaLabel(isFav ? "Remove from favourites" : "Add to favourites");

            // Add to cart button
            Button addToCartBtn = new Button("Add to Cart", e -> {
                cartService.addToCart(p, 1);
                Notification.show(p.getName() + " added to cart!", 2000,
                        Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            addToCartBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            HorizontalLayout badges = new HorizontalLayout(freshBadge, organicBadge);
            HorizontalLayout actions = new HorizontalLayout(addToCartBtn, favBtn);

            card.add(badges, name, category, price, farmer, stock, desc, ratingSpan, actions);
            productGrid.add(card);
        }
    }
}
